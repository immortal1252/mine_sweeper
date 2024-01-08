package com.spg.bean;

public class MetaInfo {

    private Integer mistake;
    private Integer badluck;
    private Integer success;
    private Integer maxWinStreak;
    private Integer maxLossStreak;
    private Integer currStreak;

    public Integer getMaxWinStreak() {
        return maxWinStreak;
    }

    public void setMaxWinStreak(Integer maxWinStreak) {
        this.maxWinStreak = maxWinStreak;
    }

    public Integer getMaxLossStreak() {
        return maxLossStreak;
    }

    public void setMaxLossStreak(Integer maxLossStreak) {
        this.maxLossStreak = maxLossStreak;
    }

    public Integer getCurrStreak() {
        return currStreak;
    }

    public void setCurrStreak(Integer currStreak) {
        this.currStreak = currStreak;
    }

    public MetaInfo() {
    }

    public Integer getSuccess() {
        return success;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

    public Integer getMistake() {
        return mistake;
    }

    public void setMistake(Integer mistake) {
        this.mistake = mistake;
    }

    public void setBadluck(Integer badluck) {
        this.badluck = badluck;
    }


    public Integer getBadluck() {
        return badluck;
    }

}
