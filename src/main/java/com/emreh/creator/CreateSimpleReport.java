package com.emreh.creator;

import com.emreh.enums.ExportType;
import com.emreh.exception.CreateReportException;
import com.emreh.model.ColumnModel;
import com.emreh.model.ColumnModelDetails;
import lombok.AccessLevel;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

@Getter
public class CreateSimpleReport<T> {

    private List<T> dataList;
    private ColumnModel columnModel;
    @Getter(AccessLevel.NONE)
    private final List<String> header = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    private final List<List<String>> columns = new ArrayList<>();
    @Getter(AccessLevel.NONE)
    private final List<List<String>> columnsExport = new ArrayList<>();

    public CreateSimpleReport<T> setModel(List<T> modelList) {
        this.dataList = modelList;
        return this;
    }

    public CreateSimpleReport<T> setDetails(ColumnModel columnModel) {
        this.columnModel = columnModel;
        return this;
    }

    public CreateSimpleReport<T> builder() {
        Field[] allFields = this.dataList.get(0).getClass().getDeclaredFields();

        createHeader(allFields, this.columnModel);
        createColumn(allFields, this.dataList, this.columnModel);

        columnsExport.add(this.header);
        columnsExport.addAll(this.columns);
        return this;
    }

    public Object getExportFileReport(ExportType exportType, String name) {
        if (exportType == null)
            throw new CreateReportException("The Input Parameter Cannot Be Null");

        CreatorSimpleExcel creatorSimpleExcel = new CreatorSimpleExcel();

        return creatorSimpleExcel.createExportFromList(this.columnsExport, exportType, name != null ? name : "Created By Report Recipient", columnModel);
    }

