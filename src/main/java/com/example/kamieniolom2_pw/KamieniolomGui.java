package com.example.kamieniolom2_pw;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class KamieniolomGui extends Application {
    private static final int GRID_SIZE = 3; // Rozmiar siatki (3x3)
    private static final int CELL_SIZE = 50; // Rozmiar każdej komórki w pikselach
    private static final int WINDOW_WIDTH = 700; // Szerokość okna
    private static final int WINDOW_HEIGHT = 400; // Wysokość okna
    private static final int PALETTE_LIMIT = 3; // Ilość palet do napełnienia
    private boolean[][] cellOccupied = new boolean[GRID_SIZE][GRID_SIZE];



    private final int[][][] decisions = {{{1, 1, 2}, {2, 2, 1}, {3, 3, 0}},
                                         {{2, 2, 1}, {3, 3, 0}, {5, 2, 0}},
                                         {{6, 0, 1}, {5, 2, 0}, {7, 1, 0}}
    };
    private int[] tempDecisions;
    private final int[] palleteMaxWeight = {14, 13, 11};
    private final int[] stoneWeight = {1, 3, 5};
    private int currentPaletteNumber = 1;
    private int currentPaletteWeight = 0;
    private int combination;

    private GridPane palletGrid;
    private Text paletteCounter;
    private Text weightCounter;
    Spinner<Integer> workerSpinner;
    private ExecutorService executorService;
    private Random random = new Random();
    private final Lock lock = new ReentrantLock();
    private final CountDownLatch signalFullPalette = new CountDownLatch(3);



    @Override
    public void start(Stage stage) {
        Scene scene = createScene();
        stage.setTitle("Symulacja Kamieniołomu");
        stage.setScene(scene);
        stage.show();
    }


    private Scene createScene() {
        HBox root = new HBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER_LEFT);

        // Tworzenie siatki reprezentującej paletę

        palletGrid = new GridPane();
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
        paletteCounter = new Text("Paleta: " + currentPaletteNumber + ", max waga: " + palleteMaxWeight[currentPaletteNumber -1]);
        paletteCounter.setStyle("-fx-font-size: 16px;");
        weightCounter = new Text("Aktualna waga palety: " + currentPaletteWeight);
        weightCounter.setStyle("-fx-font-size: 16px;");

        // Ustawienia liczby pracowników
        VBox settingsPane = new VBox(20);
        settingsPane.setAlignment(Pos.TOP_CENTER);

        Label workerLabel = new Label("Liczba pracowników:");
        workerSpinner = new Spinner<>();
        workerSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));

        // Przycisk aktualizuj
        Button startButton = new Button("Start");
        startButton.setOnAction(e -> startWorkers());

        // Przycisk stopu
        Button stopButton = new Button("Reset");
        stopButton.setOnAction(e -> resetButton());

        settingsPane.getChildren().addAll(workerLabel, workerSpinner, startButton, stopButton, paletteCounter, weightCounter);

        root.getChildren().addAll(palletGrid, settingsPane);

        return new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    private void startWorkers() {
        System.out.println("Początek startWorker, interrupt: " + Thread.currentThread().isInterrupted());
        executorService = Executors.newFixedThreadPool(workerSpinner.getValue());
        combination = random.nextInt(0, 3);
        int paletteIndex = currentPaletteNumber - 1;
        tempDecisions = decisions[paletteIndex][combination];

        Runnable worker = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("Wykonuję pętlę, wątek: " + Thread.currentThread().getName());
                addStoneToPalette();
                sleepSomeTime();
            }
            System.out.println("Wyszedłem z pętli.");
        };

        for (int i = 0; i < workerSpinner.getValue(); i++) {
            executorService.execute(worker);
//            executorService.submit(worker);
        }

    }

    private void sleepSomeTime() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
