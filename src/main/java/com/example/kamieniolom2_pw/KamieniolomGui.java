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
    private static final int GRID_SIZE = 3; // Rozmiar siatki (3x3)
    private static final int CELL_SIZE = 50; // Rozmiar każdej komórki w pikselach
    private static final int WINDOW_WIDTH = 700; // Szerokość okna
    private static final int WINDOW_HEIGHT = 400; // Wysokość okna
    private static final int PALETTE_LIMIT = 3; // Ilość palet do napełnienia


    private final int[] palleteMaxWeight = {14, 13, 11};
    private final int[] stoneWeight = {1, 3, 5};
    private int currentPaletteNumber = 1;
    private int currentPaletteWeight = 0;


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

    private Scene createScene() {
        HBox root = new HBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER_LEFT);

        // Tworzenie siatki reprezentującej paletę

        GridPane palletGrid = new GridPane();
        palletGrid.setPadding(new Insets(10));
        palletGrid.setHgap(10);
        palletGrid.setVgap(10);

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                Pane cell = new Pane();
                cell.setMinSize(CELL_SIZE, CELL_SIZE);
                cell.setStyle("-fx-border-color: black;");
                palletGrid.add(cell, j, i); // Zmiana indeksowania wierszy i kolumn
            }
        }

        // Tekst wyświetlający numer aktualnej palety
        Text paletteCounter = new Text("Paleta: " + currentPaletteNumber + ", max waga: " + palleteMaxWeight[currentPaletteNumber -1]);
        paletteCounter.setStyle("-fx-font-size: 16px;");
        Text weightCounter = new Text("Aktualna waga palety: " + currentPaletteWeight);
        weightCounter.setStyle("-fx-font-size: 16px;");

        // Ustawienia liczby pracowników
        VBox settingsPane = new VBox(20);
        settingsPane.setAlignment(Pos.TOP_CENTER);

        Label workerLabel = new Label("Liczba pracowników:");
        Spinner<Integer> workerSpinner = new Spinner<>();
        workerSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));

        // Przycisk aktualizuj
        Button updateWorkersButton = new Button("Aktualizuj");
        updateWorkersButton.setOnAction(e -> updateWorkers());

        // Przycisk stopu
        Button stopButton = new Button("Zatrzymaj");
        stopButton.setOnAction(e -> stopWorkers());

        settingsPane.getChildren().addAll(workerLabel, workerSpinner, updateWorkersButton, paletteCounter, weightCounter);

        root.getChildren().addAll(palletGrid, settingsPane);

        return new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

}
