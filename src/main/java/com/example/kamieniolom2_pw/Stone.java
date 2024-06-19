package com.example.kamieniolom2_pw;

import javafx.scene.paint.Color;

public class Stone {
    private final int weight;
    private final Color color;

    public Stone(int weight, Color color) {
        this.weight = weight;
        this.color = color;
    }

    public int getWeight() {
        return weight;
    }

    public Color getColor() {
        return color;
    }
}
