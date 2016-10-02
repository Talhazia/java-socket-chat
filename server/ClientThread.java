package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ClientThread extends Thread {

    private Boolean isConnected;
    private Socket clientSocket;
    private PrintWriter outStream;
    private BufferedReader inStream;
    private String username;

    ClientThread (Socket socket) {
        super("Client Thread");

        isConnected = false;
        username = "";

        clientSocket = socket;

        try
        {
            outStream = new PrintWriter(clientSocket.getOutputStream(), true);
            inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            Debug.log("New Client Thread Initialized");
        }
        catch(Exception ex) {
            System.out.println("Exception: " + ex);
        }
    }

    public void run()
    {
        Debug.log("Listening for client stream ...");
        listen();
    }

    private void listen()
    {

        try
        {
            while(true)
            {
                String message = inStream.readLine();

                if(message.startsWith("LOGIN"))
                {
                    login(message);
                }
                else if(message.startsWith("LOGOUT"))
                {
                    if(isConnected)
                    {
                        isConnected = false;
                        int index = ChatServer.clientList.indexOf(this);
                        ChatServer.clientList.remove(this);

                        for(int j = 0; j < ChatServer.clientList.size(); j++)
                        {
                            ClientThread thread = ChatServer.clientList.get(j);
                            if(thread.isConnected)
                            {
                                thread.updateList();
                            }
                        }

                        outStream.println("OK");
                        outStream.close();
                        clientSocket.close();
                        return;
                    } else
                    {
                        sendMessage("UnAuthenticated");
                    }
                } else if(message.startsWith("POST "))
                {
                    for(int index = 0; index < ChatServer.clientList.size(); index++)
                    {
                        ClientThread thread = ChatServer.clientList.get(index);
                        if(thread.isConnected)
                        {
                            thread.sendMessage("RECIEVE "+ username + ": " + message.substring(5, message.length()));
                        }
                    }

                } else if(message.startsWith("PRIVATECHAT "))
                {
                    String privateMessage = null;
                    String reciever = null;

                    Pattern p = Pattern.compile("(PRIVATECHAT) ([A-Za-z0-9\\s]+), ([A-Za-z0-9]+)");
                    Matcher m = p.matcher(message);
                    boolean b = m.matches();

                    if(b) {
                        privateMessage = m.group(2);
                        reciever = m.group(3);
                    }

                    boolean isSuccess = false;

                    for(int index = 0; index < ChatServer.clientList.size(); index++)
                    {
                        ClientThread thread = ChatServer.clientList.get(index);
                        if(thread.isConnected)
                        {
                            if(thread.username.equals(reciever))
                            {
                                thread.sendMessage("PRIVATERECIEVE " + reciever + ": " + privateMessage);
                                isSuccess = true;
                                break;
                            }
                        }
                    }
                } else
                {
                    sendMessage(message);
                }
            }
        }
        catch(SocketException ex)
        {
            if(isConnected)
            {
                try
                {
                    isConnected = false;
                    int index = ChatServer.clientList.indexOf(this);
                    ChatServer.clientList.remove(this);

                    updateList();

                    outStream.println("OK");
                    outStream.close();
                    clientSocket.close();
                }
                catch(Exception e)
                {
                }
            } else
            {
                sendMessage("UnAuthenticated");
            }
        }
        catch(Exception ex)
        {
            Debug.log("Exception: " + ex);
        }
    }

    private boolean login(String message)
    {
        if(isConnected)
        {
            outStream.println("Already Connected");
            return true;
        }

        boolean doExists = false;

        for(int index = 0; index < ChatServer.clientList.size(); index++)
        {
            if(ChatServer.clientList.get(index) != null)
            {
                ClientThread clientThread = ChatServer.clientList.get(index);
                if((clientThread.username).equals(message.substring(7, message.length())))
                {
                    doExists = true;
                    break;
                }
            }
        }

        if(!doExists)
         {
            isConnected = true;
            username = message.substring(7, message.length());

             Debug.log("[LOGIN] by [" + username + "]");

            updateList();
        }

        return true;
    }

    private void updateList()
    {
        String list = "";

        if(ChatServer.clientList.size() == 0)
            return;

        for(int index = 0; index < ChatServer.clientList.size(); index++)
        {
            ClientThread clientThread = ChatServer.clientList.get(index);
            if(ChatServer.clientList.get(index) != null)
            {
                if(isConnected)
                {
                    list = clientThread.username + ", " + list;
                }
            }
        }

        list = "LIST " + list.substring(0,list.length() -1) +";";

        for(int index = 0; index < ChatServer.clientList.size(); index++)
        {
            ClientThread clientThread = ChatServer.clientList.get(index);
            if(clientThread.isConnected)
                clientThread.sendMessage(list);
        }
    }

    private void sendMessage(String message)
    {
       outStream.println(message);
    }
}
