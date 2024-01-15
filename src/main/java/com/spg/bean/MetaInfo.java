package com.spg.bean;

import com.spg.anno.DataAnno;

@DataAnno(name = "meta.csv")
public class MetaInfo {

    @DataAnno(name = "失误次数")
    private Integer mistake = 0;
    @DataAnno(name = "运气次数")
    private Integer badluck = 0;
    @DataAnno(name = "胜场")
    private Integer success = 0;
    @DataAnno(name = "最大连胜")
    private Integer maxWinStreak = 0;
    @DataAnno(name = "最大连败")
    private Integer maxLossStreak = 0;
    @DataAnno(name = "当前连胜")
    private Integer currStreak = 0;


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
