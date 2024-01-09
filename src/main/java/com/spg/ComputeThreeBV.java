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
        x = find(x);
        y = find(y);
        fa[x] = y;
    }

}

public class ComputeThreeBV {
    private final int[][] board;
    private final int[][] color;

    public ComputeThreeBV(int[][] board) {
        this.board = board;
        color = new int[board.length][board[0].length];
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
                    boolean connected;
                    if (board[i][j] == 0 || board[i][j + 1] == 0) {
                        connected = true;
                    } else {
                        int[][] next = {{-1, 0}, {-1, 1}, {1, 0}, {1, 1}};
                        connected = isConnected(i, j, next);
                    }
                    if (connected) {
                        unionFind.union(tuple2int(i, j), tuple2int(i, j + 1));
                    }
                }
                //bot
                if (bot) {
                    boolean connected;
                    if (board[i][j] == 0 || board[i + 1][j] == 0) {
                        connected = true;
                    } else {
                        int[][] next = {{0, -1}, {1, -1}, {0, 1}, {1, 1}};
                        connected = isConnected(i, j, next);
                    }
                    if (connected) {
                        unionFind.union(tuple2int(i, j), tuple2int(i + 1, j));
                    }
                }
                //right,bot
                if (right && bot) {
                    boolean connected;
                    if (board[i][j] == 0 || board[i + 1][j + 1] == 0) {
                        connected = true;
                    } else {
                        int[][] next = {{1, 0}, {0, 1}};
                        connected = isConnected(i, j, next);
                    }
                    if (connected) {
                        unionFind.union(tuple2int(i, j), tuple2int(i + 1, j + 1));
                    }
                }
            }
        }

        Set<Integer> integers = new HashSet<>();
        for (int i = 0; i < unionFind.fa.length; i++) {
            if (unionFind.fa[i] == -1)
                continue;
//            System.out.println(i + ":" + unionFind.find(i));
            integers.add(unionFind.find(i));
        }
        return integers.size();
    }

    private void dfs(int r, int c, int cnt) {
        color[r][c] = cnt;
        final int[][] next = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
        for (int[] nextT : next) {
            int nextR = r + nextT[0];
            int nextC = c + nextT[1];
            if (nextR < 0 || nextR >= board.length || nextC < 0 || nextC >= board[0].length)
                continue;
            if (board[nextR][nextC] == 9 || color[nextR][nextC] != 0)
                continue;
            if (board[r][c] != 0 && board[nextR][nextC] != 0)
                continue;
            dfs(nextR, nextC, cnt);
        }
    }

    public int dfsCompute() {
        int cnt = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == 9 || color[i][j] != 0)
                    continue;
                dfs(i, j, ++cnt);
            }
        }
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                color[i][j] = 0;
            }
        }
        return cnt;
    }

    public static void main(String[] args) {
        Game game = new Game(100, 100, 2000);
        game.init(new Pos(9, 12));

        ComputeThreeBV computeThreeBV = new ComputeThreeBV(game.board);
        long start, end, t, val;

        start = System.nanoTime();
        val = computeThreeBV.dfsCompute();
        end = System.nanoTime();
        t = end - start;
        System.out.println(t + "," + val);

        start = System.nanoTime();
        val = computeThreeBV.dfsCompute();
        end = System.nanoTime();
        t = end - start;
        System.out.println(t + "," + val);

        start = System.nanoTime();
        val = computeThreeBV.compute();
        end = System.nanoTime();
        t = end - start;
        System.out.println(t + "," + val);

    }
}
