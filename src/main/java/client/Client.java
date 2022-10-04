package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String SERVER_HOST = "localhost";
    private int port;
    private Socket clientSocket;
    private Scanner inMessage;
    private Scanner writeMessage;
    private PrintWriter outMessage;
    private server.Logger logger = server.Logger.getInstance();
    private boolean isCloseConn;
    public Client() {
        try {
            clientSocket = new Socket(SERVER_HOST, setPort());
            inMessage = new Scanner(clientSocket.getInputStream());
            outMessage = new PrintWriter(clientSocket.getOutputStream());
            writeMessage = new Scanner(System.in);
        } catch (IOException e) {
            logger.log(e.getMessage());
        }

        new Thread(() -> {
            while (true) {
                if (inMessage.hasNext()) {
                    String inMes = inMessage.nextLine();
                    if (inMes.equals("/end")) {
                        closeConnection();
                        break;
                    }
                    System.out.println(inMes);
                }
            }
        }).start();
        new Thread(() -> {
            while (true) {
              sendMsg();
              if(isCloseConn){
                  break;
              }
            }
        }).start();
    }

    public void sendMsg() {
        String msg = writeMessage.nextLine();
        if(msg.equals("/exit")){
            isCloseConn=true;
        }
        outMessage.println(msg);
        outMessage.flush();
    }

    private int setPort() {
        try (FileReader reader = new FileReader("settings.txt");
             BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                port = Integer.parseInt(line);
            }
        } catch (IOException e) {
            logger.log(e.getMessage());
        }
        return port;
    }

    public void closeConnection() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            logger.log(e.getMessage());
        }
        inMessage.close();
        outMessage.close();
        writeMessage.close();
    }
}

