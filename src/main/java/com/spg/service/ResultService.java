package com.spg.service;

import com.spg.bean.Result;
import com.spg.dao.CSVModifier;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;

public class ResultService {
    private final CSVModifier<Result> csvModifier = new CSVModifier<>(Result.class, "history.csv");

    public ResultService() throws NoSuchMethodException {
        String[] getNames = {"getThreeBV", "getThreeBVPS", "getElapsed", "getDate"};
        String[] setNames = {"setThreeBVFromString", null, "setElapsedFromString", "setDateFromString"};
        String[] chNames = {"3bv", "3bv/s", "时间", "日期"};
        csvModifier.setChNames(chNames);
        csvModifier.setSetMethods(setNames);
        csvModifier.setGetMethods(getNames);
    }

    public void save(Result result) throws IOException, InvocationTargetException, IllegalAccessException {
        csvModifier.save(result);
    }


    public static void main(String[] args) throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException {
        ResultService resultDao = new ResultService();
        resultDao.save(new Result(10, 10., LocalDate.now()));
    }
}
