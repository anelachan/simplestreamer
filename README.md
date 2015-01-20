## About SimpleStreamer

I created SimpleStreamer with a classmate for a course on Distributed Systems at the University of Melbourne. SimpleStreamer is a streaming video application that enables you both to live broadcast a streaming feed from your webcam (Shiba Inu puppy cam anyone?) and to connect to other users' streaming feeds. 

## Quickstart

If you are broadcasting your webcam stream (by default, the one broadcasting the webcam feed will listen on port 6262):

```
java simplestream.SimpleStreamer
```


If you are connecting to a stream broadcast elsewhere (assuming the remote host's port was set to 6262):

```
java simplestream.SimpleStreamer -sport 6263 -remote hostname -rport 6262 -rate 400
```


### Explanation of command-line arguments

* -sport is the port on which SimpleStreamer will listen for connections. By default this is 6262. In remote mode, this should be specified as something other than 6262, as you will be using port 6262 to connect to the live stream broadcaster.
* -rport is the port to which you are trying to connect when receiving a stream.
* -remote is the name of the host to connect to
* -rate is time between sent "messages" which contain the image data. A higher number will use less bandwidth.


## Concurrency

As this is a simulation for a class, our number of concurrent clients was hard-limited at 3, BUT, if the server is "overloaded" with 3 connected clients the application will perform a breadth-first search to find a stream on which to piggyback. This is the reason -sport is specified - a connected client will also become a server and start serving the stream to other interested viewers.

## Other notes

To kill the application, enter a newline. All connected clients will be disconnected and sockets in use will be closed.