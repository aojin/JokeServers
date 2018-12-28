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

My Admin class does one thing: switch modes or quit.
For some reason, other FileWriter's were giving me trouble, but the output of Bytes worked just fine.
The only annoyance was having to figure out how to use line.separator to append to new line in .txt file.
Works as expected in concert with the AdminLooper and Handler. The handler lets you know which mode you're currently on.
Handler then Admin then passes the selection to handler which deals with the changeMode() method on the server.

*/

import java.io.*;
import java.net.Socket;

public class AsynchJokeClientAdmin {

    public static void main(String[] args) {
        // same initial setup as AsynchJokeClient
        String serverName;
        if (args.length < 1)
            serverName = "localhost"; // so on terminal start call, you can provide one non local server name
        else serverName = args[0];

        System.out.println("Alex Jin's JokeServer Admin, 1.8.\n");
        System.out.println("Using server: " + serverName + ", Port: 5050");

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        try {
            String input;
            do {
                System.out.print
                        ("Press Enter to continue, type (quit) to end: \n");
                System.out.flush();
                input = in.readLine ();
                if (input.indexOf("quit") < 0)
                    switchModes(serverName);
            } while (input.indexOf("quit") < 0); // there can't be quit in any of the string
            System.out.println("Cancelled by user request.");
        } catch (IOException ex) {ex.printStackTrace();}
    }

    static void switchModes(String serverName){

        // we'll need I/O and a place to store response...
        Socket sock;
        BufferedReader fromServer;
        PrintStream toServer;
        String textFromServer;

        // same procedure as AsynchJokeClient
        try {
            sock = new Socket(serverName, 5050); // set socket to port address of AdminLooper
            fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            toServer = new PrintStream(sock.getOutputStream());

            // adding admin file logging IO
            FileOutputStream adminFileOutput = new FileOutputStream("JokeLog.txt", true);

            // we want an input stream dedicated to our console inputs from the admin
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            // report the current jokeserver mode status which the AdminHandler out.println'd
            textFromServer = fromServer.readLine();
            System.out.println(textFromServer);
            System.out.println("\nPlease type 'joke' for JOKEMODE or 'proverb' for PROVERBMODE\n");
            System.out.println("Your selection, Admin?\n");
            String adminSelection = in.readLine();

            // Log file output
            String logOutput = "Admin selected " + adminSelection + " Mode." + System.getProperty("line.separator");
            byte[] logToBytes = logOutput.getBytes();
            adminFileOutput.write(logToBytes);
            adminFileOutput.close();


            System.out.println("You selected: " + adminSelection);
            toServer.println(adminSelection);

        } catch (IOException ioe) {
            System.out.println("IO Exception on Admin, make sure AdminLooper is running...");
        }

    }
}
