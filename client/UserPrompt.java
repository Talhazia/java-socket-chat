package client;

import server.Debug;

import java.io.BufferedReader;
import java.io.InputStreamReader;

class UserPrompt extends Thread {
    @Override
    public void run() {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        while(true)
        {
            if(ChatClientFrame.onLogout) return;

            try
            {
                String instruction = bufferedReader.readLine();
                if(instruction.equals("LOGOUT"))
                {
                    ChatClientFrame.sendMessage(instruction);
                    ChatClientFrame.onLogout = true;
                    return;
                } else ChatClientFrame.sendMessage(instruction);
            }
            catch (Exception ex)
            {
                Debug.log("Exception in UserPrompt: " + ex);
            }
        }
    }
}
