package com.dpg7.main;

import java.util.*;

public class Table {
    private List<LinkedHashMap<String, String>> records;
    private String schemaName;

    public Table(List<LinkedHashMap<String, String>> tableRecords, String schemaName) {
        records = tableRecords;
        this.schemaName = schemaName;
    }

    public List<LinkedHashMap<String, String>> getRecords() {
        return records;
    }

    public void insertRecord(LinkedHashMap<String, String> newRecord) {
        newRecord.put("_id", UUID.randomUUID().toString());
        records.add(newRecord);
    }

    public List<String> stringifyTable() {
        List<String> returnVal = new ArrayList<>();

        for (LinkedHashMap<String, String> record : records) {
            StringJoiner sb = new StringJoiner("\t");
            for (var entry : record.values()) {
                sb.add(entry);
            }
            returnVal.add(sb.toString());
        }
        return returnVal;
    }

}
