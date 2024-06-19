package com.example.kamieniolom2_pw;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class KamieniolomGui extends Application {
    @Override
    public void start(Stage stage) {
        Scene scene = createScene();
        stage.setTitle("Symulacja Kamieniołomu");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

//    private Scene createScene() {
//        HBox root = new HBox(20);
//        root.setPadding(new Insets(20));
//        root.setAlignment(Pos.CENTER_LEFT);
//
//        // Tworzenie siatki reprezentującej paletę
//
//        GridPane palletGrid = new GridPane();
//        palletGrid.setPadding(new Insets(10));
//        palletGrid.setHgap(10);
//        palletGrid.setVgap(10);
//
//        for (int i = 0; i < GRID_SIZE; i++) {
//            for (int j = 0; j < GRID_SIZE; j++) {
//                Pane cell = new Pane();
//                cell.setMinSize(CELL_SIZE, CELL_SIZE);
//                cell.setStyle("-fx-border-color: black;");
//                palletGrid.add(cell, j, i); // Zmiana indeksowania wierszy i kolumn
//            }
//        }
//
//        // Tekst wyświetlający numer aktualnej palety
//        paletteCounter = new Text("Paleta: " + currentPalette + ", max waga: " + palleteWeight[currentPalette-1]);
//        paletteCounter.setStyle("-fx-font-size: 16px;");
//        weightCounter = new Text("Aktualna waga palety: " + currentPaletteWeight);
//        weightCounter.setStyle("-fx-font-size: 16px;");
//
//        // Ustawienia liczby pracowników
//        VBox settingsPane = new VBox(20);
//        settingsPane.setAlignment(Pos.TOP_CENTER);
//
//        Label workerLabel = new Label("Liczba pracowników:");
//        workerSpinner = new Spinner<>();
//        workerSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
//
//        Button updateWorkersButton = new Button("Aktualizuj");
//        updateWorkersButton.setOnAction(e -> updateWorkers());
//
//        settingsPane.getChildren().addAll(workerLabel, workerSpinner, updateWorkersButton, paletteCounter, weightCounter);
//
//        root.getChildren().addAll(palletGrid, settingsPane);
//
//        return new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
//    }

}
