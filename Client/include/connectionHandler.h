#ifndef CONNECTION_HANDLER__
#define CONNECTION_HANDLER__
                                           
#include <string>
#include <iostream>
#include <boost/asio.hpp>

using boost::asio::ip::tcp;

class ConnectionHandler {
private:
	const std::string host_;
	const short port_;
	boost::asio::io_service io_service_;   // Provides core I/O functionality
	tcp::socket socket_;
    char delimiter_;

    bool encodeRegister (std::string);
    bool encodeLogIn (std::string);
    bool encodeLogOut (std::string);
    bool encodeFollow (std::string);
    bool encodePost (std::string);
    bool encodePM (std::string);
    bool encodeUserList ();
    bool encodeStat (std::string);

    bool decodeNotification ();
    bool decodeACK ();
    bool decodeError ();
    bool decodeACKFollowOrUserlist(short);
    bool decodeACKStat();

    short bytesToShort (char* bytesArr);
    void shortToBytes(short num, char* bytesArr);


public:
    ConnectionHandler(std::string host, short port, char delimiter);
    virtual ~ConnectionHandler();

    bool isLoggedIn;
    bool isLoggedOut;
    bool receivedLogInResponse;

    bool encode (std::string);
    bool decode ();


    // Connect to the remote machine
    bool connect();
 
    // Read a fixed number of bytes from the server - blocking.
    // Returns false in case the connection is closed before bytesToRead bytes can be read.
    bool getBytes(char bytes[], unsigned int bytesToRead);
 
	// Send a fixed number of bytes from the client - blocking.
    // Returns false in case the connection is closed before all the data is sent.
    bool sendBytes(const char bytes[], int bytesToWrite);
	
    // Read an ascii line from the server
    // Returns false in case connection closed before a newline can be read.
    bool getLine(std::string& line);
	
	// Send an ascii line from the server
    // Returns false in case connection closed before all the data is sent.
    bool sendLine(std::string& line);
 
    // Get Ascii data from the server until the delimiter character
    // Returns false in case connection closed before null can be read.
    bool getFrameAscii(std::string& frame, char delimiter);
 
    // Send a message to the remote host.
    // Returns false in case connection is closed before all the data is sent.
    bool sendFrameAscii(const std::string& frame, char delimiter);
	
    // Close down the connection properly.
    void close();



}; //class ConnectionHandler
 
#endif