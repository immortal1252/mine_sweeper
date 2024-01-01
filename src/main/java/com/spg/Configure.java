package com.spg;

import java.util.Map;

public class Configure {

    private int numWidth;
    private int numHeight;
    private int cellSize;
    private int numMine;

    private Map<Integer, String> id2name;

    public int getNumWidth() {
        return numWidth;
    }

    public Map<Integer, String> getId2name() {
        return id2name;
    }

    public void setId2name(Map<Integer, String> id2name) {
        this.id2name = id2name;
    }

    public void setNumWidth(int numWidth) {
        this.numWidth = numWidth;
    }

    public int getNumHeight() {
        return numHeight;
    }

    public void setNumHeight(int numHeight) {
        this.numHeight = numHeight;
    }

    public int getCellSize() {
        return cellSize;
    }

    public void setCellSize(int cellSize) {
        this.cellSize = cellSize;
    }

    public int getNumMine() {
        return numMine;
    }

    public void setNumMine(int numMine) {
        this.numMine = numMine;
    }
}
