package client;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class LoginUI extends Application {
    private TextField usernameField;

    @Override
    public void start(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        usernameField = new TextField();
        usernameField.setPromptText("Nhập tên người dùng");
        Button loginButton = new Button("Đăng nhập");
        Label messageLabel = new Label();

        loginButton.setOnAction(event -> login(messageLabel));

        layout.getChildren().addAll(usernameField, loginButton, messageLabel);
        Scene scene = new Scene(layout, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Đăng nhập");
        primaryStage.show();
    }

    private void login(Label messageLabel) {
        String username = usernameField.getText();
        if (!username.isEmpty()) {
            try {
                Socket socket = new Socket("localhost", 12345);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Gửi yêu cầu login
                out.println("LOGIN|" + username);

                // Nhận phản hồi từ server
                String response = in.readLine();
                if ("LOGIN_SUCCESS".equals(response)) {
                    messageLabel.setText("Đăng nhập thành công!");
                    // Chuyển sang giao diện MessageUI
                    MessageUI messageUI = new MessageUI(username);
                    Stage stage = (Stage) usernameField.getScene().getWindow();
                    messageUI.start(stage);
                } else {
                    messageLabel.setText("Đăng nhập thất bại! Người dùng không tồn tại.");
                }

                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
                messageLabel.setText("Lỗi kết nối đến server.");
            }
        } else {
            messageLabel.setText("Vui lòng nhập tên người dùng.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
