package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.*;

public class ConnectionClient implements Runnable {
    private Socket socket;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private Server server;
    private static int clients_count = 0;//счётчик клиентов в чаие
    private Connection connection_BD;
    private String ip;
    private int port;

    protected ConnectionClient(Socket socket, Server server){
        clients_count++;
        this.server = server;
        this.socket = socket;
        connectionBD();
        insertUser(socket);
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            whileChatting();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            closeConnection();
        }
    }
    /*Метод выводит количество человек в чате, добавляет конекшн клиента в коллекцию.
     * Читает сообщения и отправляет всем клиентам*/
    private void whileChatting() throws IOException {
        printWriter.println("Новый участник вошёл в чат \nЛюдей в чате: " + clients_count + "\r\n");
        server.addClient(this);
        String message = "SERVER msg: you are connecting " + socket.getInetAddress() + " : " + socket.getPort();
        printWriter.println(message + "\r\n");
        while ((message = bufferedReader.readLine()) != null){
            insertMessage(message);
            server.sendToAllConnection(message);
            System.out.println(message);
        }
    }
    private void closeConnection(){
        server.removeClient(this);
        clients_count--;
        server.sendToAllConnection("Пользователь отключился \r\n" + "Людей в чате " + clients_count);
        try {
            printWriter.close();
            bufferedReader.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected void sendMessage(String message){
        printWriter.println(message);
    }

    @Override
    public String toString() {
        return "Connection: " + socket.getInetAddress() + " : " + socket.getPort();
    }
    /*Метод коннектится с базой данных*/
    private void connectionBD(){
        String userName = "root";
        String pass = "1234";
        String connectURL = "jdbc:mysql://localhost:3306/mydbtest";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection_BD = DriverManager.getConnection(connectURL, userName, pass);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    /*Добавление ip и port подключившевося клиента в БД*/
    private void insertUser(Socket socket){
        ip = socket.getInetAddress() + "";
        port = socket.getPort();
        try (PreparedStatement preparedStatement = connection_BD.prepareStatement("INSERT INTO ueres (ip_user, port_user, message_user) VALUES (?, ?, ?)")) {
            preparedStatement.setString(1, ip);
            preparedStatement.setInt(2, port);
            preparedStatement.setString(3, "");
            preparedStatement.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    /*Запись всех сообщений клиента с новой строки */
    private void insertMessage(String message){
        try (PreparedStatement preparedStatement = connection_BD.prepareStatement("UPDATE ueres SET message_user = CONCAT(message_user, ?) WHERE (ip_user = ? AND port_user = ?)")){
            preparedStatement.setString(1, message + "\r\n");
            preparedStatement.setString(2, ip);
            preparedStatement.setInt(3, port);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /*Чтение всех сообщений пользователей*/
    protected void select(){
        try (Statement statement = connection_BD.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM ueres")) {
            while (resultSet.next()){
                String message = resultSet.getString("message_user");
                System.out.println(message);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
