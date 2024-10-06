package client;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import ip.IP_ADDRESS;

public class ChatboxUI extends Application {
    private String username;  // Tên người dùng hiện tại
    private String chatWith;  // Tên người dùng mà bạn muốn chat
    private TextArea chatArea;
    private TextField messageField;
    private PrintWriter out;

    public ChatboxUI(String username, String chatWith) {
        this.username = username; // Nhận username
        this.chatWith = chatWith; // Nhận tên người dùng để chat
    }

    @Override
    public void start(Stage primaryStage) {
        // Khởi tạo và hiển thị giao diện chat
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        
        chatArea = new TextArea();
        chatArea.setEditable(false);
        messageField = new TextField();
        Button sendButton = new Button("Gửi");
        Button refreshButton = new Button("Làm mới tin nhắn");
        Button exitButton = new Button("Thoát"); // Nút Thoát

        sendButton.setOnAction(event -> sendMessage());
        refreshButton.setOnAction(event -> loadMessages());
        exitButton.setOnAction(event -> goToMessageUI(primaryStage)); // Hành vi khi bấm nút Thoát

        layout.getChildren().addAll(chatArea, messageField, sendButton, refreshButton, exitButton);
        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Chat with " + chatWith);
        primaryStage.show();

        connectToServer(); // Kết nối đến server
        loadMessages(); // Tải tin nhắn từ server
    }

    // Kết nối đến server
    private void connectToServer() {
        try {
            Socket socket = new Socket(IP_ADDRESS.IP, 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Tải tin nhắn từ server
    private void loadMessages() {
        new Thread(() -> {
            try {
                Socket socket = new Socket(IP_ADDRESS.IP, 12345);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Gửi yêu cầu lấy tin nhắn giữa user1 và user2
                out.println("GET_MESSAGES|" + username + "|" + chatWith);

                // Nhận phản hồi từ server và hiển thị nội dung tin nhắn vào chatArea
                StringBuilder allMessages = new StringBuilder();
                String message;
                while ((message = in.readLine()) != null && !message.isEmpty()) {
                    allMessages.append(message).append("\n");
                }

                // Cập nhật lại chatArea với tin nhắn mới, xóa nội dung cũ trước đó
                chatArea.clear();
                chatArea.setText(allMessages.toString());

                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Gửi tin nhắn
    private void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            chatArea.appendText(username + ": " + message + "\n");
            messageField.clear();
            // In ra log trên client xem tin nhắn gửi đi có đúng không
            System.out.println("Sending message to server: " + message);
            // Gửi tin nhắn đến server theo định dạng "PRIVATE|username1|username2|message"
            out.println("PRIVATE|" + username + "|" + chatWith + "|" + message);
        } else {
            chatArea.appendText("Bạn cần nhập tin nhắn trước khi gửi.\n");
        }
    }

    // Phương thức để quay lại MessageUI
    private void goToMessageUI(Stage primaryStage) {
        // Đóng cửa sổ chat hiện tại
        primaryStage.close();
        
        // Khởi tạo và hiển thị giao diện MessageUI
        MessageUI messageUI = new MessageUI(username); // Giả sử bạn truyền username vào MessageUI
        messageUI.start(new Stage()); // Khởi động giao diện MessageUI mới
    }

    public static void main(String[] args) {
        launch(args);
    }
}
