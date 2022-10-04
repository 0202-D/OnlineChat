package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Instant;
import java.util.Scanner;

public class ClientHandler implements Runnable {

    private Server server;
    private PrintWriter outMessage;
    private Scanner inMessage;
    private String nick = "administrator";
    private Scanner sc = new Scanner(System.in);
    private Socket socket;
    private Logger logger = Logger.getInstance();
    public String getNick() {
        return nick;
    }

    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;
            this.outMessage = new PrintWriter(socket.getOutputStream());
            this.inMessage = new Scanner(socket.getInputStream());
        } catch (IOException e) {
            logger.log(e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                try {
                    authentication();
                } catch (Exception e) {
                    logger.log(e.getMessage());
                }
                break;
            }

            while (true) {
                if (inMessage.hasNext()) {
                    String clientMessage = inMessage.nextLine();
                    if (clientMessage.equalsIgnoreCase("/exit")) {
                        server.sendMessageToAllClients(nick + " out of chat");
                        server.sendMessageToCloseConnections(this,"/end");
                        logger.log(nick + " out of chat "+Instant.now().toString());
                        break;
                    }
                    if (clientMessage.startsWith("/nick")) {
                        String[] array = clientMessage.split("-", 3);
                        server.sendMessageToClients(this, array[1], array[2]);
                    } else {
                        server.sendMessageToAllClients(clientMessage);
                    }
                }
            }
        } finally {
            closeConnection();
        }

    }

    public void sendMsg(String msg) {
        try {
            outMessage.println(msg);
            outMessage.flush();
        } catch (Exception e) {
            logger.log(e.getMessage());
        }
    }

    public void authentication()  {
        while (true) {
            String message = inMessage.nextLine();
            if (message.startsWith("/start")) {
                String[] arr = message.split("-", 2);
                final String nick = arr[1];
                if (nick != null) {
                    if (!server.nickNameIsBusy(nick)) {
                        this.nick = nick;
                        server.sendMessageToAllClients(" New participant " + nick + " come in chat!");
                        logger.log(" New participant " + nick + " come in chat! "+ Instant.now().toString());
                        return;
                    } else {
                        sendMsg("Your nick is busy now. Try later.");
                    }
                } else {
                    sendMsg("Wrong nick");
                }
            }
        }
    }

    public void closeConnection() {
        server.removeClient(this);
        try {
            socket.close();
        } catch (IOException e) {
            logger.log(e.getMessage());
        }
        outMessage.close();
        sc.close();
        inMessage.close();
    }
}