    /**
     * Create Column
     *
     * @param allFields
     * @param data
     * @param columnModel
     */
    private void createColumn(Field[] allFields, List<T> data, ColumnModel columnModel) {
        data.forEach(d -> {
            Class<?> clazz = d.getClass();
            List<String> col = new ArrayList<>();
            AtomicReference<List<List<String>>> resp = new AtomicReference<>();
            AtomicInteger columnCounter = new AtomicInteger(1);

            List<ColumnModelDetails> model = columnModel.getColumnModelDetailsList();
            for (Field field : allFields) {
                field.setAccessible(true);
                model.forEach(m -> {
                    if (!m.getFieldName().contains(".") &&
                            m.getFieldName().equals(field.getName()) &&
                            !Collection.class.isAssignableFrom(field.getType())) {
                        try {
                            col.add((String) field.get(d));
                            columnCounter.getAndIncrement();
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (m.getFieldName().contains(".") && Collection.class.isAssignableFrom(field.getType())) {

                        Set<Object> subList = new HashSet<>();
                        Stream.of(clazz.getDeclaredFields())
                                .map(f -> "get" + capitalize(field.getName()))
                                .map(getterName -> {
                                    try {
                                        return clazz.getMethod(getterName);
                                    } catch (NoSuchMethodException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                                .map(getterMethod -> {
                                    try {
                                        return getterMethod.invoke(d);
                                    } catch (IllegalAccessException | InvocationTargetException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                                .forEach(subList::add);

                        try {
                            resp.set(createNestedColumn(field.getName(), Class.forName(getClassNameFromCollectionField(field)).getDeclaredFields(), columnCounter, subList, model));
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
            columns.add(col);

            if (resp.get() != null)
                columns.addAll(resp.get());
        });
    }

    /**
     * Create Nested Column & Create Empty Column
     *
     * @param parentName
     * @param subAllFields
     * @param columnCounter
     * @param subList
     * @param model
     * @return
     */
    private List<List<String>> createNestedColumn(String parentName, Field[] subAllFields, AtomicInteger columnCounter, Set<Object> subList, List<ColumnModelDetails> model) {
        List<List<String>> localColumns = new ArrayList<>();
        subList.forEach(sub -> {
            ((List<?>) sub).forEach(s -> {

                Class<?> clazz = s.getClass();
                List<String> col = new ArrayList<>();
                AtomicReference<List<List<String>>> resp = new AtomicReference<>();

                for (int i = 0; i < columnCounter.get(); i++)
                    col.add("");

                AtomicInteger localCounter = new AtomicInteger(columnCounter.get() + 1);

                model.forEach(m -> {
                    Arrays.stream(subAllFields).forEach(field -> {
                        if (m.getFieldName().contains(".") && m.getFieldName().equals(parentName + "." + field.getName()) &&
                                !Collection.class.isAssignableFrom(field.getType())) {
                            try {
                                field.setAccessible(true);
                                col.add((String) field.get(s));
                                localCounter.getAndIncrement();
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        } else if (m.getFieldName().contains(".") && m.getFieldName().equals(parentName + "." + field.getName()) &&
                                Collection.class.isAssignableFrom(field.getType())) {
                            Set<Object> subListLoop = new HashSet<>();

                            Stream.of(clazz.getDeclaredFields())
                                    .map(f -> "get" + capitalize(field.getName()))
                                    .map(getterName -> {
                                        try {
                                            return clazz.getMethod(getterName);
                                        } catch (NoSuchMethodException e) {
                                            throw new RuntimeException(e);
                                        }
                                    })
                                    .map(getterMethod -> {
                                        try {
                                            return getterMethod.invoke(s);
                                        } catch (IllegalAccessException | InvocationTargetException e) {
                                            throw new RuntimeException(e);
                                        }
                                    })
                                    .forEach(subListLoop::add);
                            try {
                                resp.set(createNestedColumn(parentName + "." + field.getName(), Class.forName(getClassNameFromCollectionField(field)).getDeclaredFields(), localCounter, subListLoop, model));
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                });

                localColumns.add(col);

                if (resp.get() != null)
                    localColumns.addAll(resp.get());
            });
        });

        return localColumns;
    }

    /**
     * Create Header
     *
     * @param allFields
     * @param columnModel
     */
    private void createHeader(Field[] allFields, ColumnModel columnModel) {
        AtomicInteger headerCounter = new AtomicInteger(0);

        Integer size = columnModel.getColumnModelDetailsList().size();
        List<ColumnModelDetails> model = columnModel.getColumnModelDetailsList();
        for (int i = 0; i < size; i++) {
            for (Field field : allFields) {
                // Field Only
                if (!model.get(i).getFieldName().contains(".") &&
                        field.getName().equals(model.get(i).getFieldName()) && i == headerCounter.get()) {
                    header.add(model.get(i).getTitle());
                    indexing(model.get(i).getFieldName(), headerCounter);
                    headerCounter.getAndIncrement();
                }
                // Child Of Collection
                else if (model.get(i).getFieldName().contains(".") && Collection.class.isAssignableFrom(field.getType())) {
                    createNestedHeader(field.getName(), model, headerCounter);
                    headerCounter.getAndIncrement();
                }
                // Class Of Collection
                else if (model.get(i).getFieldName().contains(".") && Collection.class.isAssignableFrom(field.getType())) {
                    createNestedHeader(field.getName(), model, headerCounter);
                    headerCounter.getAndIncrement();
                }
            }
        }
    }

    /**
     * Child Of createHeader Function, Create Nested Header
     *
     * @param className
     * @param columnModelDetailsList
     * @param headerCounter
     */
    private void createNestedHeader(String className, List<ColumnModelDetails> columnModelDetailsList, AtomicInteger headerCounter) {
        for (int i = 0; i < columnModelDetailsList.size(); i++)
            if (columnModelDetailsList.get(i).getFieldName().startsWith(getClassNameFromPackage(className))
                    && i == headerCounter.get()) {
                header.add(columnModelDetailsList.get(i).getTitle());
                indexing(columnModelDetailsList.get(i).getFieldName(), headerCounter);
            }
    }

    /**
     * For Find Index Of Header
     *
     * @param filedName
     * @param headerCounter
     */
    private void indexing(String filedName, AtomicInteger headerCounter) {
        columnModel.getColumnModelDetailsList().forEach(merge -> {
            if (merge.getFieldName().equals(filedName)) {
                merge.setIndex(headerCounter.get());
            }
        });
    }

    private String getClassNameFromPackage(String className) {
        String[] bits = className.split("[.]");
        return bits[bits.length - 1];
    }

    private String getClassNameFromCollectionField(Field field) {
        ParameterizedType genericType = (ParameterizedType) field.getGenericType();
        Type type = genericType.getActualTypeArguments()[0];

        return type.getTypeName();
    }

    private static String capitalize(String s) {
        if (s.length() == 0) return s;

        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
