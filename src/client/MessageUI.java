package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import ip.IP_ADDRESS;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MessageUI extends Application {
    private String username;
    private ListView<String> userList;

    public MessageUI(String username) {
        this.username = username; // Nhận username từ LoginUI
    }

    @Override
    public void start(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        
        Label welcomeLabel = new Label("Chào mừng " + username + " đến với hệ thống chat!");
        userList = new ListView<>();
        Button selectButton = new Button("Chọn người để chat");
        Button groupChatButton = new Button("Tham gia nhóm chat");
        
        groupChatButton.setOnAction(event -> openGroupChatbox(primaryStage));

        selectButton.setOnAction(event -> {
            String selectedUser = userList.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                openChatbox(selectedUser, primaryStage); 
            }
        });

        layout.getChildren().addAll(welcomeLabel, userList, selectButton, groupChatButton);
        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Chat");
        primaryStage.show();

        // Kết nối đến server và tải danh sách người dùng
        loadUserList();
    }

    private void loadUserList() {
        try {
            Socket socket = new Socket(IP_ADDRESS.IP, 12345);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Gửi yêu cầu lấy danh sách người dùng
            out.println("GET_USER_LIST|" + username);

            // Nhận danh sách người dùng từ server
            String userListResponse = in.readLine();
            if (userListResponse != null && !userListResponse.isEmpty()) {
                String[] users = userListResponse.split(",");
                for (String user : users) {
                    if (!user.isEmpty()) {
                        userList.getItems().add(user);
                    }
                }
            }

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hàm mở ChatboxUI khi chọn user từ danh sách
    private void openChatbox(String selectedUser, Stage primaryStage) {
        ChatboxUI chatboxUI = new ChatboxUI(username, selectedUser);
        try {
            chatboxUI.start(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void openGroupChatbox(Stage primaryStage) {
        try {
            // Tạo kết nối đến server để gửi yêu cầu tham gia nhóm
            Socket socket = new Socket(IP_ADDRESS.IP, 12345);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            
            // Gửi yêu cầu tham gia nhóm
            out.println("JOIN_GROUP|" + username); // Gửi yêu cầu tham gia nhóm

            // Mở GroupChatboxUI
            GroupChatboxUI groupChatboxUI = new GroupChatboxUI(username);
            groupChatboxUI.start(primaryStage); 

            // Đóng kết nối socket
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
