package com.dpg7.main.ERDiagram;

import com.dpg7.main.FileProcessor;
import com.dpg7.main.Schema;
import com.dpg7.main.Table;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

// Generates ERDiagram from a given database name
public class ERDiagram {

    public static String generateERD(String schemaName) throws IllegalAccessException {
        // Validate schema
        if (schemaName == null || schemaName.length() == 0) {
            return "";
        }

        // Get the schema from remote and local
        FileProcessor localFileProcessor = new FileProcessor(false);
        FileProcessor remoteFileProcessor = new FileProcessor(true);
        Schema localSchema = localFileProcessor.loadSchema(schemaName);
        Schema remoteSchema = remoteFileProcessor.loadSchema(schemaName);

        // Create a new String Builder for output
        StringBuilder output = new StringBuilder();
        output.append(String.format("SCHEMA:\t%s\n", schemaName));

        // Retrieve table data from schema
        Map<String, Table> allTablesMap;
        Map<String, Table> localTablesMap = localSchema.getSchema();
        Map<String, Table> remoteTablesMap = remoteSchema.getSchema();

        // Create an all tables map by merging local and remote tables
        allTablesMap = new HashMap<>(localTablesMap);
        for (String table : remoteTablesMap.keySet()) {
            allTablesMap.put(table, remoteTablesMap.get(table));
        }

        // Append all table names
        output.append(
                String.format("TABLES:\t %s \n",
                        new ArrayList(allTablesMap.keySet()).toString().replaceAll("\\[|\\]", "").replaceAll(".txt",
                                "")));

        // Process each table, append metadata and find cardinality relationships, if
        // any
        StringBuilder tableMetadata = new StringBuilder();
        StringBuilder tableCardinality = new StringBuilder().append("\nCARDINALITY\n");
        FileProcessor currFileProcessor;
        for (String tableName : allTablesMap.keySet()) {
            tableMetadata.append(String.format("\n%s\n", tableName.replaceAll(".txt", "")));

            if (localTablesMap.containsKey(tableName)) {
                currFileProcessor = localFileProcessor;
            } else {
                currFileProcessor = remoteFileProcessor;
            }
            tableMetadata.append(stringifyTableMetadata(
                    currFileProcessor.readTableMeta(tableName.replaceAll(".txt", ""), schemaName)));
            tableCardinality
                    .append(getTableCardinalities(
                            currFileProcessor.readTableMeta(tableName.replaceAll(".txt", ""), schemaName), tableName));
        }

        // Append the results
        output.append(tableMetadata.toString());
        output.append(tableCardinality.toString());

        // Write ERD to text file
        try {
            writeErdToFile(output.toString());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error while writing ERD");
        }

        System.out.println(output.toString());

        return output.toString();
    }

    private static String stringifyTableMetadata(LinkedHashMap<String, HashMap<String, String>> meta) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("%s, %s, %s, %s, %s\n", "columnName", "dataType", "size", "key", "unique"));
        StringBuilder currentRow;
        for (String column : meta.keySet()) {
            currentRow = new StringBuilder();
            HashMap<String, String> columnMeta = meta.get(column);
            currentRow.append(
                    String.format("%s, %s, %s, %s, %s\n", column, columnMeta.get("dataType"), columnMeta.get("Size"),
                            columnMeta.get("key"), columnMeta.get("unique")));
            stringBuilder.append(String.format("%s", currentRow.toString()));
        }
        return stringBuilder.toString();
    }

    private static String getTableCardinalities(LinkedHashMap<String, HashMap<String, String>> meta, String tableName) {
        StringBuilder cardinalityRow = new StringBuilder();

        HashMap<String, String> columnMeta;
        for (String column : meta.keySet()) {
            columnMeta = meta.get(column);
            if (columnMeta.get("key").length() > 0 && !columnMeta.get("key").equals("PRIMARY KEY")
                    && !columnMeta.get("key").equalsIgnoreCase("null")) {
                // There's a foreign key, check for unique
                if (columnMeta.get("unique").equalsIgnoreCase("true")) {
                    // Table1(FK) --> Table2()
                    cardinalityRow
                            .append(String.format("%s, %s, %s, %s\n", columnMeta.get("key"), tableName, column,
                                    "one-to-one"));
                } else if (columnMeta.get("unique").equalsIgnoreCase("null")) {
                    cardinalityRow
                            .append(String.format("%s, %s, %s, %s\n", columnMeta.get("key"), tableName, column,
                                    "one-to-many"));
                }
            }
        }

        return cardinalityRow.toString();
    }

    public static void writeErdToFile(String input) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("ERD.txt"));
        writer.write(input);
        writer.close();
    }
}
