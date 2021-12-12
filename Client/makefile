CFLAGS:=-c -Wall -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lpthread

all: BGSclient
	g++ -o bin/BGSclient bin/connectionHandler.o bin/BGSclient.o $(LDFLAGS) 

BGSclient: bin/connectionHandler.o bin/BGSclient.o
	
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/BGSclient.o: src/BGSclient.cpp
	g++ $(CFLAGS) -o bin/BGSclient.o src/BGSclient.cpp
	
clean:
	rm -f bin/*
