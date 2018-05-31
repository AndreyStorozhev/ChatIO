package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Server {
    private static List<ConnectionClient> connections = Collections.synchronizedList(new LinkedList<>());

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5656, 25)) {
            System.out.println("Server running...");
            while (true){
                Socket socket = serverSocket.accept();
                ConnectionClient connection = new ConnectionClient(socket, new Server());
                connection.select();
                new Thread(connection).start();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    /*Метод добавления клиента в коллекцию*/
    protected void addClient(ConnectionClient connection){
        connections.add(connection);
    }
    /*Метод для отправки сообщений всем клиентам*/
    protected void sendToAllConnection(String message){
        for (ConnectionClient c : connections)
            c.sendMessage(message);
    }
    /*Метод удаления клиента из коллекции и сообщение об этом всем клиентам*/
    protected void removeClient(ConnectionClient connection){
        connections.remove(connection);
        sendToAllConnection("Пользователь вышел из чата " + connection);
    }
}
