package com.spg;

import java.util.HashMap;
import java.util.Map;

public class ClickStatus {
    private final Map<Pos, Integer> cell2update = new HashMap<>();

    private boolean fail;

    ClickStatus() {
    }


    public void put(Pos pos, Integer cls) {
        cell2update.put(pos, cls);
    }

    public void put(ClickStatus another) {
        cell2update.putAll(another.getCell2update());
    }


    public Map<Pos, Integer> getCell2update() {
        return cell2update;
    }

    public boolean isFail() {
        return fail;
    }

    public void setFail(boolean fail) {
        this.fail = fail;
    }
}
