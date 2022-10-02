package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
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
            logger.log("Server started " + Instant.now().toString());
            clients = new ArrayList<>();
            while (true) {
                clientSocket = serverSocket.accept();
                logger.log("client " + clientSocket.getPort() + " accepted "+Instant.now().toString());
                ClientHandler client = new ClientHandler(clientSocket, this);
                clients.add(client);
                new Thread(client).start();
            }
        } catch (IOException e) {
            logger.log(e.getStackTrace().toString());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Server stopped!!");
                logger.log("server stopped");
                serverSocket.close();
            } catch (IOException e) {
                logger.log(e.getStackTrace().toString());
            }
        }
    }

    public synchronized void sendMessageToAllClients(String msg) {
        for (ClientHandler cl : clients) {
            cl.sendMsg(msg);
        }
    }

    public synchronized void sendMessageToClients(ClientHandler cH, String to, String message) {
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getNick().equals(to)) {
                clientHandler.sendMsg("  You have a message from  " + cH.getNick() + ": " + message);
                cH.sendMsg(" Message for : " + to + ": " + message);
                logger.log(cH.getNick()+" Message for : " + to + ": " + message + " " + Instant.now().toString());
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

    private int setPort() {
        try (FileReader reader = new FileReader("settings.txt");
             BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                port = Integer.parseInt(line);
            }
        } catch (IOException e) {
            logger.log(e.getStackTrace().toString());
        }
        return port;
    }

    public int getPort() {
        return port;
    }
}
