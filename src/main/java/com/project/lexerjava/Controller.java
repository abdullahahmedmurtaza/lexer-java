package com.project.lexerjava;

// Ye file main code hai jo sab kuch control kar rahi hai
// Almost puri logic idhar hi defined hoti hai.
// Simple event --> eventHandlers use hote hain.

// methods idhar define hote hain or scene builder me jaa ke button se connect / attach hote hain.

// Scenes switching ke liye bhi idhar hi methods implmemented hote hain.

// Aik FXML pe aik hi Controller class use hosakti hai multiple nahin.


import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;


// Ye class sirf scenes ki switching ko control karegi. ke konse button ke click hone pe konsa scene ayega.

public class Controller {
    private Stage stage;
    private Scene scene;
    private Parent root;


    // ---- Analyzer.fxml ke fx:id se yahan bind honge (Upload/Export/Table/Errors) ----
    @FXML private Button uploadFileButton;
    @FXML private Text loadedFileText;
    @FXML private Hyperlink exportLink;
    @FXML private TableView<Token> tokenTable;
    @FXML private TableColumn<Token, String> tokenColumn;
    @FXML private TableColumn<Token, String> lexemeColumn;
    @FXML private TableColumn<Token, Integer> lineNumberColumn;
    @FXML private TextArea errorArea;

    // ---- Phase 2 (Parser) ke controls ----
    @FXML private Button parserButton;
    @FXML private TableView<SymbolEntry> symbolTable;
    @FXML private TableColumn<SymbolEntry, String> symLexemeColumn;
    @FXML private TableColumn<SymbolEntry, String> symTypeColumn;
    @FXML private TableColumn<SymbolEntry, String> symScopeColumn;
    @FXML private TableColumn<SymbolEntry, String> symArraySizeColumn;

    // Lexer engine + last results (Export button ko baad me inhi ki zaroorat hogi).
    private final LexerEngine lexerEngine = new LexerEngine();
    private List<Token> lastTokens;
    private List<LexError> lastErrors;

    // Phase 2: parser lastTokens (Phase 1 ka output) par chalta hai.
    private final SyntaxParser syntaxParser = new SyntaxParser();
    private List<SymbolEntry> lastSymbols;      // parser chalne tak null
    private List<SyntaxError> lastSyntaxErrors; // parser chalne tak null

    // FXMLLoader ye method automatically call krta hai jab saare @FXML fields inject ho chuke hon.
    // TableView ke columns ko Token model ki properties se bind karte hain idhar.
    // Null check isliye hai kyun ke ye Controller About.fxml / HowToUse.fxml pe bhi reuse hoti hai,
    // jahan analyzer-specific fields exist hi nahin karte.
    @FXML
    private void initialize() {
        if (tokenTable == null) {
            return;
        }
        tokenColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        lexemeColumn.setCellValueFactory(new PropertyValueFactory<>("lexeme"));
        lineNumberColumn.setCellValueFactory(new PropertyValueFactory<>("lineNumber"));
        tokenTable.setItems(FXCollections.observableArrayList());

        if (symbolTable != null) { // FXML mein symbol table aane tak crash se bachne ke liye
            symLexemeColumn.setCellValueFactory(new PropertyValueFactory<>("lexeme"));
            symTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            symScopeColumn.setCellValueFactory(new PropertyValueFactory<>("scope"));
            symArraySizeColumn.setCellValueFactory(new PropertyValueFactory<>("arraySize"));
            symbolTable.setItems(FXCollections.observableArrayList());
        }
    }

    // Method to handle the click on "Start Analyzing" button.
    // handlers need an ActionEvent object --> fire this method when an event is encountered.

//    Switching ka code thora complex hai, lekin bass castings yaad honi chahiyen to itna msla nahin hota.

//    Ye IOException bhi throw krta hai.


//    Switching methods.

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



//    Exit confirmation method "stage" se related hai isliye Main.java mein ayega wo.

//    File Uploading method

