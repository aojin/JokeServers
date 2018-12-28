

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;


public class JokeServer {

    static ServerMode mode = ServerMode.JOKEMODE; // Default setting for Server mode.

    public enum ServerMode {
        JOKEMODE,
        PROVERBMODE
    }

//    Map<UUID, put inside own class {name: "John Doe", jokes[t, t, f, t], proverbs[f,t,f,t], processing methods}>
    static HashMap<UUID, UserState> hashMap = new HashMap<>(); // the data structure to persist which jokes or proverbs I've seen.

    private static boolean controlSwitch = true;

    // ------ single instance of my jokes and proverbs lives in Server class memory.
    private static String[] Jokes = {
            "\nJA. Why did the chicken cross the road?\n...\nTo get to the other side.\n",
            "\nJB. I'm terrified of elevators,\n...\nI'm going to have to start taking steps to avoid them.\n",
            "\nJC. What do you call a pony with a sore throat?\n...\nA little horse.\n",
            "\nJD. I'll call you later.\n...\nDon't call me later, call me Dad.\n"
    };

    private static String[] Proverbs = {
            "PA. There's no such thing as a free lunch\n",
            "PB. Beggars can't be choosers.\n",
            "PC. There is no shame in not knowing.\nThe shame lies in not finding out.\n",
            "PD. Some men go through a forest and see no firewood.\n"
    };

    static void setMode(String newMode) {
        System.out.println("Admin user selected " + newMode + " mode.");

        switch (newMode.toLowerCase()) {
            case "proverb":
                JokeServer.mode = ServerMode.PROVERBMODE;
                break;
            case "joke":
                JokeServer.mode = ServerMode.JOKEMODE;
            default:
                break;
        }
    }

    // methods to retrieve Joke or Proverb from the String Arrays stored in Server.

    static String getFirstJoke() {
        return Jokes[0];
    }

    static String getFirstByMode() {
        if (mode == ServerMode.JOKEMODE){
            return (Jokes[0]);
        }
        return (Proverbs[0]);
    }

    static String getRandomByMode(UserState userState) {
        if (JokeServer.mode.equals(ServerMode.JOKEMODE)){
            return Jokes[userState.getRandomIndex()];
        }
        return Proverbs[userState.getRandomIndex()];
    }

    // The complete randomized method based on Mode, returns next random unseen:

    static String getRandomUnseenByMode(UserState userState) {
        if (JokeServer.mode.equals(ServerMode.JOKEMODE)){
            return Jokes[userState.getAnUnseenJokeIndex()];
        }
        return Proverbs[userState.getAnUnseenProverbIndex()];
    }


    public static void main(String args[]) throws IOException {
        int q_len = 100; // OS queue length
        int port = 4545;
        Socket sock;


        // Prior to opening up socket for clients, we generate the AdminLooper
        AdminLooper AL = new AdminLooper(); // create a separate mode thread
        Thread t = new Thread(AL);
        t.start();

        ServerSocket servsock = new ServerSocket(port, q_len);

        System.out.println
                ("Alex Jin's JokeServer server 1.8 starting up, listening at port " + port + "\n");

        while (controlSwitch) {

            // while the joke server is running, it enters an infinite loop listening for connection request from up to 6 open clients. The 7th is rejected.
            // this is where the blocking occurs until connected
            sock = servsock.accept(); // should go until socket close
            new Worker(sock).start(); // constructing new worker to handle new socket request.
        }
    } // end of main

}

// ---------------------------------------------------------------------------------------------------------------------

class Worker extends Thread {       // Java utils has multi-threading library. Also Worker is a static class inside Server
    private Socket sock; // From java.net. Each service worker gets its own socket

    Worker (Socket s) {
        this.sock = s;
    }   // The worker constructor takes a socket to work with as a parameter

