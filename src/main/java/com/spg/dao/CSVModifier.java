package com.spg.dao;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CSVModifier<T> {
    Class<T> clazz;
    private final String savePath;
    private String[] chNames;
    //中文名
    private final List<Method> setMethods = new ArrayList<>();
    //set方法
    private final List<Method> getMethods = new ArrayList<>();
    //get方法
    private final Logger logger = LogManager.getLogger();


    public CSVModifier(Class<T> clazz, String savePath) {
        this.clazz = clazz;
        this.savePath = savePath;
    }

    public void save(T data) throws IOException, InvocationTargetException, IllegalAccessException {
        Path path = Paths.get(savePath);
        boolean exist = true;
        //创建文件
        if (Files.notExists(path)) {
            Files.createFile(path);
            exist = false;
        }
        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(savePath, true))) {
            //写入表头
            if (!exist) {
                csvWriter.writeNext(chNames);
            }
            //写入数据
            List<String> row = new ArrayList<>();
            for (Method getMethod : getMethods) {
                String value = getMethod.invoke(data).toString();
                row.add(value);
            }
            csvWriter.writeNext(row.toArray(new String[0]));
        }
    }

    public List<T> read() throws Exception {
        Path path = Paths.get(savePath);
        if (Files.notExists(path))
            return null;
        List<T> datas = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(savePath))) {
            csvReader.readNext();
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                T t = clazz.getConstructor().newInstance();
                for (int i = 0; i < setMethods.size(); i++) {
                    setMethods.get(i).invoke(t, row[i]);
                }
                datas.add(t);
            }
        }
        return datas;
    }

    public void setChNames(String[] chNames) {
        this.chNames = chNames;
    }

    public void setSetMethods(String[] setMethods) throws NoSuchMethodException {
        for (String setMethod : setMethods) {
            if (setMethod == null) {
                this.setMethods.add(null);
                continue;
            }
            Method method = clazz.getDeclaredMethod(setMethod, String.class);
            this.setMethods.add(method);
        }
    }

    public void setGetMethods(String[] getMethods) throws NoSuchMethodException {
        for (String getMethod : getMethods) {
            if (getMethod == null) {
                this.getMethods.add(null);
                continue;
            }
            Method method = clazz.getDeclaredMethod(getMethod);
            this.getMethods.add(method);
        }
    }

//    public static void main(String[] args) throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException {
//        BaseDao<Result> resultBaseDao = new BaseDao<>(Result.class, "history.csv");
//        String[] getNames = {"getThreeBV", "getThreeBVPS", "getElapsed", "getDate"};
//        String[] setNames = {"setThreeBVFromString", null, "setElapsedFromString", "setDateFromString"};
//        String[] chNames = {"3bv", "3bv/s", "时间", "日期"};
//        resultBaseDao.setChNames(chNames);
//        resultBaseDao.setSetMethods(setNames);
//        resultBaseDao.setGetMethods(getNames);
//        resultBaseDao.save(new Result(30, 20.0, LocalDate.now()));
//    }
}
