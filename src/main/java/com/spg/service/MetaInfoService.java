package com.spg.service;

import com.spg.bean.MetaInfo;
import com.spg.dao.CSVModifier;

import java.util.List;

public class MetaInfoService {
    private final CSVModifier<MetaInfo> csvModifier = new CSVModifier<>(MetaInfo.class, "meta.csv");

    public MetaInfoService() throws Exception {
        String[] chNames = {"失误次数", "运气次数", "胜利次数", "最高连胜", "最高连败", "当前连胜"};
        String[] getMethods = {"getMistake", "getBadluck", "getSuccess", "getMaxWinStreak", "getMaxLossStreak", "getCurr"};
        String[] setMethods = {"setMistakeFromString", "setBadluckFromString"};
        csvModifier.setChNames(chNames);
        csvModifier.setGetMethods(getMethods);
        csvModifier.setSetMethods(setMethods);
    }

    public MetaInfo getLast() throws Exception {
        List<MetaInfo> history = csvModifier.read();
        int mistake;
        int badluck;
        if (history == null || history.isEmpty()) {
            mistake = 0;
            badluck = 0;
        } else {
            MetaInfo metaInfo = history.get(0);
            mistake = metaInfo.getMistake();
            badluck = metaInfo.getBadluck();
        }
        return new MetaInfo();
    }

    public void addMistake() throws Exception {
        MetaInfo history = getLast();
        history.setMistake(history.getMistake() + 1);
        csvModifier.save(history);
    }

    public void addBadluck() throws Exception {
        MetaInfo history = getLast();
        history.setBadluck(history.getBadluck() + 1);
        csvModifier.save(history);
    }

    public static void main(String[] args) throws Exception {
        MetaInfoService metaInfoDao = new MetaInfoService();
        metaInfoDao.addBadluck();
    }
}