    public void run() { // when a new worker is instantiated with its own socket and then run, we need to get in and out of the stream

        try {
            // -------- setting up client IO

            BufferedReader inFromClient = new BufferedReader
                    (new InputStreamReader(sock.getInputStream()));
            // declaring I/O objects, initially null objects
            PrintStream outToClient = new PrintStream(sock.getOutputStream());

            // -------- setting up log file for client

            FileOutputStream clientFileOutput = new FileOutputStream("JokeLog.txt", true);

            // checking to make sure server receives UUID & name from client on first pass.
            String clientIDString = inFromClient.readLine(); // (1)
            UUID clientID = UUID.fromString(clientIDString);
            System.out.println("Received UUID: " + clientID + " from client.");

            String userName = inFromClient.readLine();
            System.out.println("Received username: " + userName + " from client");

            // check if uuid exists in map

            UserState clientState;

            if (!JokeServer.hashMap.containsKey(clientID)){
                // if not then...
                // -------- Build new state from UUID <<<
                clientState = new UserState(userName, clientID);
            // -------- Add state to hashmap <<<
                JokeServer.hashMap.put(clientID, clientState);
                System.out.println("We created new map entry for UUID:UserState");
            } else {
                // else retrieve the state for processing
                clientState = getUserState(clientID);
                System.out.println("User state for "+ clientState.name + " exists. Moving on...");
            }

            // -------- Using retrieved client data in Server Methods...
//            printRandomByMode(outToClient, clientFileOutput, clientID);
            printNextByUUID(clientState, outToClient, clientFileOutput);
            saveUpdatedStateToHashMap(clientID, clientState);

            sock.close();
        } catch (IOException ioe) { ioe.printStackTrace(); } // this catch corresponds to the object instantiation try that came first.
    }

    // The first three methods were me trying to follow the suggested development order.

    private static void printFirstJoke (PrintStream out) {
        try {
            out.println(JokeServer.getFirstJoke());
        } catch (Exception e) {
            out.println("Failed in attempt to find first Joke.");
        }
    }

    private static void printFirstByMode (PrintStream out, FileOutputStream log) {
        try {

            System.out.println("Client Request Received!");
            out.println(JokeServer.getFirstByMode());

            // Log to Client Log
            String logOutput = "\nClient received joke!" + System.getProperty("line.separator");
            byte[] logToBytes = logOutput.getBytes();
            log.write(logToBytes);

        } catch (Exception e) {
            out.println("Failed in attempt to find first Joke.");
        }
    }

    private static void printRandomByMode(PrintStream out) {

        System.out.println("Client Request For Random Joke Received!");
        UserState clientState = new UserState("Alex", UUID.randomUUID());
        // send random joke to stream for pick up.
        out.println(clientState.getName() + ": " + JokeServer.getRandomByMode(clientState));
    }

    // nextByUUID is my complete random return method which calls to server then manipulates the passed state.

    private void printNextByUUID(UserState clientState, PrintStream out, FileOutputStream log ){


        System.out.println("Client request for next random unseen received!");
        String randomUnseenJoke = JokeServer.getRandomUnseenByMode(clientState);
        String clientName = clientState.getName();
        out.println( clientName + ": " + randomUnseenJoke);
        logClientRequest(clientName, randomUnseenJoke, log);
    }

    // at the end of each request run() we should save the state to the server hash map
    private void saveUpdatedStateToHashMap(UUID uuid, UserState state) {
        System.out.println("Saving " + state.name + "'s user state to UUID: " + uuid);
        JokeServer.hashMap.put(uuid, state);
    }

