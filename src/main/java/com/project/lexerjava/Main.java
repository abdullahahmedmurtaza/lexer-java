package com.project.lexerjava;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
// JavaFX ki har application (jo hum banate hain) Application class ko extend krti hai. Isme aik abstract method hota hai jisko implement krna zaruri hota hai --> wo method hai 'start()'

// start method 1 argument leta hai jiski type 'Stage' hoti hai or Exception/IOException throw krta hai


public class Main extends Application {

    public static void main(String[] args) {
//            start method ko behind the scenes call krne ke liye hum 'launch' method use krte hain, jisme hum 'args' pass krdete hain main method wali. --> launch method static hota hai but hum 'Application.launch' bhi likh sakte hain
        launch(args);
    }

//    First step --> Stage create krna. Ye humein already milti hai start ki argument mein. 'Stage stage'
    @Override
    public void start(Stage stage) throws Exception{

        //Group root = new Group(); // root nodes bohat types ke hote, mein group use krrha hun (sbse common or easy).

        // Root hum FXML se load krenge so for that we will be using FXMLLoader's method '.load()'.
        Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
        //        Hum stage dikhane ke liye stage.show method use krte hain, lekin bina scene ke stage theek se behave nahin krti resizing pe. Iske liye pehle 'Scene scene = new Scene' se object create hoga

        Scene scene = new Scene(root, //600,//600,
                 Color.LIGHTCYAN); // Iske constructor ko parent/root node chahiye hota hai. To upar wo bhi bana na hoga. Isme color bhi pass krskte scene ka. Isme bhi width or height bata sakte hain stage ki. --> Scene Builder ki waja se comment out krdi taake aik hi jaga se set ho.


//        CSS add krni hai styles ke liye taake design easily match ho sakey. Humein usually same stylesheet hi multiple scenes pe use krni hoti hai isliye hum readability ke liye file path ko break krdete hain.
//        Copy everything from the .add method onwards.
        String css = this.getClass().getResource("styles.css").toExternalForm();
        scene.getStylesheets().add(css);


        // ab hum is string variable ko reuse krskte hain multiple times

//        Scene Builder se id assign krke style krskte hain css mein.

        stage.setTitle("lexer-java"); // Title set krne ke liye

//        Icons set krne ke liye image resources folder mein honi chahiye --> then uska object banta hai then wo add hoti hai. Iska code niche given hai

        Image icon = new Image("lexer-icon.jpg");
        stage.getIcons().add(icon);


//        Width or Height bhi set krni hoti stage ki (mene already constructor mein ki hui hai), or ye bhi ke resizeable hai ya nahin.
//        stage.setWidth(420);
//        stage.setHeight(420);
        stage.setResizable(false);

//        By default fullscreen mein nahin khulei uske liye false rakha hai, or fullscreen se exit hone ke liya kya krna parta wo bhi define krsakte hain.

        stage.setFullScreen(false);
        stage.setFullScreenExitKeyCombination(KeyCombination.valueOf("ESC"));
        stage.setFullScreenExitHint("Press ESCAPE to exit fullscreen");

//        Apne screen pe kahan open hogi uske x or y bhi set krskte but by default center mein hi khulegi.

        stage.setScene(scene); // Jo scene banaya hai usko stage pe bhi to dalna hoga.
        stage.show();

        // Stage --> Scene --> Scene Graph (root)

//        Scenes basically drawing surface hote graphical content ke liye to hum kaafi saari chizein like text, lines, images wagera add krskte by using "root.getChildren().add("text") etc. ", or methods ke through unki properties bhi change krskte but easy hota hai Scene Builder use krna.

//        closing pe exitConfirmation ko call krna hai.
//        Idhar stage isliye paas ki taake har baar confirmation ajaye.
        stage.setOnCloseRequest(event -> exitConfirmation(stage));
    }
    //    Exit Confirmation Method
    public void exitConfirmation(Stage stage){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText("You are about to exit.");
        alert.setContentText("Are you sure you want to quit?");
        if (alert.showAndWait().get() == ButtonType.OK){
            stage.close();
        }
    }
}