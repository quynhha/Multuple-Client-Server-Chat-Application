package Client;
import java.io.IOException;

public class ChatModel {
    private String serverIP;
    private int port;
    private ChatClient access;

    private ChatView frame;

    /**
     * @param serverIP - The IP of the server to connect to
     * @param port - The port number of the server to communicate to
     */
    public ChatModel(String serverIP, int port){
        this.serverIP = serverIP;
        this.port = port;
        this.access = new ChatClient();

    }

    public void setupClient(){
        initialize();

        try {
            access.InitSocket(serverIP, port);
        } catch (IOException ex) {
            System.out.println("Cannot connect to " + serverIP + ":" + port);
            ex.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Initializes the GUI
     */
    private void initialize(){
        frame = new ChatView(access);
        frame.setupClientView(serverIP, port);
    }

}
