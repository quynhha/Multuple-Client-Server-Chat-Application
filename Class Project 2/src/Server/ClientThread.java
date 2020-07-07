package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.*;

public class ClientThread extends Thread{

    /**
     * The name of the user using this client thread
     */
    private String name = null;

    /**
     * The input stream to recieve messages from the client
     */
    private BufferedReader is = null;

    /**
     * The output stream to send messages to this client
     */
    private PrintStream os = null;

    /**
     * The socket that is used to communicate with the client
     */
    private Socket clientSocket = null;

    /**
     * An array containing all the other ClientThreads in this chatroom
     */
    private final ClientThread[] threads;


    /**
     * Constructor. Initializes data in thread
     * @param clientSocket - The socket to communicate through
     * @param threads - An array containing all other threads in the chatroom
     */
    public ClientThread(Socket clientSocket, ClientThread[] threads) {
        this.clientSocket = clientSocket;
        this.threads = threads;
    }

    /**
     * The run method, which is called by the thread throught the start() method
     */
    @Override
    public void run() {

        setup();


        runConversation();

        cleanup();

    }

    /**
     * Initializes input and output streams, gets the client to enter its name
     */
    private void setup() {

        try {
            is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            os = new PrintStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.err.println("Unable to initialize connection in clientThread.");
            e.printStackTrace();
        }

        promptNameEntry();
        welcomeMessage();


    }

    /**
     * Gets the client to enter their desired username and returns the result
     */
    private static Set<String> names = new HashSet<>();
    private void promptNameEntry(){

        try{
            while (true) {
                os.println("Enter your name.");
                name = is.readLine().trim();
                if (name == null) {

                    return;
                }
                synchronized (names) {
                    if (!name.isEmpty() && !names.contains(name)) {
                        names.add(name);
                        break;
                    }else {os.println("This name is taken! Please try other name!");}
                }


                }

        } catch(IOException e){
            e.printStackTrace();
            System.err.println("Error in prompting for name");
        }



    }
    ArrayList<String> users = new ArrayList();
    private void printOnlineUsers() throws NullPointerException{

        try {
            if (threads != null){
            for (int i =0; i< threads.length; i++){
                String user = Objects.requireNonNull(threads[i].getClientName().substring(1));
                users.add(user);}

        }

        }catch (NullPointerException e){
            e.printStackTrace();
        }
        System.out.println("List of online users:" + users);
        broadcastMessage("List of online users:" + users);
        }



    /**
     * Outputs a message welcoming the new client to the chatroom.
     * Also, sends a message to all other clients indicating that a new user has joined the chat
     */
    private void welcomeMessage() throws NullPointerException{
        os.println("Welcome " + name + " to our chat room.\nTo leave enter /quit in a new line.\n " +
                "To send private messages to another clients, hit @ + the name of that client and your message to that client.");
        broadcastMessage("*** A new user " + name + " entered the chat room !!! ***");

        printOnlineUsers();
        System.out.println("New connection request!"+ "New user:"+ name);


    }

    /**
     * Runs the conversation for as long as this thread remains in the chat
     */
    private void runConversation() throws NullPointerException{

        try{

            while (true) {
                String clientMessage = is.readLine();
                if (clientMessage.startsWith("/quit")) {

                    break;
                }
                /* If the message is private sent it to a specific client. */
                if (clientMessage.startsWith("@")) {

                    //Splits up the message by whitespace, but does not split it into more than 2 pieces.
                    String[] words = clientMessage.split("\\s", 2);

                    // Check to make sure the message is not empty
                    if (words.length > 1 && words[1] != null && !words[1].isEmpty()) {
                        String recipient = words[0];
                        String msg = "(private msg to <" + recipient + ">)<" + name + "> " + words[1].trim();
                        System.out.println(name+ " send a private message to " + recipient);
                        sendMessageTo(recipient, msg);
                    }
                } else {
                    /* The message is public, broadcast it to all clients. */
                    broadcastMessage("<" + name + "> " + clientMessage);
                    System.out.println(name+ " broadcast a message.");
                }
            }//End of while

        } catch(IOException e){
            e.printStackTrace();
            System.err.println("Error running clientThread conversation");
        } catch (NullPointerException e){
            e.printStackTrace();
            System.err.println("");
        }
    }

    /**
     * Clean up. Set the current thread variable to null so that a new client
     * could be accepted by the server.
     */
    private synchronized void cleanup(){
        try{

            goodbyeMessage();
            for (int i = 0; i < threads.length; i++) {
                if (threads[i] == this) {
                    threads[i] = null;
                }
            }

            /*
             * Close the output stream, close the input stream, close the socket.
             */
            is.close();
            os.close();
            clientSocket.close();

        } catch(IOException e){
            e.printStackTrace();
            System.err.println("Error closing clientThread");
        }
    }

    /**
     * Sends a message to all other clients indicating that this user is leaving the chatroom.
     * Also, outputs a goodbye message to this client
     */
    private void goodbyeMessage(){

        broadcastMessage("*** The user " + name + " is leaving the chat room !!! ***");
        os.println("*** Bye " + name + " ***");
        users.remove(name);
        broadcastMessage("List of online users:" + users);
        System.out.println(name + " disconnected!" );
        System.out.println("List of online users:" + users);

    }

    /**
     * Sends a message to all clients, including this one
     * @param msg - The msg to be sent
     */
    private synchronized void broadcastMessage(String msg){
        for(int i = 0; i < threads.length; i++){
            if(threads[i] != null
                    && threads[i].getClientName() != null){

                threads[i].os.println(msg);
            }
        }
    }

    /**
     * Send a message to a specific client, as well as this client.
     * If the specified client is not found, the sending client will be notified
     * @param clientName - The name of the client to send a message to
     * @param msg - The message to send
     */
    private synchronized void sendMessageTo(String clientName, String msg){
        int i;
        for(i = 0; i < threads.length; i++){
            if	(threads[i] != null && threads[i].getClientName() != null
                    && threads[i].getClientName().equals(clientName)){

                threads[i].os.println(msg);
                this.os.println(msg);
                break;
            }
        }
        if(i == threads.length){
            os.println("There are no clients with the name <" + clientName + "> currently in this chat group.");
            os.println("Your message <" + msg + "> could not be sent");
        }
    }

    /**
     * Gets the name of the client
     * @return This users name with an '@' character prepended. If a name has not been set, null is returned
     */
    private String getClientName(){
        if(name != null){
            return '@' + name;
        }
        return null;
    }
}