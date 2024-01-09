package com.spg.bean;

import com.spg.anno.DataAnno;

import java.time.LocalDate;


@DataAnno(name = "history.csv")
public class Result {
    @DataAnno(name = "3bv")
    private Integer threeBV;
    @DataAnno(name = "时间")
    private Double elapsed;
    @DataAnno(name = "日期")
    private LocalDate date;
    @DataAnno(name = "3bv/s")
    private Double threeBVPS;

    @Override
    public String toString() {
        return "{3bv=" + threeBV +
                ", elapsed=" + elapsed +
                ", date=" + date +
                ", 3bv/s=%.2f".formatted(threeBVPS) +
                '}';
    }

    public Result() {
    }

    public Result(Integer threeBV, Double elpased, LocalDate date) {
        this.threeBV = threeBV;
        this.elapsed = elpased;
        this.date = date;
        this.threeBVPS = threeBV / elpased;
    }

    public void setThreeBV(Integer threeBV) {
        this.threeBV = threeBV;
    }

    public void setElapsed(Double elapsed) {
        this.elapsed = elapsed;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setThreeBVPS(Double threeBVPS) {
        this.threeBVPS = threeBVPS;
    }

    public Double getThreeBVPS() {
        return this.threeBVPS;
    }


    public Integer getThreeBV() {
        return threeBV;
    }

    public Double getElapsed() {
        return elapsed;
    }

    public LocalDate getDate() {
        return date;
    }


}
