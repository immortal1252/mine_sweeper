package com.spg;

import java.util.ArrayList;
import java.util.List;

public class ClickStatus {
    private final List<Pos> cell2update = new ArrayList<>();
    private final List<Integer> cellCls = new ArrayList<>();

    private boolean fail;

    ClickStatus() {
    }

    public void add(Pos pos, Integer cls) {
        cell2update.add(pos);
        cellCls.add(cls);
    }

    public void addAll(List<Pos> posList, List<Integer> clsList) {
        cell2update.addAll(posList);
        cellCls.addAll(clsList);
    }

    public List<Pos> getCell2update() {
        return cell2update;
    }

    public List<Integer> getCellCls() {
        return cellCls;
    }

    public boolean isFail() {
        return fail;
    }

    public void setFail(boolean fail) {
        this.fail = fail;
    }
}
