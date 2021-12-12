#include <stdlib.h>
#include <thread>
#include "../include/connectionHandler.h"

/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/

void readFromSocket (ConnectionHandler& connectionHandler){
    while (!connectionHandler.isLoggedOut) {
        connectionHandler.decode();
    }
}


int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }

    std::string host = argv[1];
    short port = atoi(argv[2]);

    ConnectionHandler connectionHandler (host, port, '\0');
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }


    std::thread read_from_socket(std::ref(readFromSocket), std::ref(connectionHandler));


    while (1) { // handle the read from keyboard
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string line(buf);
        int firstDelimiter = line.find(' ');
        std::string firstWord (line.substr(0, firstDelimiter));
        if (firstWord == "LOGIN"){
            connectionHandler.encode(line);
        } else if ((firstWord == "LOGOUT") & (connectionHandler.isLoggedIn)){
            connectionHandler.encode(line);
            break;
        } else
            connectionHandler.encode(line);
    }


    read_from_socket.join();
    connectionHandler.close();


    return 0;
}
