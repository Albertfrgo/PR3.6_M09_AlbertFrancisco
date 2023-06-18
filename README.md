# Getting Started
This project requires the server running and two instances of the client. A simple test can be done with the server and the two clients all in the same device. First an explanation of how to run all the game in the device.

# Running the server

In the folder NodeJS, the file app.js contains the code of the server. The files for executing node are already in the repo, but they can be installed with npm install. Once node is installed, to run the server we execute npm run dev, the server will then be listening

# Running the client

The client is basically a JavaFX application in the folder JavaFX. To run the app there's the run.bat file tor un in windows and a run.sh to run it in Linux. The desktop is made to connect to a remote server, if we want to connect it locally, in the file main.java, in the line 21, we have to modify the parameters of the connection:     
    public static int port = 3000;
    public static String protocol = "https";
    public static String host = "localhost";
    public static String protocolWS = "ws";

# Running the game online
To run the game online, the folder NodeJS must be deployed in a remote hosting. 
In the file main.java, in the line 21, we have to modify the parameters of the connection:
    public static int port = 443;
    public static String protocol = "https";
    public static String host = "hostingName";
    public static String protocolWS = "ws";                                                        
