package com.project.lexerjava;

// Ye file main code hai jo sab kuch control kar rahi hai
// Almost puri logic idhar hi defined hoti hai.
// Simple event --> eventHandlers use hote hain.

// methods idhar define hote hain or scene builder me jaa ke button se connect / attach hote hain.

// Scenes switching ke liye bhi idhar hi methods implmemented hote hain.

// Aik FXML pe aik hi Controller class use hosakti hai multiple nahin.


import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


// Ye class sirf scenes ki switching ko control karegi. ke konse button ke click hone pe konsa scene ayega.

public class Controller {
    private Stage stage;
    private Scene scene;
    private Parent root;


    // Method to handle the click on "Start Analyzing" button.
    // handlers need an ActionEvent object --> fire this method when an event is encountered.

//    Switching ka code thora complex hai, lekin bass castings yaad honi chahiyen to itna msla nahin hota.

//    Ye IOException bhi throw krta hai.

    public void switchToAnalyzer(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Analyzer.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        String css = this.getClass().getResource("styles.css").toExternalForm();
        scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.show();
    }
    public void switchToAbout(ActionEvent event) throws IOException{
        Parent root = FXMLLoader.load(getClass().getResource("About.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        String css = this.getClass().getResource("styles.css").toExternalForm();
        scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.show();
    }
    public void switchToHowToUse(ActionEvent event) throws IOException{
        Parent root = FXMLLoader.load(getClass().getResource("HowToUse.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        String css = this.getClass().getResource("styles.css").toExternalForm();
        scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.show();
    }
}
