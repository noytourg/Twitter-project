package bgu.spl.net.api.bidi;

import bgu.spl.net.api.bidi.message.Message;
import bgu.spl.net.api.bidi.message.NotificationMessage;
import bgu.spl.net.api.bidi.message.PMMessage;
import bgu.spl.net.api.bidi.message.PostMessage;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerData {

    private static ServerData instance = new ServerData();
    private ConcurrentHashMap<String, User> users; // match userName to each user
    private List<String> registrationOrder;

    private ServerData() {
        users = new ConcurrentHashMap<>();
        registrationOrder = new LinkedList<>();
    }

    public static ServerData getInstance(){
        return instance;
    }


    public boolean register (User user){
        boolean success = users.putIfAbsent(user.getUserName(), user) == null;
        if (success) {
            synchronized (registrationOrder) {
                registrationOrder.add(user.getUserName());
            }
        }
        return success;
    }

    public boolean logIn(String userName, String password, BidiMessagingProtocolImpl protocol){
        if (!isRegistered(userName))
            return false;
        User user = users.get(userName);
        synchronized (user) { //in case that 2 persons try to log in with the same user
            if (isLoggedIn(userName) | !isPasswordMatch(userName, password))
                return false;
            user.connect(protocol.getConnectionId());
            ConcurrentHashMap<String, Message> notifications = user.getNotifications();
            for (Map.Entry<String, Message> entry: notifications.entrySet())
                protocol.send(protocol.getConnectionId(), entry.getValue());
            user.emptyNotifiaction();
            return true;
        }
    }

    public boolean logOut (int connectionId) {
        User user = IDToUser(connectionId);
        if (user == null) //this user has never registered
            return false;

        synchronized (user) {
            if (isLoggedIn(user.getUserName())) {
                user.disconnect();
                return true;
            }
            else
                return false;
        }
    }

    public List<String> follow (int connectionId, List<String> toFollow){
        List<String> userNames = new LinkedList<>();
        User sender = IDToUser(connectionId);
        if (sender != null && isLoggedIn(sender.getUserName())) { //no user with this connectionID
            for (String userName : toFollow) {
                if (isRegistered(userName) && sender.addFollowing(users.get(userName)) &&
                        users.get(userName).addFollower(sender))
                    userNames.add(userName);
            }
        }
        return userNames;
    }


    public List<String> unfollow(int connectionId, List<String> toUnfollow){
        List<String> userNames = new LinkedList<>();
        User sender = IDToUser(connectionId);
        if (sender != null && isLoggedIn(sender.getUserName())) { //no user with this connectionID
            for (String userName : toUnfollow) {
                if (isRegistered(userName) && sender.removeFollowing(users.get(userName)) &&
                        users.get(userName).removeFollower(sender))
                    userNames.add(userName);
            }
        }
        return userNames;
    }


    public boolean post (List<String> userNames, BidiMessagingProtocolImpl protocol, PostMessage message){
        List<User> registeredHashtags = new LinkedList<>();
        User sender = IDToUser(protocol.getConnectionId());
        if (sender == null || !isLoggedIn(sender.getUserName()))
            return false;

        sender.addPost(message.getContent());
        for (String userName: userNames){
            if ((isRegistered(userName)) & (!registeredHashtags.contains(users.get(userName))))
                registeredHashtags.add(users.get(userName));
        }

        registeredHashtags.removeAll(sender.getFollowers());
        registeredHashtags.addAll(sender.getFollowers());

        for (User user: registeredHashtags){
            synchronized (user) {
                if (!isLoggedIn(user.getUserName()))
                    user.addNotification(sender.getUserName(), '1', message.getContent());
                else
                    protocol.send(user.getConnectionId(),
                            new NotificationMessage(sender.getUserName(), '1', message.getContent()));
            }
        }
        return true;
    }


    public boolean PM (BidiMessagingProtocolImpl protocol, PMMessage message){
        User recipient = users.get(message.getUserName());
        User sender = IDToUser(protocol.getConnectionId());
        if (sender == null || recipient == null ||
                !isLoggedIn(sender.getUserName()) || !isRegistered(recipient.getUserName()))
            return false;

        sender.addPM(message.getContent());
        synchronized (recipient) {
            if (isLoggedIn(recipient.getUserName()))
                protocol.send(recipient.getConnectionId(),
                        new NotificationMessage(sender.getUserName(), '0', message.getContent()));
            else
                recipient.addNotification(sender.getUserName(), '0', message.getContent());
        }

        return true;
    }


    public List<String> userList (int connectionId){
        User user = IDToUser(connectionId);
        if (user != null && isLoggedIn(user.getUserName())){
            return registrationOrder;
        }
        return null;
    }


    /**
     * toReturn[0] = number of posts
     * toReturn[1] = number of followers
     * toReturn[2] = number of following
     */
    public int[] stat (int connectionId, String userName){
        int[] toReturn = {-1, -1, -1};
        User sender = IDToUser(connectionId);
        if (sender != null && isLoggedIn(sender.getUserName()) & isRegistered(userName)){
            User toStock = users.get(userName);
            toReturn[0] = toStock.getPosts().size();
            toReturn[1] = toStock.getFollowers().size();
            toReturn[2] = toStock.getFollowing().size();
        }
        return toReturn;
    }



    private User IDToUser (int connectionId){
        User user = null;
        for (Map.Entry<String, User> entry: users.entrySet()){
            if (entry.getValue().getConnectionId() == connectionId) {
                user = entry.getValue();
                break;
            }
        }
        return user;
    }

    private boolean isRegistered(String userName){
        return users.containsKey(userName);
    }

    private boolean isLoggedIn (String userName){
        if (!isRegistered(userName))
            return false;
        return users.get(userName).getConnectionId() != -1;
    }

    private boolean isPasswordMatch (String userName, String password){
        if (!isRegistered(userName))
            return true;
        return users.get(userName).getPassword().equals(password);
    }
}
