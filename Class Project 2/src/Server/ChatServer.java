package Server;

import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

public class ChatServer {


    // This chat server can accept up to maxClientsCount clients' connections.
    private static final int maxClientsCount = 10;

    // The server socket.
    private ServerSocket serverSocket = null;

    // An array of the clientThread conenctions
    private final ClientThread[] threads = new ClientThread[maxClientsCount];

    /**
     * Creates a server to communicate on the given port number
     * @param portNumber - The port number to communicate through
     */
    public ChatServer(int portNumber){
        setupServer(portNumber);
    }

    /**
     * Open a server socket on the given portNumber.
     * The server will terminate if the socket fails to open
     * @param portNumber - The port number to open the connection too.
     */
    private void setupServer(int portNumber){
        System.out.println("Starting port: "+ portNumber);
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    /**
     * Starts the server running
     */
    public void startServer(){
        runServer();

    }

    /**
     * Create a client socket for each connection and pass it to a new client
     * thread.
     */
    private void runServer(){

        Socket newClientSocket;

        while (true) {
            try {
                newClientSocket = serverSocket.accept();
                int i = 0;
                for (i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == null) {
                        (threads[i] = new ClientThread(newClientSocket, threads)).start();
                        break;
                    }
                }

                if (i == maxClientsCount) {
                    PrintStream os = new PrintStream(newClientSocket.getOutputStream());
                    os.println("Server too busy. Try later.");
                    os.close();
                    newClientSocket.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}
