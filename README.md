# MopedNetwork

See src/mopedClient/ for the client code. This handles sending and receiving data between mopeds.
To start the client simply run the ClientFactory.createAndRunClient() method. The first time the client is ran it will create a config file where the name of the moped, the name of the moped following it, the server ip, and the port can be modified.

The way it currently works is that the client sends the name of the moped following it, aswell as its own name to the server, who then returns the IP of the moped following the client to the client so that it can start sending its data to that moped. This way only the IP of the server needs to be known, and since it is assumed that the order of cars is known, putting the name of the car behind oneself in the config file is less of a hassle than looking up their ip.

Current known issues: 
- The client terminates if the connection is lost or the server isn't running when the client is started.
- The client sends messages to the server and the car behind them simultaneously, causing unnecessary load on the server since it should only need to be pinged every second or so.
- If multiple cars claim the same car is following them they will both start sending data to that car, causing it to get data from two different cars simultaneously. 

Potentially planned changes by priority:
- A simple public way of checking if the client has lost connection.
- Let the client try to reconnect instead of stopping upon losing connection.
- Make the client know which car they should be listening to instead of which car they should send data to. This prevents multiple cars from sending data to the same one.


See src/mopedp2pserver/ for the server code. This handles connecting the different mopeds together.

All other files are either for development or unused.
