package com.spg.bean;

import java.text.DecimalFormat;
import java.time.LocalDate;


public class Result {
    private Integer threeBV;
    private Double elapsed;
    private LocalDate date;
    private static final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    @Override
    public String toString() {
        return "Result{" +
                "threeBV=" + threeBV +
                ", elapsed=" + elapsed +
                ", date=" + date +
                '}';
    }

    Result() {
    }

    public Result(Integer threeBV, Double elpased, LocalDate date) {
        this.threeBV = threeBV;
        this.elapsed = elpased;
        this.date = date;
    }

    public Double getThreeBVPS() {
        return threeBV / elapsed;
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

    public void setThreeBVFromString(String threeBV) {
        this.threeBV = Integer.parseInt(threeBV);
    }


    public void setElapsedFromString(String elapsed) {
        this.elapsed = Double.parseDouble(elapsed);
    }


    public void setDateFromString(String date) {
        this.date = LocalDate.parse(date);
    }

}
