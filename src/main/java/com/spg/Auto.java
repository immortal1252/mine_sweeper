package com.spg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Auto {
    private final Logger logger = LogManager.getLogger();

    class MineSet {
        int id;
        int ukMineNum;
        //未知雷个数
        Set<Integer> ukSet = new HashSet<>();
        // 该集合的UK格子id

        public MineSet(Pos pos) {
            this.id = game.pos2id(pos);
            this.ukMineNum = openedBoard[pos.r][pos.c];
            for (Pos posT : game.get_surround(pos)) {
                if (openedBoard[posT.r][posT.c] == -1) {
                    this.ukSet.add(game.pos2id(posT));
                } else if (openedBoard[posT.r][posT.c] == 9) {
                    ukMineNum--;
                }
            }
        }
    }

    private final Game game;

    private int[][] openedBoard;

    public Auto(Game game) {
        this.game = game;
    }

    private ClickStatus markMine() {
        // noflag标雷
        ClickStatus clickStatus = new ClickStatus();
        for (int i = 0; i < game.getNumHeight(); i++) {
            for (int j = 0; j < game.getNumWidth(); j++) {
                if (openedBoard[i][j] < 1 || openedBoard[i][j] > 8)
                    continue;
                MineSet mineSet = new MineSet(new Pos(i, j));
                ClickStatus clickStatusT = singleSet(mineSet);
                clickStatus.put(clickStatusT);
            }
        }
        return clickStatus;
    }

    public ClickStatus check() {
        if (openedBoard == null) {
            openedBoard = game.getKnown();
        }
        ClickStatus clickStatus = new ClickStatus();
        while (true) {
            ClickStatus clickStatusT = checkOnce();
            int oldSize = clickStatus.getCell2update().size();
            clickStatus.put(clickStatusT);
            int newSize = clickStatus.getCell2update().size();
            if (newSize == oldSize)
                break;
        }
        return clickStatus;
    }

    public ClickStatus checkOnce() {
        for (int i = 0; i < game.getNumHeight(); i++) {
            for (int j = 0; j < game.getNumWidth(); j++) {
                if (openedBoard[i][j] < 1 || openedBoard[i][j] > 8)
                    continue;
                MineSet mineSet = new MineSet(new Pos(i, j));
                ClickStatus clickStatus = singleSet(mineSet);
                if (!clickStatus.getCell2update().isEmpty())
                    return clickStatus;
                clickStatus = doubleSet(mineSet);
                if (!clickStatus.getCell2update().isEmpty()) {
                    return clickStatus;
                }
                //TODO TripleSet
            }
        }
        return new ClickStatus();
    }

    private ClickStatus singleSet(MineSet mineSet) {
        ClickStatus clickStatus = new ClickStatus();
        //未知全是雷
        if (mineSet.ukMineNum == mineSet.ukSet.size()) {
            for (int id : mineSet.ukSet) {
                Pos pos = game.id2pos(id);
                openedBoard[pos.r][pos.c] = 9;
            }
        }
        // 未知全不是雷
        if (mineSet.ukMineNum == 0) {
            for (int id : mineSet.ukSet) {
                Pos pos = game.id2pos(id);
                clickStatus.put(pos, 12);
            }
        }
        return clickStatus;
    }

    private ClickStatus doubleSet(MineSet mineSet) {
        ClickStatus clickStatus = new ClickStatus();
        int[][] next = {{0, 1}, {1, 0}, {1, 1}};
        for (int[] nextT : next) {
            Pos pos = game.id2pos(mineSet.id);
            MineSet mineSet1 = new MineSet(new Pos(pos.r + nextT[0], pos.c + nextT[1]));
            ClickStatus clickStatusT;
            if (mineSet.ukMineNum == mineSet1.ukMineNum) {
                clickStatusT = equalFormula(mineSet, mineSet1);
            } else {
                clickStatusT = subFormula(mineSet, mineSet1);
            }
            clickStatus.put(clickStatusT);
        }
        return clickStatus;
    }

    private ClickStatus subFormula(MineSet a, MineSet b) {
        // a<b
        ClickStatus clickStatus = new ClickStatus();
        if (a.ukMineNum > b.ukMineNum) {
            MineSet t = a;
            a = b;
            b = t;
        }
        Set<Integer> b_a = new HashSet<>(b.ukSet);
        b_a.removeAll(a.ukSet);
        if (b_a.size() == (b.ukMineNum - a.ukMineNum)) {
            for (int id : b_a) {
                Pos pos = game.id2pos(id);
                openedBoard[pos.r][pos.c] = 9;
            }
            Set<Integer> a_b = new HashSet<>(a.ukSet);
            a_b.removeAll(b.ukSet);
            for (int id : a_b) {
                Pos pos = game.id2pos(id);
                clickStatus.put(pos, 12);
            }
        }
        return clickStatus;
    }

    private ClickStatus equalFormula(MineSet a, MineSet b) {
        ClickStatus clickStatus = new ClickStatus();
        Set<Integer> a_b = new HashSet<>(a.ukSet);
        a_b.removeAll(b.ukSet);
        Set<Integer> b_a = new HashSet<>(b.ukSet);
        b_a.removeAll(a.ukSet);
        if (a_b.isEmpty()) {
            for (int id : b_a) {
                Pos pos = game.id2pos(id);
                clickStatus.put(pos, 12);
            }
        }
        if (b_a.isEmpty()) {
            for (int id : a_b) {
                Pos pos = game.id2pos(id);
                clickStatus.put(pos, 12);
            }
        }
        return clickStatus;
    }

    public static void main(String[] args) {
        Logger logger = LogManager.getLogger();
        Game game = new Game(4, 2, 1);
        Auto auto = new Auto(game);
        try {
//            int[][] board = {{1, 1, 1, 1, 1}, {9, 2, 1, 9, 1}, {9, 2, 2, 3, 3}, {1, 1, 2, 9, 9}, {0, 0, 2, 9, 9}};
            Field boardField = game.getClass().getDeclaredField("board");
            boardField.setAccessible(true);
            int[][] board = {{1, 2, 2, 1}, {-1, -1, -1, -1}};
            boardField.set(game, board);
            Field statusField = game.getClass().getDeclaredField("status");
            statusField.setAccessible(true);
            Game.Status[][] status = new Game.Status[board.length][board[0].length];
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[0].length; j++) {
                    status[i][j] = board[i][j] == -1 ? Game.Status.UK : Game.Status.OPENED;
                }
            }
            statusField.set(game, status);
        } catch (Exception e) {
            logger.error("{}", e.getMessage(), e);
        }
        ClickStatus clickStatus = auto.check();
        for (Map.Entry<Pos, Integer> entry : clickStatus.getCell2update().entrySet()) {
            System.out.println(entry.getKey() + "," + entry.getValue());
        }
    }
}
