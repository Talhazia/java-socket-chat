package client;

import java.util.StringTokenizer;

class ServerResponseThread extends Thread {

    ServerResponseThread(ChatClientFrame chatClientFrame) {

    }

    public void run () {
        while(true) {
            if(ChatClientFrame.onLogout) {
                return;
            }

            String toReadString = ChatClientFrame.read();

            if(checkCommand(toReadString, "LIST"))
            {
                ChatClientFrame.isConnected = true;

                ChatClientFrame.list.clear();

                StringTokenizer stringTokenizer = new StringTokenizer(toReadString.substring(5, toReadString.length()),", ");
                String tempToken = null;

                while(stringTokenizer.hasMoreTokens())
                {
                    tempToken = stringTokenizer.nextToken();

                    ChatClientFrame.list.addElement(replace(tempToken, ";", ""));
                }

                ChatClientFrame.connectionStatus.setText("Logged in as " + ChatClientFrame.username + " on " + ChatClientFrame.serverName);

            } else if(checkCommand(toReadString, "RECIEVE"))
            {
                ChatClientFrame.messageArea.append(toReadString.substring(8, toReadString.length()) + "\n");

            }
            else if(checkCommand(toReadString, "PRIVATERECIEVE"))
            {
                ChatClientFrame.messageArea.append("[PRIVATE]" + toReadString.substring(14, toReadString.length()) + "\n");

            }
        }
    }

    private boolean checkCommand(String message, String command) {
        return message.startsWith(command);
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
