/*--------------------------------------------------------

1. Alex Jin / 09/23/18:

2. Java version used, if not the official version for the class:

build 10.0.1+10

3. Precise command-line compilation examples / instructions:

> javac AsynchJokeServer.java <--- holds my worker/handler classes for Server & Admin
> javac AsynchJokeClient.java
> javac AsynchJokeClientAdmin.java
> javac UserState.java <--- this is the class that holds my boolean values for seen/unseen and processes indexes.


4. Precise examples / instructions to run this program:

In separate shell windows:

> java JokeServer
> java AsynchJokeClient
> java AsynchJokeClientAdmin

All acceptable commands are displayed on the various consoles.

Was not able to test across multiple machines, however I did not hardcode a serverName into any main files, so it should run by replacing localhost as expected:

> java AsynchJokeClient 140.192.1.22
> java AsynchJokeClientAdmin 140.192.1.22

5. List of files needed for running the program.

 a. checklist.html
 b. AsynchJokeServer.java
 c. AsynchJokeClient.java
 d. AsynchJokeClientAdmin.java
 e. UserState.java

5. Notes:

May be a bug, or may just be how streams manage buffer, but sometimes when you send a request or two in succession, and then wait for a few seconds, the next request is processed but doesn't print to client console until you press enter again. At which time two jokes or proverbs are spit out. The Server recognizes both requests though.

Client class is responsible for generating a unique UUID as detailed in Joke Server State Concepts' implementation ONE.

The client gets the server info, creates a console input stream and then prompts the user for their user name which will be passed to server worker for use in printing. User names themselves are
not unique in this case. There could be two "alex" users that are identified uniquely by their UUID.

My running server instance can handle as many clients as I threw at it, interleaving jokes and proverbs between different users correctly.

However, since I didn't set up individual server modes per client, if you change the mode by admin input, the mode is changed for all connected clients.

This is a pretty reusable client since it holds no information except for UUID generation and passing.

The giveMeSomething() method simply passes those unique identifiers to the stream and awaits up to 10 lines of response from the input stream to client.

*/

import java.io.*;
import java.net.*; // same imports as Server. They share the same IDL
import java.util.UUID;

public class AsynchJokeClient {
    public static void main(String args[]) {
        // ------- Declare globals & set IP domain -------
        String serverName;
        UUID clientID;
        String userName = "";


        if (args.length < 1)
            serverName = "localhost"; // so on terminal start call, you can provide one non local server name
        else serverName = args[0];

        System.out.println("Alex Jin's JokeServer Client, 1.8.\n");
        System.out.println("Using server: " + serverName + ", Port: 4545");

        // ------- Set IO for Client console input -------

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); // Buffering the system's default console input read through stream

        // ------- Generate UUID for this particular client's thread -------

        clientID = generateUUID(); // when the client is created, first thing is to generate a new UUID. This happens once upon client startup...

        System.out.println("\nClient generated new UUID: " + clientID);

        // ------- Get Username to pass to server -------

        System.out.println("\nPlease enter your username for this session: ");

        try {
            do {
                userName = in.readLine();
            } while (userName.length() < 2);
            System.out.println("\nYou selected the user name: " + userName);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        try {

            // ------- Start Input do-while loop to process or terminate thread.

            // ------- Prompt user input -------

            System.out.println("\nPress (Enter) to get something! OR type (Quit) to terminate Client");

            String userInput;

            do {
                userInput = in.readLine();
                // request to server every time user input is not quit.
                // giveMe simply passes userName & UUID on every request and simply
                // regurgitates all of the server's return up to 10 lines (arbitrary)
                if (!userInput.contains("Quit") && !userInput.contains("quit")) {
                    giveMeSomething(clientID, userName, serverName);
                }
            } while (!userInput.contains("Quit") && !userInput.contains("quit")); // there can't be quit in any of the string

            System.out.println("Cancelled by user request.");

        } catch (IOException ex) {
            System.out.println("There is no server to connect to on the chosen port.");
            ex.printStackTrace();
        }
    }

    private static UUID generateUUID(){
        return UUID.randomUUID();
    }

    private static void giveMeSomething(UUID clientID, String userName, String serverName) {
        String textFromServer;
        Socket sock;
        BufferedReader fromServer;
        PrintStream toServer;
        try {
            // ------- Set up IO streams for client -------
            // ------- creates our new Socket connection: this is where we declare a port #
            sock = new Socket(serverName, 4545);

            // ------- instantiate the I/O stream objects for that given socket

            toServer =
                    new PrintStream(sock.getOutputStream()); //OUT

            fromServer =
                    new BufferedReader(new InputStreamReader(sock.getInputStream())); // IN

            // ------- Send UserName & generated UUID to Server

            String UUIDtoString = clientID.toString();

            toServer.println(UUIDtoString); // Send the UUID with every request! (1)
            toServer.println(userName); // (2)

            for (int i = 1; i <= 10; i++){
                // take up to 10 lines from server buffer.
                textFromServer = fromServer.readLine();
                if (textFromServer != null)
                    System.out.println(textFromServer); // prints the returned response
            }

            sock.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }


}
