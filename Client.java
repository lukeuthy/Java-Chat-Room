package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class Client extends Application {

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    private VBox messageContainer;

    @Override
    public void start(Stage primaryStage) {
        messageContainer = new VBox();
        messageContainer.setSpacing(5);
        ScrollPane scrollPane = new ScrollPane(messageContainer);
        scrollPane.setFitToWidth(true);

        TextField inputField = new TextField();
        inputField.setPromptText("Type your message...");
        inputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String message = inputField.getText().trim();
                if (!message.isEmpty()) {
                    sendMessage(message);
                    inputField.clear();
                }
            }
        });

        VBox root = new VBox(10, scrollPane, inputField);
        root.setPrefSize(400, 600);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        primaryStage.setTitle("Chat Client");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Start the client socket connection
        new Thread(this::startClient).start();
    }

    private void startClient() {
        try {
            client = new Socket("127.0.0.1", 9999);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            String inMessage;
            while ((inMessage = in.readLine()) != null) {
                addMessage(inMessage);
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    private void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    private void addMessage(String message) {
        Platform.runLater(() -> {
            TextFlow messageBubble = new TextFlow(new Text(message));
            messageBubble.getStyleClass().add("message-bubble");
            messageContainer.getChildren().add(messageBubble);
        });
    }

    private void shutdown() {
        done = true;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (client != null && !client.isClosed()) client.close();
        } catch (IOException e) {
            // Ignore
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
