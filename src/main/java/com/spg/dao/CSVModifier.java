package com.spg.dao;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.spg.anno.DataAnno;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

public class CSVModifier<T> {
    private final Class<T> clazz;
    private final String savePath;

    //字符串映射到字段上
    private final Map<String, Field> name2field = new HashMap<>();
    //数据视图
    private final List<T> data = new ArrayList<>();

    public CSVModifier(Class<T> clazz) throws Exception {
        this.clazz = clazz;
        DataAnno clazzAnnotation = clazz.getAnnotation(DataAnno.class);
        this.savePath = clazzAnnotation.name();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            DataAnno annotation = field.getAnnotation(DataAnno.class);
            if (annotation == null)
                continue;
            field.setAccessible(true);
            String name = annotation.name();
            name2field.put(name, field);
        }
        loadData();
    }

    public List<T> getData() {
        return data;
    }

    private void setByName(T obj, String name, String value) throws IllegalAccessException {
        Field field = name2field.get(name);
        if (field == null) {
            System.out.printf("%s not exist%n", name);
            return;
        }
        Class<?> type = field.getType();
        if (type == Integer.class) {
            int valueInt = Integer.parseInt(value);
            field.set(obj, valueInt);
        } else if (type == Double.class) {
            double valueDouble = Double.parseDouble(value);
            field.set(obj, valueDouble);
        } else if (type == LocalDate.class) {
            LocalDate valueDate = LocalDate.parse(value);
            field.set(obj, valueDate);
        }
    }

    private String getByName(T obj, String name) throws IllegalAccessException {
        Field field = name2field.get(name);
        if (field == null) {
            System.out.printf("%s not exist%n", name);
            return null;
        }
        Object value = field.get(obj);
        return String.valueOf(value);
    }

    private void loadData() throws Exception {
        System.out.println("load");
        Path path = Paths.get(savePath);
        //创建文件
        if (Files.notExists(path)) {
            return;
        }

        try (CSVReader csvReader = new CSVReader(new FileReader(savePath))) {
            String[] names = csvReader.readNext();
            if (!Arrays.equals(names, name2field.keySet().toArray(new String[0]))) {
                return;
            }
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                T t = clazz.getDeclaredConstructor().newInstance();
                for (int i = 0; i < row.length; i++) {
                    setByName(t, names[i], row[i]);
                }
                data.add(t);
            }
        }

    }


    //写回文件
    public void close() throws IOException, IllegalAccessException {
        Path path = Paths.get(savePath);
        if (Files.notExists(path)) {
            Files.createFile(path);
        }
        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(savePath))) {
            String[] names = name2field.keySet().toArray(new String[0]);
            csvWriter.writeNext(names);
            for (T t : data) {
                String[] row = new String[names.length];
                for (int i = 0; i < names.length; i++) {
                    row[i] = getByName(t, names[i]);
                }
                csvWriter.writeNext(row);
            }
        }
    }

}
