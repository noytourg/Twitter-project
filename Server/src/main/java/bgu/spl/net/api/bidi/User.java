package bgu.spl.net.api.bidi;

import bgu.spl.net.api.bidi.message.Message;
import bgu.spl.net.api.bidi.message.NotificationMessage;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class User {

    private final String userName;
    private final String password;
    private int connectionId;
    private LinkedList<String> posts;
    private LinkedList<String> PM;
    private LinkedList<User> followers;
    private LinkedList<User> following;
    private ConcurrentHashMap<String, Message> notifications; //<PM/POST, Message>


    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
        this.connectionId = -1;
        followers = new LinkedList<>();
        following = new LinkedList<>();
        posts = new LinkedList<>();
        PM = new LinkedList<>();
        notifications = new ConcurrentHashMap();
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void connect (int connectionId){
        if (this.connectionId == -1)
            this.connectionId = connectionId;
    }

    public void disconnect (){
        connectionId =-1;
    }

    public boolean addFollower (User user){
        synchronized (followers) {
            if (followers.contains(user))
                return false;
            return followers.add(user);
        }
    }

    public boolean removeFollower (User user){
        synchronized (followers) {
            if (!followers.contains(user))
                return false;
            return followers.remove(user);
        }
    }

    public boolean addFollowing (User user){
        if (following.contains(user))
            return false;
        return following.add(user);
    }

    public boolean removeFollowing (User user){
        if (!following.contains(user))
            return false;
        return following.remove(user);
    }

    public synchronized void addPost (String post){
        posts.add(post);
    }

    public synchronized void addPM (String pm){
        PM.add(pm);
    }

    public void addNotification (String userName, char type, String content){
        notifications.put(userName, new NotificationMessage(userName, type, content));
    }

    public ConcurrentHashMap<String, Message> getNotifications() {
        return notifications;
    }

    public void emptyNotifiaction (){
        notifications.clear();
    }

    public LinkedList<User> getFollowers() {
        return followers;
    }

    public LinkedList<User> getFollowing() {
        return following;
    }

    public LinkedList<String> getPosts() {
        return posts;
    }

    public LinkedList<String> getPM() {
        return PM;
    }
}
