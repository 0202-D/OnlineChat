package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Server {
    private int port;
    private Logger logger;
    private ArrayList<ClientHandler> clients;
    private Socket clientSocket;
    private ServerSocket serverSocket;

    public Server() {

        try {
            logger = Logger.getInstance();
            serverSocket = new ServerSocket(setPort());
            System.out.println("Server started!");
            logger.log("Server started " + LocalDateTime.now());
            clients = new ArrayList<>();
            while (true) {
                clientSocket = serverSocket.accept();
                logger.log("client " + clientSocket.getPort() + " accepted " + LocalDateTime.now());
                ClientHandler client = new ClientHandler(clientSocket, this);
                clients.add(client);
                new Thread(client).start();
            }
        } catch (IOException e) {
            logger.log(e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Server stopped!!");
                logger.log("server stopped");
                serverSocket.close();
            } catch (IOException e) {
                logger.log(e.getMessage());

            }
        }
    }

    public synchronized void sendMessageToAllClients(ClientHandler cH, String msg) {
        for (ClientHandler cl : clients) {
            if (!cl.getNick().equals(cH.getNick())) {
                cl.sendMsg(cH.getNick()+" : "+msg);
            }
            logger.log(cH.getNick()+" Send message for all "+msg+" "+LocalDateTime.now());
        }

    }

    public synchronized void sendClientToCloseConnection(ClientHandler cH, String message) {
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getNick().equals(cH.getNick())) {
                clientHandler.sendMsg(message);
                return;
            }
        }
    }

    public synchronized void sendMessageToClients(ClientHandler cH, String to, String message) {
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getNick().equals(to)) {
                clientHandler.sendMsg("  You have a message from  " + cH.getNick() + ": " + message);
                cH.sendMsg(" Message for : " + to + ": " + message);
                logger.log(cH.getNick() + " Message for : " + to + ": " + message + " " + LocalDateTime.now());
                return;
            }
        }

        cH.sendMsg("Client " + to + " out of chat");
    }

    public synchronized boolean nickNameIsBusy(String nickName) {
        return clients
                .stream()
                .anyMatch(clientHandler -> clientHandler.getNick().equalsIgnoreCase(nickName));
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public int setPort() {
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

}
