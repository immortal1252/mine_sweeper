package com.spg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class Game {
    public enum Status {
        UK(10), FLAG(11), OPENED(12);

        Status(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        private final int id;
    }

    private final int numWidth;
    private final int numHeight;
    private final int numMine;
    private int safeGridLeft;
    private final int[][] board;
    private final Status[][] status;

    private final int[][] next = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};

    public int pos2id(Pos pos) {
        return pos.r * numWidth + pos.c;
    }

    public Pos id2pos(int id) {
        return new Pos(id / numWidth, id % numWidth);
    }

    public List<Pos> get_surround(Pos pos) {
        List<Pos> ret = new ArrayList<>();
        for (int[] nextRC : next) {
            int nextR = pos.r + nextRC[0];
            int nextC = pos.c + nextRC[1];
            if (nextR < 0 || nextR >= this.numHeight || nextC < 0 || nextC >= this.numWidth) {
                continue;
            }
            ret.add(new Pos(nextR, nextC));
        }
        return ret;
    }

    public Game(int numWidth, int numHeight, int mineTotal) {
        this.numWidth = numWidth;
        this.numHeight = numHeight;
        this.numMine = mineTotal;
        this.board = new int[numHeight][numWidth];
        this.status = new Status[numHeight][numWidth];
        for (int i = 0; i < status.length; i++) {
            for (int j = 0; j < status[0].length; j++) {
                this.status[i][j] = Status.UK;
            }
        }
        this.safeGridLeft = numHeight * numWidth - mineTotal;
    }

    public void init(Pos pos) {
        Random random = new Random();
        HashSet<Integer> mineSet = new HashSet<>();
        List<Integer> safe = new ArrayList<>();
        for (Pos posT : get_surround(pos)) {
            safe.add(pos2id(posT));
        }
        safe.add(pos2id(pos));
        while (mineSet.size() < numMine) {
            int mineId = random.nextInt(numHeight * numWidth);
            if (safe.contains(mineId))
                continue;
            mineSet.add(mineId);
        }
        for (int mindId : mineSet) {
            Pos posT = id2pos(mindId);
            board[posT.r][posT.c] = 9;
        }
        for (int r = 0; r < numHeight; r++) {
            for (int c = 0; c < numWidth; c++) {
                if (board[r][c] == 9) continue;
                int mine = 0;
                for (Pos posT : get_surround(new Pos(r, c))) {
                    if (board[posT.r][posT.c] == 9) {
                        mine++;
                    }
                }
                board[r][c] = mine;
            }
        }
    }


    public ClickStatus flag(Pos pos) {
        ClickStatus clickStatus = new ClickStatus();
        int r = pos.r;
        int c = pos.c;
        if (status[r][c] != Status.FLAG && status[r][c] != Status.UK) {
            return clickStatus;
        }
        status[r][c] = status[r][c] == Status.FLAG ? Status.UK : Status.FLAG;
        clickStatus.put(pos, status[r][c].getId());
        return clickStatus;
    }

    public ClickStatus pressOne(Pos pos) {
        ClickStatus clickStatus = new ClickStatus();
        int r = pos.r;
        int c = pos.c;
        //skip opened grid
        if (status[r][c] != Status.UK) {
            return clickStatus;
        }
        clickStatus.put(pos, 0);
        return clickStatus;
    }


    public ClickStatus pressNine(Pos pos) {
        ClickStatus clickStatus = new ClickStatus();
        //skip opened grid
        List<Pos> surround = get_surround(pos);
        surround.add(pos);
        for (Pos posT : surround) {
            ClickStatus clickStatusT = pressOne(posT);
            clickStatus.put(clickStatusT);
        }
        return clickStatus;
    }

    public ClickStatus openOne(Pos pos) {
        System.out.println("open" + pos);
        ClickStatus clickStatus = new ClickStatus();
        int r = pos.r;
        int c = pos.c;
        if (status[r][c] != Status.UK) {
            return clickStatus;
        }
        //open current grid
        status[r][c] = Status.OPENED;
        clickStatus.put(pos, board[r][c]);
        boolean fail = board[r][c] == 9;
        clickStatus.setFail(fail);
        if (!fail) {
            safeGridLeft--;
        }
        if (board[r][c] != 0) {
            return clickStatus;
        }
        //recur neibor grid
        for (Pos posT : get_surround(pos)) {
            ClickStatus clickStatusT = openOne(posT);
            clickStatus.put(clickStatusT);
        }
        return clickStatus;
    }

    public ClickStatus openNine(Pos pos) {
        ClickStatus clickStatus = new ClickStatus();
        if (status[pos.r][pos.c] != Status.OPENED) {
            return clickStatus;
        }
        List<Pos> surround = get_surround(pos);
        int mine = 0;
        for (Pos posT : surround) {
            if (status[posT.r][posT.c] == Status.FLAG) {
                mine++;
            }
        }
        boolean fail = false;
        if (mine == board[pos.r][pos.c]) {
            for (Pos posT : surround) {
                ClickStatus clickStatusT = openOne(posT);
                clickStatus.put(clickStatusT);
                fail |= clickStatus.isFail();
            }
        }
        clickStatus.setFail(fail);
        return clickStatus;
    }

    public int[][] getKnown() {
        // 对机器扫雷暴露的接口,将打开的格子暴露,其余全部为-1
        int[][] ret = new int[numHeight][numWidth];
        for (int i = 0; i < numHeight; i++) {
            for (int j = 0; j < numWidth; j++) {
                ret[i][j] = status[i][j] != Status.OPENED ? -1 : board[i][j];
            }
        }
        return ret;
    }

    public int getSafeGridLeft() {
        return safeGridLeft;
    }

    public boolean success() {
        return safeGridLeft == 0;
    }

    public int getNumWidth() {
        return numWidth;
    }

    public int getNumHeight() {
        return numHeight;
    }

    public int getNumMine() {
        return numMine;
    }
}


