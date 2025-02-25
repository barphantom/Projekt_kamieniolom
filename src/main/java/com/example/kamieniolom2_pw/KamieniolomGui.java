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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class KamieniolomGui extends Application {
    private static final int GRID_SIZE = 3; // Rozmiar siatki (3x3)
    private static final int CELL_SIZE = 50; // Rozmiar każdej komórki w pikselach
    private static final int WINDOW_WIDTH = 700; // Szerokość okna
    private static final int WINDOW_HEIGHT = 400; // Wysokość okna
    private static final int PALETTE_LIMIT = 3; // Ilość palet do napełnienia
    private boolean[][] cellOccupied = new boolean[GRID_SIZE][GRID_SIZE];

    private final static int[][][] decisions = {
            {{1, 1, 2}, {2, 2, 1}, {3, 3, 0}},
            {{2, 2, 1}, {3, 3, 0}, {5, 2, 0}},
            {{6, 0, 1}, {5, 2, 0}, {7, 1, 0}}
    };
    private int[] tempDecisions;
    private final int[] palleteMaxWeight = {14, 13, 11};
    private final int[] stoneWeight = {1, 3, 5};
    private AtomicInteger currentPaletteNumber = new AtomicInteger(1);
    private AtomicInteger currentPaletteWeight = new AtomicInteger(0);
    private int combination;

    private GridPane palletGrid;
    private Text paletteCounter;
    private Text weightCounter;
    Spinner<Integer> workerSpinner;
    private ExecutorService executorService;
    private Random random = new Random();
    private final Lock lock = new ReentrantLock();
    private Semaphore firstBlock;
    private Semaphore secondBlock;
    private AtomicBoolean isChanged = new AtomicBoolean(false);
    private CyclicBarrier cyclicBarrier1;
    private CyclicBarrier cyclicBarrier2;

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

        Image backgroundImage = new Image(getClass().getResourceAsStream("quarryOpacity2.png"));

        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,  // Powtarzanie obrazka w poziomie
                BackgroundRepeat.NO_REPEAT,  // Powtarzanie obrazka w pionie
                BackgroundPosition.DEFAULT,  // Pozycja obrazka
                new BackgroundSize(
                        BackgroundSize.DEFAULT.getWidth(),
                        BackgroundSize.DEFAULT.getHeight(),
                        true,   // Szerokość w procentach
                        true,   // Wysokość w procentach
                        false,  // Zachować proporcje
                        true)   // Dopasować do kontenera
        );
        root.setBackground(new Background(background));

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
        paletteCounter = new Text("Paleta: " + currentPaletteNumber.get() + ", max waga: " + palleteMaxWeight[currentPaletteNumber.get() - 1]);
        paletteCounter.setStyle("-fx-font-size: 16px;");
        weightCounter = new Text("Aktualna waga palety: " + currentPaletteWeight.get());
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
        firstBlock = new Semaphore(workerSpinner.getValue());
        secondBlock = new Semaphore(workerSpinner.getValue());
        cyclicBarrier1 = new CyclicBarrier(workerSpinner.getValue());
        cyclicBarrier2 = new CyclicBarrier(workerSpinner.getValue());
        combination = random.nextInt(0, 3);
        int paletteIndex = currentPaletteNumber.get() - 1;
        tempDecisions = decisions[paletteIndex][combination].clone();

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
        }
    }

    private void sleepSomeTime() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void stopWorkers() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    private void addStoneToPalette() {
        lock.lock();
        int stone = getStoneNumber();
        try {
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
                currentPaletteWeight.addAndGet(stoneWeight[stone - 1]);
                Platform.runLater(() -> addStone(row, column, stone, color));
            }
        } finally {
            lock.unlock();
        }
        sleepSomeTime();
        sleepSomeTime();
        if (stone == 0) {
            try {
                cyclicBarrier1.await();
                lock.lock();
                if (!isChanged.get()) {
                    nextPaletteSetup();
                    resetGrid();
                    isChanged.set(true);
                }
                lock.unlock();
                cyclicBarrier2.await();
                isChanged.set(false);
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
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
                if (!cellOccupied[i][j] && (3 - i >= stoneLength)) {
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

    private void addStone(int row, int column, int length, Color color) {
        for (int i = 0; i < length; i++) {
            Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);
            rect.setFill(color);
            rect.setTranslateY(WINDOW_HEIGHT);

            ImageView imageView = new ImageView(
                    new Image(
                            getClass().getResourceAsStream("rock.png")
                    )
            );
            imageView.setFitHeight(CELL_SIZE);
            imageView.setFitWidth(CELL_SIZE);
            imageView.setTranslateY(WINDOW_HEIGHT);

            palletGrid.add(imageView, column, row + i);
            weightCounter.setText("Aktualna waga palety: " + currentPaletteWeight.get());
            paletteCounter.setText("Paleta: " + currentPaletteNumber.get() + ", max waga: " + palleteMaxWeight[currentPaletteNumber.get() - 1]);

            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(imageView.translateYProperty(), 0);
            KeyFrame kf = new KeyFrame(Duration.seconds(2), kv);
            timeline.getKeyFrames().add(kf);
            timeline.play();
        }
    }

    private void resetGrid() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                cellOccupied[i][j] = false;
            }
        }
        Platform.runLater(() -> {
            palletGrid.getChildren().clear();
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    Pane cell = new Pane();
                    cell.setMinSize(CELL_SIZE, CELL_SIZE);
                    cell.setStyle("-fx-border-color: black;");
                    palletGrid.add(cell, j, i);
                }
            }
        });
    }

    private void nextPaletteSetup() {
        currentPaletteNumber.incrementAndGet();
        if (currentPaletteNumber.get() > PALETTE_LIMIT) {
            currentPaletteNumber.set(1);
        }
        currentPaletteWeight.set(0);
        Platform.runLater(() -> {
            paletteCounter.setText("Paleta: " + currentPaletteNumber.get() + ", max waga: " + palleteMaxWeight[currentPaletteNumber.get() - 1]);
            weightCounter.setText("Aktualna waga palety: " + currentPaletteWeight.get());
        });

        int paletteIndex = currentPaletteNumber.get() - 1;
//        System.out.println("Uzyskany indeks palety: " + paletteIndex);
        combination = random.nextInt(0, 3);  // Losujemy nową kombinację dla nowej palety
//        System.out.println("Wylosowana kombinacja: " + combination);
        tempDecisions = decisions[paletteIndex][combination].clone();
//        System.out.println("Oryginał cały: " + Arrays.deepToString(decisions));
//        System.out.println("Oryginał: " + Arrays.toString(decisions[paletteIndex][combination]));
//        System.out.println("W temp: " + Arrays.toString(tempDecisions));
    }

    private void resetButton() {
        stopWorkers();
        currentPaletteNumber.set(1);
        currentPaletteWeight.set(0);
        resetGrid();
        Platform.runLater(() -> {
            paletteCounter.setText("Paleta: " + currentPaletteNumber.get() + ", max waga: " + palleteMaxWeight[currentPaletteNumber.get() - 1]);
            weightCounter.setText("Aktualna waga palety: " + currentPaletteWeight.get());
        });
        stopWorkers();
    }

    public static void main(String[] args) {
        launch();
    }
}
