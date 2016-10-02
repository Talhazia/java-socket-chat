package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ChatServer {
    static Vector<ClientThread> clientList;

    public static void main(String args[]) {
        clientList = new Vector<>();

        Socket clientSocket = null;
        ServerSocket serverSocket = null;

        try
        {
            serverSocket = new ServerSocket(9000);
            Debug.log("New Socket Server Initialized");
        } catch(IOException ex) {
            System.out.println("IOException: " + ex);
        }

        while(true) {
            try
            {
                Debug.log("Accepting client requests");
                if (serverSocket != null) {
                    clientSocket = serverSocket.accept();
                }
                ClientThread clientThread = new ClientThread(clientSocket);
                clientList.add(clientThread);
                clientThread.start();
            } catch (IOException ex) {
                System.out.println("IOException: " + ex);
            }
        }
    }
}
