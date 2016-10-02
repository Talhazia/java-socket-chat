package client;

import server.Debug;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient
{
    public static void main(String args[]) {
        ChatClientFrame chatClientFrame = new ChatClientFrame();
        chatClientFrame.run();
    }
}

class ChatClientFrame extends JFrame {

    private static BufferedReader inStream;
    static Boolean isConnected = false;
    static Boolean onLogout = false;
    ServerResponseThread serverInput;
    static PrintWriter outStream;
    static DefaultListModel list;
    UserPrompt userPrompt;
    Socket clientSocket;

    JButton logoutButton;
    JButton messageSendButton;
    JTextArea messageTextInput;
    static JTextArea messageArea;
    static JLabel connectionStatus;

    static String serverName;
    static String username;

    JPanel contentPane;

    static JList usernameList;
    JButton connectToServerButton;

    void run() {
        setTitle("A messaging distributed application");

        isConnected = false;

        list = new DefaultListModel();
        list.addElement("Not Connected");

        make();
        pack();
        setVisible(true);
    }

    private void make()
    {
        setBounds(100, 100, 600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JPanel top_panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top_panel.setBackground(Color.LIGHT_GRAY);
        top_panel.setPreferredSize(new Dimension(600, 40));
        contentPane.add(top_panel, BorderLayout.NORTH);

        connectionStatus = new JLabel("Press Connect To Server to get started");
        connectionStatus.setPreferredSize(new Dimension(300, 20));
        top_panel.add(connectionStatus);

        connectToServerButton = new JButton("Connect To Server");
        top_panel.add(connectToServerButton);

        logoutButton = new JButton("Logout");
        logoutButton.setEnabled(false);
        top_panel.add(logoutButton);

        JPanel bottom_panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom_panel.setBackground(Color.LIGHT_GRAY);
        bottom_panel.setPreferredSize(new Dimension(600, 100));
        contentPane.add(bottom_panel, BorderLayout.SOUTH);

        messageTextInput = new JTextArea();
        messageTextInput.setEnabled(false);
        messageTextInput.setAlignmentX(Component.LEFT_ALIGNMENT);
        messageTextInput.setPreferredSize(new Dimension(400, 90));
        bottom_panel.add(messageTextInput);

        messageSendButton =new JButton("Send Message");
        messageSendButton.setEnabled(false);
        messageSendButton.setPreferredSize(new Dimension(175, 90));
        bottom_panel.add(messageSendButton);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setPreferredSize(new Dimension(600, 400));
        contentPane.add(splitPane, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane();
        splitPane.setLeftComponent(scrollPane);

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setColumns(37);
        messageArea.setRows(5);
        scrollPane.setViewportView(messageArea);

        usernameList = new JList(list);
        splitPane.setRightComponent(usernameList);

        handleEvents();
    }

    private void handleEvents()
    {
        usernameList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }
        });

        connectToServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToServer();
            }
        });

        messageSendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!messageTextInput.getText().equals("")) {
                    if(isConnected) {
                        sendMessage("POST " + replace(messageTextInput.getText(), "\n", " "));
                        messageTextInput.setText(null);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Please type a message first");
                }
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(isConnected)
                {
                    if (JOptionPane.showConfirmDialog(null, "Are you sure?", "WARNING",
                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        outStream.println("LOGOUT");
                        ChatClientFrame.onLogout = true;
                        System.exit(0);
                    }
                }
            }
        });

        usernameList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(isConnected && (!usernameList.getSelectedValue().equals(username))) {
                    String privateMessage = JOptionPane.showInputDialog(null, "Message to send: ");
                    if(privateMessage != null) {
                        outStream.println("PRIVATECHAT " + privateMessage + ", " + usernameList.getSelectedValue());
                    }
                }
            }
        });
    }

    private void connectToServer() {
        onLogout = false;

        serverInput = new ServerResponseThread(this);
        userPrompt = new UserPrompt();

        clientSocket = null;
        outStream = null;
        inStream = null;

        boolean isError = false;

        serverName = JOptionPane.showInputDialog("Server Name: ", "localhost");

        try
        {
            clientSocket = new Socket(serverName, 9000);

            outStream = new PrintWriter(clientSocket.getOutputStream(), true);
            inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            userPrompt.start();
            serverInput.start();
        }
        catch(UnknownHostException uex)
        {
            JOptionPane.showMessageDialog(this, "Failed to connect to the server.", "ERROR", JOptionPane.ERROR_MESSAGE);
            isError = true;
        }
        catch(IOException ex)
        {
            Debug.log("IOException: " + ex);
        }

        if(!isError)
        {
            username = null;
            username = JOptionPane.showInputDialog(null, "Username: ");

            while(username.contains(";"))
            {
                username = JOptionPane.showInputDialog(null, "Username: ");
            }

            sendMessage("LOGIN: " + username);

            if(username != null)
            {
                connectToServerButton.setEnabled(false);
                logoutButton.setEnabled(true);

                messageTextInput.setEnabled(true);
                messageSendButton.setEnabled(true);
            }
        }
    }

    static String read() {
        String text = null;

        try
        {
            text = inStream.readLine();
        }
        catch (Exception e)
        {
            Debug.log(e + "");
        }

        return text;
    }

    static void sendMessage(String message)
    {
        outStream.println(message);
    }

    private String replace(String tempToken, String s1, String s2) {
        int s = 0;
        int e = 0;
        StringBuffer result = new StringBuffer();
        while ((e = tempToken.indexOf(s1, s)) >= 0)
        {
            result.append(tempToken.substring(s, e));
            result.append(s2);
            s = e + s1.length();
        }
        result.append(tempToken.substring(s));
        return result.toString();
    }

}