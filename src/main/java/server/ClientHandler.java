package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Scanner;

public class ClientHandler implements Runnable {

    private Server server;
    private PrintWriter outMessage;
    private Scanner inMessage;
    private String nick = "administrator";
    private final Scanner sc = new Scanner(System.in);
    private Socket socket;
    private final Logger logger = Logger.getInstance();
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
        try{
            while (true) {
                authentication();
                break;
            }

            while (true) {
                if (inMessage.hasNext()) {
                    String clientMessage = inMessage.nextLine();
                    if (clientMessage.equalsIgnoreCase("/exit")) {
                        server.sendMessageToAllClients(this,nick + " out of chat");
                        server.sendClientToCloseConnection(this,"/end");
                        logger.log(nick + " out of chat "+ LocalDateTime.now());
                        break;
                    }
                    if (clientMessage.startsWith("/nick")) {
                        String[] array = clientMessage.split("-", 3);
                        server.sendMessageToClients(this, array[1], array[2]);
                    } else {
                        server.sendMessageToAllClients(this,clientMessage);
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
                final String nickName = arr[1];
                if (nickName != null) {
                    if (!server.nickNameIsBusy(nickName)) {
                        this.nick = nickName;
                        server.sendMessageToAllClients(this," New participant " + nick + " come in chat!");
                        logger.log(" New participant " + nick + " come in chat! "+ LocalDateTime.now());
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