//            System.out.println("Złapałem interrupta!");
        }
    }

    private void stopWorkers() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    private void addStoneToPalette() {
        lock.lock();
        try {
            int stone = getStoneNumber();
            Color color;
            switch (stone) {
                case 1:
                    color = Color.RED;
                    break;
                case 2:
                    color = Color.GREEN;
                    break;
                case 3:
                    color = Color.BLUE;
                    break;
                default:
                    color = Color.BLACK;
                    break;
            }
            int[] info = findRowColumn(stone);
            int row = info[0];
            int column = info[1];
            if (stone > 0) {
                currentPaletteWeight += stoneWeight[stone - 1];
                Platform.runLater(() -> addStone(row, column, stone, color, lock));
            } else {
                signalFullPalette.countDown();
            }
        } finally {
            lock.unlock();
        }
        sleepSomeTime();
        sleepSomeTime();
        if (signalFullPalette.getCount() == 0) {
            Platform.runLater(this::resetGrid);
            nextPaletteSetup();
        }
    }

    private int getStoneNumber() {
        int stoneNumber = 0;
        for (int i = 2; i >= 0; i--) {
            if (tempDecisions[i] > 0) {
                stoneNumber = i + 1;
                tempDecisions[i]--;
                break;
            }
        }
        return stoneNumber;
    }

    private int[] findRowColumn(int stoneLength) {
        int row = 0;
        int column = 0;

        outerLoop:
        for (int i = 0; i < cellOccupied.length; i++) {
            for (int j = 0; j < cellOccupied.length; j++) {
                if (!cellOccupied[i][j] && (3-i >= stoneLength)) {
                    row = i;
                    column = j;
                    break outerLoop;
                }
            }
        }
        for (int k = 0; k < stoneLength; k++) {
            cellOccupied[row + k][column] = true;
        }

        return new int[]{row, column};
    }

    private void addStone(int row, int column, int length, Color color, Lock lock) {
        for (int i = 0; i < length; i++) {
            Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);
            rect.setFill(color);
            rect.setTranslateY(WINDOW_HEIGHT);
            palletGrid.add(rect, column, row + i);
            weightCounter.setText("Aktualna waga palety: " + currentPaletteWeight);
            paletteCounter.setText("Paleta: " + currentPaletteNumber + ", max waga: " + palleteMaxWeight[currentPaletteNumber - 1]);

            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(rect.translateYProperty(), 0);
            KeyFrame kf = new KeyFrame(Duration.seconds(2), kv);
            timeline.getKeyFrames().add(kf);
//            timeline.setOnFinished(e -> lock.unlock());
            timeline.play();
        }
    }

    private void resetGrid() {
        palletGrid.getChildren().clear();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                Pane cell = new Pane();
                cell.setMinSize(CELL_SIZE, CELL_SIZE);
                cell.setStyle("-fx-border-color: black;");
                palletGrid.add(cell, j, i);
                cellOccupied[i][j] = false;
            }
        }
    }

    private void nextPaletteSetup() {
        currentPaletteNumber++;
        if (currentPaletteNumber > PALETTE_LIMIT) {
            currentPaletteNumber = 1;
        }
        currentPaletteWeight = 0;
        paletteCounter.setText("Paleta: " + currentPaletteNumber + ", max waga: " + palleteMaxWeight[currentPaletteNumber - 1]);
        weightCounter.setText("Aktualna waga palety: " + currentPaletteWeight);

        int paletteIndex = currentPaletteNumber - 1;
        combination = random.nextInt(3);  // Losujemy nową kombinację dla nowej palety
        tempDecisions = decisions[paletteIndex][combination];
    }

    private void resetButton() {
        stopWorkers();
        currentPaletteNumber = 1;
        currentPaletteWeight = 0;
        Platform.runLater(() -> {
            resetGrid();
            paletteCounter.setText("Paleta: " + currentPaletteNumber + ", max waga: " + palleteMaxWeight[currentPaletteNumber - 1]);
            weightCounter.setText("Aktualna waga palety: " + currentPaletteWeight);
        });
        stopWorkers();
    }

    public static void main(String[] args) {
        launch();
    }

}

