import javax.swing.*;
import Server.ChatServer;
import Client.ChatModel;

public class ChatProgram {

    public static void main(String args[]){

        Object[] selectionValues = { "Server","Client"};
        String initialSection = "Server";

        Object selection = JOptionPane.showInputDialog(null, "Login as : ",
                "Chat Program", JOptionPane.QUESTION_MESSAGE, null, selectionValues, initialSection);
        if(selection.equals("Server")){
            startAsServer();
        }else if(selection.equals("Client")){
            startAsClient();
        }
        else System.err.println("Cancel connection!");
    }

    private static void startAsServer(){
        ChatServer server;
        int port = -1;
        port = Integer.parseInt(JOptionPane.showInputDialog("Enter the Server port number"));

        while(port < 1024 || port > 65535){
            port = Integer.parseInt(JOptionPane.showInputDialog(port + " is an invalid port number. Enter the Server port number"));
        }

        server = new ChatServer(port);

        server.startServer();

    }

    private static void startAsClient(){
        String serverIP = JOptionPane.showInputDialog("Enter the Server ip address"); // if no specific ip address, enter localhost
        int serverPort = Integer.parseInt(JOptionPane.showInputDialog("Enter the Server port number"));

        ChatModel client = new ChatModel(serverIP, serverPort);
        client.setupClient();
    }

}
