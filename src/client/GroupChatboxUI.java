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

public class GroupChatboxUI extends Application {
    private String username;
    private TextArea chatArea;
    private TextField messageField;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public GroupChatboxUI(String username) {
        this.username = username;
    }

    @Override
    public void start(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);

        chatArea = new TextArea();
        chatArea.setEditable(false);

        messageField = new TextField();
        messageField.setPromptText("Nhập tin nhắn...");

        Button sendButton = new Button("Gửi");
        sendButton.setOnAction(event -> sendMessage());

        Button leaveButton = new Button("Rời nhóm");
        leaveButton.setOnAction(event -> leaveGroup(primaryStage));

        layout.getChildren().addAll(chatArea, messageField, sendButton, leaveButton);
        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Nhóm Chat - " + username);
        primaryStage.show();

        try {
            // Initialize the socket and I/O streams here
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            startListeningForMessages(); // Bắt đầu lắng nghe tin nhắn mới từ server
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            try {
                out.println("GROUP_MESSAGE|" + username + "|" + message);
                messageField.clear(); // Xóa trường nhập tin nhắn
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startListeningForMessages() {
        new Thread(() -> {
            try {
                socket = new Socket("localhost", 12345);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Lắng nghe tin nhắn từ server
                String line;
                while ((line = in.readLine()) != null) {
                    chatArea.appendText(line + "\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void leaveGroup(Stage primaryStage) {
        try {
            out.println("LEAVE_GROUP|" + username); // Gửi yêu cầu rời nhóm đến server
        } catch (Exception e) {
            e.printStackTrace();
        }
        primaryStage.close();
        
        // Khởi tạo và hiển thị giao diện MessageUI
        MessageUI messageUI = new MessageUI(username);
        messageUI.start(new Stage());
    }
}