package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MyClient extends JFrame {
    private JTextField userInputText;
    private JTextArea chatWindow;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private Socket socket;
    private JTextField name = new JTextField("Unknown name");

    public static void main(String[] args) {
        new MyClient();
    }

    protected MyClient(){
        super("Client");
        userInputText = new JTextField();
        userInputText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (name.getText().isEmpty())//если поле с именем пустое, то записываем Unknown name
                    name.setText("Unknown name");
                sendMessage(name.getText() + ": " + e.getActionCommand());
                userInputText.setText("");
            }
        });
        add(userInputText, BorderLayout.NORTH);
        chatWindow = new JTextArea();
        chatWindow.setEditable(false);
        add(new JScrollPane(chatWindow), BorderLayout.CENTER);
        add(name, BorderLayout.SOUTH);
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        startClient();
    }
    private void startClient() {
        try {
            connectServer();
            whileChatting();
        }catch (EOFException e){
            showMessage("Клиент оборвал соединение");
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            closeConnection();
        }
    }
    private void connectServer() throws IOException {
        showMessage("Connecting...");
        socket = new Socket();
        socket.connect(new InetSocketAddress("127.0.0.1", 5656), 2000);
        printWriter = new PrintWriter(socket.getOutputStream(), true);
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        showMessage("Connection ready");
    }
    /*Чтение сообщений и вывод на экран*/
    private void whileChatting() throws IOException {
        String message;
        while ((message = bufferedReader.readLine()) != null){
            showMessage("\n" + message);
        }
    }
    private void closeConnection(){
        showMessage("\nClose connection...");
        try {
            printWriter.close();
            bufferedReader.close();
            socket.close();
        } catch (IOException e) {
            e.getMessage();
        }
    }
    /*Метод отправки сообщения*/
    protected void sendMessage(String message){
        printWriter.println(message);
    }
    private void showMessage(String msg){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                chatWindow.append(msg);
            }
        });
    }
}
