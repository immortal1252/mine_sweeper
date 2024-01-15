package com.spg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Auto {

    class MineSet {
        Pos pos;
        int ukMineNum;
        //未知雷个数
        Set<Pos> ukSet = new HashSet<>();
        // 未知格子集合

        public MineSet(Pos pos) {
            this.pos = new Pos(pos.r, pos.c);
            this.ukMineNum = openedBoard[pos.r][pos.c];
            for (Pos posT : game.get_surround(pos)) {
                if (openedBoard[posT.r][posT.c] == -1) {
                    this.ukSet.add(posT);
                } else if (openedBoard[posT.r][posT.c] == 9) {
                    ukMineNum--;
                }
            }
        }
    }

    private Game game;

    public void setGame(Game game) {
        this.game = game;
    }

    private int[][] openedBoard;
    // -1是未知,-2是可以点开

    public Auto(Game game) {
        this.game = game;
    }


    private void updateOpenBoard() {
        this.openedBoard = game.getKnown();
    }

    public List<Pos> check() {
        updateOpenBoard();
        while (true) {
            boolean change = checkOnce();
            if (!change)
                break;
        }
        List<Pos> ret = new ArrayList<>();
        for (int i = 0; i < game.getNumHeight(); i++) {
            for (int j = 0; j < game.getNumWidth(); j++) {
                if (openedBoard[i][j] == -2) {
                    ret.add(new Pos(i, j));
                }
            }
        }
        return ret;
    }

    private boolean checkOnce() {
        for (int i = 0; i < game.getNumHeight(); i++) {
            for (int j = 0; j < game.getNumWidth(); j++) {
                if (openedBoard[i][j] < 1 || openedBoard[i][j] > 8)
                    continue;
                MineSet mineSet = new MineSet(new Pos(i, j));
                if (mineSet.ukSet.isEmpty())
                    continue;
                boolean change = singleSet(mineSet);
                if (change)
                    return true;
                change = doubleSet(mineSet);
                if (change)
                    return true;
            }
        }
        return false;
    }


    private boolean singleSet(MineSet mineSet) {
        boolean change = false;
        //未知全是雷
        if (mineSet.ukMineNum == mineSet.ukSet.size()) {
            for (Pos posT : mineSet.ukSet) {
                change = true;
                openedBoard[posT.r][posT.c] = 9;
            }
        }
        // 未知全不是雷
        if (mineSet.ukMineNum == 0) {
            for (Pos posT : mineSet.ukSet) {
                change = true;
                openedBoard[posT.r][posT.c] = -2;
            }
        }
        return change;
    }

    private boolean doubleSet(MineSet mineSet) {
        int[][] next = {{0, 1}, {1, 0}, {1, 1}};
        for (int[] nextT : next) {
            Pos pos = mineSet.pos;
            int nextR = pos.r + nextT[0];
            int nextC = pos.c + nextT[1];
            if (nextR < 0 || nextR >= game.getNumHeight() || nextC < 0 || nextC >= game.getNumWidth() ||
                    openedBoard[nextR][nextC] < 1 || openedBoard[nextR][nextC] > 8)
                continue;
            MineSet mineSet1 = new MineSet(new Pos(nextR, nextC));
            if (mineSet.ukMineNum == mineSet1.ukMineNum) {
                if (equalFormula(mineSet, mineSet1))
                    return true;
            } else {
                if (subFormula(mineSet, mineSet1))
                    return true;
            }
        }
        return false;
    }

    private boolean subFormula(MineSet a, MineSet b) {
        boolean change = false;
        // a<b
        if (a.ukMineNum > b.ukMineNum) {
            MineSet t = a;
            a = b;
            b = t;
        }
        Set<Pos> b_a = new HashSet<>(b.ukSet);
        b_a.removeAll(a.ukSet);
        if (b_a.size() == (b.ukMineNum - a.ukMineNum)) {
            for (Pos posT : b_a) {
                change = true;
                openedBoard[posT.r][posT.c] = 9;
            }
            Set<Pos> a_b = new HashSet<>(a.ukSet);
            a_b.removeAll(b.ukSet);
            for (Pos posT : a_b) {
                change = true;
                openedBoard[posT.r][posT.c] = -2;
            }
        }
        return change;
    }

    private boolean equalFormula(MineSet a, MineSet b) {
        boolean change = false;
        Set<Pos> a_b = new HashSet<>(a.ukSet);
        a_b.removeAll(b.ukSet);
        Set<Pos> b_a = new HashSet<>(b.ukSet);
        b_a.removeAll(a.ukSet);
        if (a_b.isEmpty()) {
            for (Pos posT : b_a) {
                change = true;
                openedBoard[posT.r][posT.c] = -2;
            }
        }
        if (b_a.isEmpty()) {
            for (Pos posT : a_b) {
                change = true;
                openedBoard[posT.r][posT.c] = -2;
            }
        }
        return change;
    }


    public static void main(String[] args) {
        Logger logger = LogManager.getLogger();
        Game game = new Game(5, 5, 1);
        try {
//            int[][] board = {{1, 1, 1, 1, 1}, {9, 2, 1, 9, 1}, {9, 2, 2, 3, 3}, {1, 1, 2, 9, 9}, {0, 0, 2, 9, 9}};
            Field boardField = game.getClass().getDeclaredField("board");
            boardField.setAccessible(true);
            int[][] board = {{1, 1, 1, 1, 1}, {9, 1, 1, 9, 1}, {1, 1, 1, 1, 1}, {0, 1, 1, 1, 0}, {0, 1, 9, 1, 0}};
            boolean[][] known = new boolean[board.length][board[0].length];
            known[0] = new boolean[]{true, true, false, true, false};
            boardField.set(game, board);
            Field statusField = game.getClass().getDeclaredField("status");
            statusField.setAccessible(true);
            Game.Status[][] status = new Game.Status[board.length][board[0].length];
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[0].length; j++) {
                    status[i][j] = known[i][j] ? Game.Status.OPENED : Game.Status.UK;
                }
            }
            statusField.set(game, status);
        } catch (Exception e) {
            logger.error("{}", e.getMessage(), e);
        }
//        HashSet<String> strings = new HashSet<>();
//        for (String t : strings) {

    }
}