    @FXML
    private void handleUploadFile(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select a C source file");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("C source files", "*.c", "*.h"));

        File file = chooser.showOpenDialog(uploadFileButton.getScene().getWindow());
        if (file == null) {
            return; // user cancelled the dialog
        }

        try {
            LexerEngine.LexResult result = lexerEngine.analyze(file);
            lastTokens = result.getTokens();
            lastErrors = result.getErrors();
            lastSymbols = null;
            lastSyntaxErrors = null;
            if (symbolTable != null) {
                symbolTable.setItems(FXCollections.observableArrayList());
            }
            showTokenTable();

            tokenTable.setItems(FXCollections.observableArrayList(lastTokens));
            loadedFileText.setText("Loaded: " + file.getName());

            if (lastErrors.isEmpty()) {
                errorArea.setText("No lexical errors found.");
            } else {
                StringBuilder sb = new StringBuilder();
                for (LexError e : lastErrors) {
                    sb.append(e.toString()).append("\n");
                }
                errorArea.setText(sb.toString());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Failed to read file: " + e.getMessage());
        }

    }

    // Export method (Apache POI). FileChooser ke extension filters se
    // .xlsx ya .csv choose kiya jata hai, phir ExcelExportUtil ka sahi method call hota hai.

    @FXML
    private void handleExport(ActionEvent event) {
        if (lastTokens == null) {
            showAlert(Alert.AlertType.WARNING, "Upload and analyze a file before exporting.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export analysis results");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx"),
                new FileChooser.ExtensionFilter("CSV File", "*.csv"));

        File file = chooser.showSaveDialog(exportLink.getScene().getWindow());
        if (file == null) {
            return; // user cancelled
        }

        try {
            if (file.getName().toLowerCase().endsWith(".csv")) {
                ExcelExportUtil.exportToCsv(lastTokens, lastErrors, file);
            } else {
                ExcelExportUtil.exportToExcel(lastTokens, lastErrors, file);
            }
            showAlert(Alert.AlertType.INFORMATION, "Exported successfully to " + file.getName());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Export failed: " + e.getMessage());
        }
    }
    // ---------------------------------------------------------------------
    // Phase 2: "Run Parser" button. Lexer ke tokens (lastTokens) par SyntaxParser chalta hai,
    // phir Symbol Table table mein aur syntax errors Results box mein dikhte hain.
    // ---------------------------------------------------------------------
    @FXML
    private void handleParse(ActionEvent event) {
        if (lastTokens == null) {
            showAlert(Alert.AlertType.WARNING, "Upload a file first. The parser works on the tokens produced by the lexer.");
            return;
        }

        SyntaxParser.ParseResult result = syntaxParser.parse(lastTokens);
        lastSymbols = result.getSymbols();
        lastSyntaxErrors = result.getErrors();

        symbolTable.setItems(FXCollections.observableArrayList(lastSymbols));
        showSymbolTable();

        StringBuilder sb = new StringBuilder();
        if (lastSyntaxErrors.isEmpty()) {
            sb.append("Parsing completed. No syntax errors found.\n");
        } else {
            sb.append("Syntax errors (").append(lastSyntaxErrors.size()).append("):\n");
            for (SyntaxError e : lastSyntaxErrors) {
                sb.append(e.toString()).append("\n");
            }
        }
        if (lastErrors != null && !lastErrors.isEmpty()) {
            sb.append("\nLexical errors from Phase 1 (").append(lastErrors.size()).append("):\n");
            for (LexError e : lastErrors) {
                sb.append(e.toString()).append("\n");
            }
        }
        errorArea.setText(sb.toString());
    }

    @FXML
    private void handleShowTokens(ActionEvent event) {
        showTokenTable();
    }

    // Token table aur Symbol table ek hi jagah par hain: ek dikhti hai, doosri chhupti hai.
    private void showSymbolTable() {
        tokenTable.setVisible(false);
        tokenTable.setManaged(false);
        symbolTable.setManaged(true);
        symbolTable.setVisible(true);
    }

    private void showTokenTable() {
        if (symbolTable != null) {
            symbolTable.setVisible(false);
            symbolTable.setManaged(false);
        }
        tokenTable.setManaged(true);
        tokenTable.setVisible(true);
    }
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