    private void logClientRequest(String clientName, String randomUnseenJoke, FileOutputStream fileIO){
        try {
            // log file output
            String logOutput = clientName + ": " + randomUnseenJoke + System.getProperty("line.separator");
            byte[] logtoBytes = logOutput.getBytes();
            fileIO.write(logtoBytes);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private UserState getUserState(UUID uuid) {
        return JokeServer.hashMap.get(uuid);
    }

}

// ---------------------------------------------------------------------------------------------------------------------

class AdminLooper implements Runnable {
    private static boolean adminControlSwitch = true;

    public void run() { // override run in the admin thread.
        System.out.println("In the admin looper thread...");

        int q_len = 6; // max number of requests to queue
        int port = 5050; // admin clients listen in at a different port
        Socket sock;

        try {
            ServerSocket servsock = new ServerSocket(port, q_len);
            while (adminControlSwitch) {
                // wait for the next admin client connection:
                sock = servsock.accept();
                new AdminHandler(sock).start();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}

// ---------------------------------------------------------------------------------------------------------------------

/* AdminHandler is generated on the AdminLooper serverSocket and handles taking admin client requests and inputs to switch the JokeServer's mode through setMode()
* */


class AdminHandler extends Thread {
    private Socket adminSocket;
    AdminHandler(Socket sock) {
        this.adminSocket = sock;
    }

    public void run()
    {
        PrintStream out;
        BufferedReader in;
        try{
            in = new BufferedReader(new InputStreamReader(adminSocket.getInputStream()));
            out = new PrintStream(adminSocket.getOutputStream());

            System.out.println("Admin Request Received.");

            out.println("AdminHandler: Current Server Mode = " + JokeServer.mode + "\n");

            String modeSelection = in.readLine();

            JokeServer.setMode(modeSelection);

            adminSocket.close();

        } catch (IOException ioe) {
            System.out.println("IO error on AdminHandler");
            ioe.printStackTrace();
        }
    }
}


// ---------------------------------------------------------------------------------------------------------------------

// Initially had this class in it's own file, but wanted to make sure to conform to the submission standards.

class UserState {
    UUID uuid;
    String name;
    boolean[] jokes = {false, false, false, false};
    boolean[] proverbs = {false, false, false, false};

    int jokeLastIndex;
    int proverbLastIndex;

    public UserState(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public void markJokeAsRead(int readIndex){
        jokes[readIndex] = true;
    }

    public void markProverbAsRead(int readIndex) {
        proverbs[readIndex] = true;
    }

    public int getRandomIndex() {
        Random rand = new Random();
        return rand.nextInt(jokes.length);
    }

    public int getAnUnseenJokeIndex() {

        if (checkJokesOrReset()){
            resetToUnseen(jokes);
        }

        // Select a random index to test
        int index = getRandomIndex();

        while (jokes[index] == true) {
            System.out.println("Joke at index "+ index +" has already been seen.");
            index = getRandomIndex();
        }
        System.out.println("Marking joke at index " + index + " as seen.");
        markJokeAsRead(index);
        jokeLastIndex = index;
        return  index;

    }

    public int getAnUnseenProverbIndex() {

        if (checkProverbsOrReset()){
            resetToUnseen(proverbs);
        }

        // Select a random index to test
        int index = getRandomIndex();

        while (proverbs[index] == true) {
            System.out.println("Proverb at index "+ index +" has already been seen.");
            index = getRandomIndex();
        }
        System.out.println("Marking joke at index " + index + " as seen.");
        markProverbAsRead(index);
        proverbLastIndex = index;
        return  index;

    }

    public boolean checkJokesOrReset(){

        for (int i = 0; i < jokes.length; i++){
            // if we hit an unseen, we return false immediately
            if (jokes[i] == false){
                return false; // false, we didn't need to reset
            }
        }
        return true;
    }

    public boolean  checkProverbsOrReset(){

        for (int i = 0; i < proverbs.length; i++){
            // if we hit an unseen, we return false immediately
            if (proverbs[i] == false){
                return false; // false, we didn't need to reset
            }
        }
        return true;
    }

    public void resetToUnseen(boolean[] array) {

        for(int i = 0; i < array.length; i++){
            array[i] = false;
        }

        System.out.println("Resetting all to unseen...");

    }

    public String getName() {
        return name;
    }

}
