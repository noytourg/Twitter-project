#include "../include/connectionHandler.h"
#include <algorithm>
#include <boost/algorithm/string.hpp>

using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;
 
ConnectionHandler::ConnectionHandler(string host, short port, char delimiter) :
        host_(host), port_(port), io_service_(), socket_(io_service_), delimiter_(delimiter),
        isLoggedIn(false), isLoggedOut(false), receivedLogInResponse(false) {}
    
ConnectionHandler::~ConnectionHandler() {
    close();
}
 
bool ConnectionHandler::connect() {
    std::cout << "Starting connect to " 
        << host_ << ":" << port_ << std::endl;
    try {
		tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
		boost::system::error_code error;
		socket_.connect(endpoint, error);
		if (error)
			throw boost::system::system_error(error);
    }
    catch (std::exception& e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}
 
bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
	boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp ) {
			tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);			
        }
		if(error)
			throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
	boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp ) {
			tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
		if(error)
			throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}
 
bool ConnectionHandler::getLine(std::string& line) {
    return getFrameAscii(line, '\0');
}

bool ConnectionHandler::sendLine(std::string& line) {
    return sendFrameAscii(line, '\0');
}
 
bool ConnectionHandler::getFrameAscii(std::string& frame, char delimiter) {
    char ch;
    // Stop when we encounter the null character. 
    // Notice that the null character is not appended to the frame string.
    try {
		do{
			getBytes(&ch, 1);
            frame.append(1, ch);
        }while (delimiter != ch);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}
 
bool ConnectionHandler::sendFrameAscii(const std::string& frame, char delimiter) {
	bool result=sendBytes(frame.c_str(),frame.length());
	if(!result) return false;
	return sendBytes(&delimiter,1);
}
 
// Close down the connection properly.
void ConnectionHandler::close() {
    try{
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}


bool ConnectionHandler::encode (std::string input){
    int firstDelimiter = input.find(' ');
    std::string firstWord = input.substr (0, firstDelimiter);
    std::string command = input.substr(firstDelimiter+1);
    if (firstWord == "REGISTER") {
        return encodeRegister(command);
    }
    else if (firstWord == "LOGIN") {
        return encodeLogIn(command);
    }
    else if (firstWord == "LOGOUT") {
        return encodeLogOut(command);
    }
    else if (firstWord == "FOLLOW") {
        return encodeFollow(command);
    }
    else if (firstWord == "POST") {
        return encodePost(command);
    }
    else if (firstWord == "PM") {
        return encodePM(command);
    }
    else if (firstWord == "USERLIST") {
        return encodeUserList();
    }
    else if (firstWord == "STAT") {
        return encodeStat(command);
    }
return false;
}

bool ConnectionHandler::decode (){
    char opcode[2];
    bool success (getBytes(opcode, 2));
    if (success) {
        short command;
        command = bytesToShort(opcode);
        if (command == 9) {
            return decodeNotification();
        }
        else if (command == 10) {
            return decodeACK();
        }
        else if (command == 11) {
            return decodeError();
        }
    }
    return success;
}



bool ConnectionHandler::encodeRegister (std::string input){
     char opcode[2];
     shortToBytes(1, opcode);
     std::replace (input.begin(), input.end(), ' ', delimiter_);
     return sendBytes (opcode, sizeof(opcode)) & sendLine(input);
}

bool ConnectionHandler::encodeLogIn (std::string input){
    char opcode[2];
    shortToBytes(2, opcode);
    std::replace (input.begin(), input.end(), ' ', delimiter_);
    return sendBytes (opcode, sizeof(opcode)) & sendLine(input);
}

bool ConnectionHandler::encodeLogOut (std::string input){
    char opcode[2];
    shortToBytes(3, opcode);
    return sendBytes (opcode, sizeof(opcode));
}

bool ConnectionHandler::encodeFollow (std::string input){
    char opcode[2];
    shortToBytes(4, opcode);
    int split = input.find(' ', 2);
    std::string firstPart = input.substr (0, split);
    boost::erase_all (firstPart, " ");
    std::string secondPart = input.substr (split+1);
    std::replace(secondPart.begin(), secondPart.end(), ' ', delimiter_);
    return sendBytes (opcode, sizeof(opcode)) & sendBytes (firstPart.c_str(), firstPart.size()) & sendLine(secondPart);
}


bool ConnectionHandler::encodePost (std::string input){
    char opcode[2];
    shortToBytes(5, opcode);
    return sendBytes(opcode, sizeof(opcode)) & sendLine(input);
}

bool ConnectionHandler::encodePM (std::string input){
    char opcode[2];
    shortToBytes(6, opcode);
    int firstSpace = input.find(' ');
    std::string userName = input.substr(0, firstSpace);
    std::string content = input.substr(firstSpace+1);
    return sendBytes(opcode, sizeof(opcode)) & sendLine(userName) & sendLine(content);
}

bool ConnectionHandler::encodeUserList (){
    char opcode[2];
    shortToBytes(7, opcode);
    return sendBytes(opcode, sizeof(opcode));
}

bool ConnectionHandler::encodeStat (std::string input){
    char opcode[2];
    shortToBytes(8, opcode);
    return sendBytes(opcode, sizeof(opcode)) & sendLine(input);
}




bool ConnectionHandler::decodeNotification (){
    string output ("NOTIFICATION ");
    char type[1];
    bool success (getBytes(type, 1));
    if (success) {
        if (type[0] == '0')
            output.append("PM ");
        else
            output.append("Public ");
        string postingUser, content;
        success = success & getLine(postingUser);
        if (success) {
            output.append(postingUser).erase(output.size()-1);
            output.append(" ");
            success = success & getLine(content);
            if (success) {
                output.append(content).erase(output.size()-1);
                output.append(" ");
                cout << output << endl;
                return success;
            }
        }
    }
    return success;
}

bool ConnectionHandler::decodeACK (){
    char msgOpcode[2];
    bool success (getBytes(msgOpcode, 2));
    if (success){
        short command (bytesToShort(msgOpcode));
        if ((command == 1) |(command == 2) |(command == 3) |(command == 5) |(command == 6)){
            if (command == 2) {
                isLoggedIn = true;
                receivedLogInResponse = true;
            } else if (command == 3)
                isLoggedOut = true;
            cout << "ACK " << command << endl;
            return  success;
        } else if ((command == 4)| (command == 7)){
           return decodeACKFollowOrUserlist(command);
        } else
            return decodeACKStat();

    }
    return success;

}
bool ConnectionHandler::decodeACKFollowOrUserlist(short command){
    string output;
    output.append("ACK ").append(std:: to_string(command)).append(" ");
    char numOfUsers[2];
    bool success (getBytes(numOfUsers, 2));
    if (success){
        short listLength(bytesToShort(numOfUsers));
        output.append(std::to_string(listLength));
        string userName;
        for (int j = 0; j < listLength; ++j) {
            success = success & getLine(userName);
            output.append(" ").append(userName);
            output.erase(output.size()-1);
            userName = "";
        }
        cout << output << endl;
    }
    return success;
}

bool ConnectionHandler::decodeACKStat() {
    string output;
    output.append("ACK 8 ");
    char numOfPosts[2];
    bool success(getBytes(numOfPosts, 2));
    if (success) {
        output.append(std::to_string(bytesToShort(numOfPosts))).append(" ");
        char numOfFollowers[2];
        success = success & (getBytes(numOfFollowers, 2));
        if (success) {
            output.append(std::to_string(bytesToShort(numOfFollowers))).append(" ");
            char numOfFollowing[2];
            success = success & (getBytes(numOfFollowing, 2));
            if (success){
                output.append(std::to_string(bytesToShort(numOfFollowing)));
                cout << output << endl;
		}
        }
    }
    return  success;
}

bool ConnectionHandler::decodeError (){
    char msgOpcode[2];
    bool success (getBytes(msgOpcode, 2));
    if (success) {
        short opcode (bytesToShort(msgOpcode));
        if (opcode == 2){
            receivedLogInResponse = true;
            cout << "ERROR " << opcode << endl;
	}
    }
return success;
}



void ConnectionHandler::shortToBytes(short num, char* bytesArr) {
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}


short ConnectionHandler::bytesToShort(char* bytesArr) {
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}
