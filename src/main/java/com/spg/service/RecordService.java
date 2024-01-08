package com.spg.service;

import com.spg.bean.MetaInfo;
import com.spg.bean.Result;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class RecordService {
    private final MetaInfoService metaInfoService = new MetaInfoService();
    private final ResultService resultService = new ResultService();

    public RecordService() throws Exception {
    }

    public void addSuccess(Result result) throws IOException, InvocationTargetException, IllegalAccessException {
        resultService.save(result);
//        metaInfoService.
    }

    public void addMistake() {

    }

    public void addBadluck() {

    }

    public List<Result> getMinElapsed() {
        return null;
    }

    public List<Result> getMaxThreeBVPS() {
        return null;
    }

    public MetaInfo getMetaInfo() {
        return null;
    }
}
