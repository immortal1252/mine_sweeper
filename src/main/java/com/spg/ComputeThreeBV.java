package com.spg;

import java.util.HashSet;
import java.util.Set;

class UnionFind {
    final int[] fa;

    UnionFind(int n) {
        fa = new int[n];
        for (int i = 0; i < n; i++) {
            fa[i] = i;
        }
    }

    int find(int x) {
        int fx = fa[x];
        if (fx == x)
            return x;
        return fa[x] = find(fx);
    }

    void union(int x, int y) {
        x = fa[x];
        y = fa[y];
        fa[x] = y;
    }

}

public class ComputeThreeBV {
    private final int[][] board;

    public ComputeThreeBV(int[][] board) {
        this.board = board;
    }

    private int tuple2int(int i, int j) {
        return i * board[0].length + j;
    }

    private boolean isConnected(int i, int j, int[][] next) {
        for (int[] nextT : next) {
            int nextR = i + nextT[0];
            int nextC = j + nextT[1];
            if (nextR < 0 || nextR >= board.length || nextC < 0 || nextC >= board[0].length || board[i][j] == 9)
                continue;
            if (board[nextR][nextC] == 0)
                return true;
        }
        return false;
    }


    public int compute() {
        UnionFind unionFind = new UnionFind(board.length * board[0].length);

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == 9) {
                    unionFind.fa[tuple2int(i, j)] = -1;
                    continue;
                }
                boolean right = j + 1 < board[0].length && board[i][j + 1] != 9;
                boolean bot = i + 1 < board.length && board[i + 1][j] != 9;
                //right
                if (right) {
                    int[][] next = {{-1, 0}, {-1, -1}, {1, 0}, {1, 1}};
                    boolean connect = isConnected(i, j, next);
                    if (connect) {
                        unionFind.union(tuple2int(i, j), tuple2int(i, j + 1));
                    }
                }
                //bot
                if (bot) {
                    int[][] next = {{0, -1}, {1, -1}, {0, 1}, {1, 1}};
                    boolean connect = isConnected(i, j, next);
                    if (connect) {
                        unionFind.union(tuple2int(i, j), tuple2int(i + 1, j));
                    }
                }
                //right,bot
                if (right && bot) {
                    int[][] next = {{1, 0}, {0, 1}};
                    boolean connect = isConnected(i, j, next);
                    if (connect) {
                        unionFind.union(tuple2int(i, j), tuple2int(i + 1, j + 1));
                    }
                }
            }
        }

        Set<Integer> integers = new HashSet<>();
        for (int i = 0; i < unionFind.fa.length; i++) {
            if (unionFind.fa[i] == -1)
                continue;
            integers.add(unionFind.find(i));
        }
        return integers.size();
    }
}
