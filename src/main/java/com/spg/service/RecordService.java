package com.spg.service;

import com.spg.bean.MetaInfo;
import com.spg.bean.Result;
import com.spg.dao.CSVModifier;

import java.io.IOException;
import java.util.*;

public class RecordService {

    private final CSVModifier<Result> resultCSVModifier = new CSVModifier<>(Result.class);
    private final CSVModifier<MetaInfo> metaInfoCSVModifier = new CSVModifier<>(MetaInfo.class);
    private final int K = 5;

    public RecordService() throws Exception {
    }

    public MetaInfo getMetaInfo() {
        List<MetaInfo> data = metaInfoCSVModifier.getData();
        if (data.isEmpty())
            return new MetaInfo();
        return data.get(0);
    }

    public void setMetaInfo(MetaInfo metaInfo) {
        List<MetaInfo> data = metaInfoCSVModifier.getData();
        if (data.isEmpty()) {
            data.add(metaInfo);
        } else {
            data.set(0, metaInfo);
        }
    }

    public void addSuccess(Result result) {
        resultCSVModifier.getData().add(result);
        MetaInfo metaInfo = getMetaInfo();
        metaInfo.setSuccess(metaInfo.getSuccess() + 1);
        if (metaInfo.getCurrStreak() >= 0) {
            metaInfo.setCurrStreak(metaInfo.getCurrStreak() + 1);
            metaInfo.setMaxWinStreak(Integer.max(metaInfo.getMaxWinStreak(), metaInfo.getCurrStreak()));
        } else {
            metaInfo.setCurrStreak(1);
        }
        setMetaInfo(metaInfo);
    }

    public void addMistake() {
        MetaInfo metaInfo = getMetaInfo();
        metaInfo.setMistake(metaInfo.getMistake() + 1);
        if (metaInfo.getCurrStreak() <= 0) {
            metaInfo.setCurrStreak(metaInfo.getCurrStreak() - 1);
            metaInfo.setMaxLossStreak(Integer.max(metaInfo.getMaxLossStreak(), metaInfo.getCurrStreak()));
        } else {
            metaInfo.setCurrStreak(-1);
        }
        setMetaInfo(metaInfo);
    }

    public void addBadluck() {
        MetaInfo metaInfo = getMetaInfo();
        metaInfo.setBadluck(metaInfo.getBadluck() + 1);
        if (metaInfo.getCurrStreak() <= 0) {
            metaInfo.setCurrStreak(metaInfo.getCurrStreak() - 1);
            metaInfo.setMaxLossStreak(Integer.max(metaInfo.getMaxLossStreak(), metaInfo.getCurrStreak()));
        } else {
            metaInfo.setCurrStreak(-1);
        }
        setMetaInfo(metaInfo);
    }

    public void close() throws IOException, IllegalAccessException {
        metaInfoCSVModifier.close();
        resultCSVModifier.close();
    }

    private List<Result> getMax(Comparator<Result> comparator) {
        List<Result> results = resultCSVModifier.getData();
        Queue<Result> queue = new PriorityQueue<>(comparator.reversed());
        for (Result result : results) {
            if (queue.size() < K) {
                queue.offer(result);
                continue;
            }
            int compare = comparator.compare(queue.peek(), result);
            if (compare > 0) {
                queue.poll();
                queue.offer(result);
            }
        }
        ArrayList<Result> result = new ArrayList<>(queue);
        result.sort(comparator);
        return result;
    }

    public List<Result> getMinElapsed() {
        Comparator<Result> resultComparator = Comparator.comparing(Result::getElapsed);
        return getMax(resultComparator);
    }

    public List<Result> getMaxThreeBVPS() {
        Comparator<Result> resultComparator = Comparator.comparing(Result::getThreeBVPS).reversed();
        return getMax(resultComparator);
    }

    public static void main(String[] args) throws Exception {
        RecordService recordService = new RecordService();
        List<Result> minElapsed = recordService.getMinElapsed();
        List<Result> maxThreeBVPS = recordService.getMaxThreeBVPS();
        System.out.println();
    }

}
