package com.spg;

public class Pos {
    public final int r;
    public final int c;

    public Pos(int r, int c) {
        this.r = r;
        this.c = c;
    }

    @Override
    public int hashCode() {
        return r * 1000 + c;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Pos pos = (Pos) obj;
        return r == pos.r && c == pos.c;
    }

    @Override
    public String toString() {
        return "Pos{" +
                +r +
                "," + c +
                '}';
    }
}

