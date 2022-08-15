package export;

import com.dpg7.main.FileProcessor;
import com.dpg7.main.Globals;
import com.dpg7.main.State;
import com.dpg7.main.Table;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.*;
import java.util.stream.Collectors;

public class ExportDatabase {
    private String schemaName;

    private State state;

    private List<LinkedHashMap<String, String>> globalMeta;

    List<String> primaryKeyColumnList;
    List<String> uniqueKeyColumnList;
    List<String> foreignKeyColumnList;

    HashMap<String, LinkedHashMap<String, HashMap<String, String>>> tableMetaHashMap;
    HashMap<String, List<String>> tableDataHashMap;

    public ExportDatabase() {
        state = State.getInstance();
        FileProcessor fp = new FileProcessor(false);

        globalMeta = fp.readGlobalMeta();

        // Remove first row that contains headers
        globalMeta.remove(0);

        primaryKeyColumnList = new ArrayList<>();
        uniqueKeyColumnList = new ArrayList<>();
        foreignKeyColumnList = new ArrayList<>();

        tableMetaHashMap = new HashMap<>();
        tableDataHashMap = new HashMap<>();
    }

    public void handleExportDB() {
        Boolean areInputsValid = handleUserInputs();

        if (areInputsValid) {
            List<LinkedHashMap<String, String>> filteredGlobalMeta = globalMeta.stream()
                    .filter(row -> {
                        if (row.size() != 3) {
                            return false;
                        }
                        return row.get("schema").equalsIgnoreCase(schemaName) && !row.get("table").isEmpty();
                    })
                    .collect(Collectors.toList());

            // List<LinkedHashMap<String, String>> uniqueFilteredGlobalMeta = new ArrayList<>();
            //
            // int sizeOfFilteredGlobalMeta = filteredGlobalMeta.size();
            //
            // for (int i = 0; i < ; i++) {
            //
            // }
            // filteredGlobalMeta = filteredGlobalMeta.stream().filter()

            loadMetaAndDataForAllTables(filteredGlobalMeta);

            int size = filteredGlobalMeta.size();

            // HashMap<String, String> dependencyHashMap = new HashMap<>();
            // List<String> independentTableList = new ArrayList<>();

            // for (int i = 0; i < size; i++) {
            //     String tName = filteredGlobalMeta.get(i).get("table");
            //     LinkedHashMap<String, HashMap<String, String>> tMeta = tableMetaHashMap.get(tName);
            //
            //     Boolean isTableIndependent = true;
            //     String [] tColumns = tMeta.keySet().toArray(new String[0]);
            //     for (String col: tColumns) {
            //         HashMap<String, String> colInfoMap = tMeta.get(col);
            //         String keyInfo = colInfoMap.get("key");
            //
            //         // If KEY column contains something
            //         if (keyInfo.trim().length() > 0) {
            //             // If key column contains something other than PRIMARY KEY
            //             if (!keyInfo.trim().equalsIgnoreCase(Globals.PRIMARY_KEY)) {
            //                 dependencyHashMap.put(tName, keyInfo);
            //                 isTableIndependent = false;
            //             }
            //         }
            //     }
            //
            //     if (isTableIndependent) {
            //         independentTableList.add(tName);
            //     }
            // }

            // List<String> sortedTableList = new TopologicalSort(dependencyHashMap).sort();
            // independentTableList.addAll(sortedTableList);

            ArrayList<String> uniqueTables = new ArrayList<>();

            String sqlDump = "";
            for (int i = 0; i < size; i++) {

                String tableName = filteredGlobalMeta.get(i).get("table");

                if (uniqueTables.contains(tableName)) {
                    continue;
                }
                uniqueTables.add(tableName);

                String dropQuery = String.format("DROP TABLE IF EXISTS `%s`;\n", tableName);
                String createQuery = handleCreateQuery(tableName);
                String lockTable = String.format("LOCK TABLES `%s` WRITE;\n", tableName);
                String insertQuery = handleInsertQuery(tableName);
                String unlockTables = "UNLOCK TABLES;\n";

                sqlDump += dropQuery + createQuery + lockTable + insertQuery + unlockTables + "\n\n";
            }

            generateSQLFile(sqlDump);
        }
    }

    private void generateSQLFile(String sqlContent) {
        String dumpFileName = schemaName + "_Dump" + ".sql";
        String filePath = Globals.GET_ROOT() + File.separator + dumpFileName;

        FileWriter fileWriter = null;
        try {
            File SQLFile = new File(filePath);
            if (!SQLFile.exists()) {
                Path newFilePath = Paths.get(filePath);
                Files.createFile(newFilePath);
            }

            fileWriter = new FileWriter(SQLFile);
            fileWriter.write(sqlContent);
            fileWriter.close();
        } catch (IOException e) {
            // TODO :: JAY :: LOG THIS INFORMATION
            e.printStackTrace();
        }
    }

    private void loadMetaAndDataForAllTables(List<LinkedHashMap<String, String>> filteredGlobalMeta) {
        int size = filteredGlobalMeta.size();

        ArrayList<String> uniqueTables = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            String tableName = filteredGlobalMeta.get(i).get("table");

            if (uniqueTables.contains(tableName)) {
                continue;
            }

            uniqueTables.add(tableName);

            Boolean isTableInCurrentVM = filteredGlobalMeta.get(i).get("instance").equalsIgnoreCase(State.getInstance().getCurrentVM());

            FileProcessor fileProcessor = new FileProcessor(!isTableInCurrentVM);

            LinkedHashMap<String, HashMap<String, String>> tableMetaData = fileProcessor.readTableMeta(tableName, schemaName);
            Table table = fileProcessor.loadTable(schemaName, tableName);

            tableMetaHashMap.put(tableName, tableMetaData);
            tableDataHashMap.put(tableName, table.stringifyTable());
        }
    }

    private String handleCreateQuery(String tableName) {
        String createQuery = "CREATE TABLE `" + tableName + "` (\n";
        createQuery += handleColumns(tableName);

        //      PRIMARY KEY (`columnName`),
        if (primaryKeyColumnList.size() > 0) {
            createQuery += ",\n";
            createQuery += handlePrimaryKey();
        }

        //      UNIQUE KEY `UK_ob8kqyqqgmefl0aco34akdtpe` (`columnName`),
        if (uniqueKeyColumnList.size() > 0) {
            createQuery += ",\n";
            createQuery += handleUniqueKey();
        }

        //      KEY `FK77ouh4l5k8uxf1sjkh26q8574` (`columnName`),
        //      CONSTRAINT `FK77ouh4l5k8uxf1sjkh26q8574` FOREIGN KEY (`columnName`) REFERENCES `anotherTableName` (`columnName`)
        if (foreignKeyColumnList.size() > 0) {
            createQuery += ",\n";
            createQuery += handleForeignKey();
        }

        createQuery += "\n);\n";

        return createQuery;
    }

    private String handleInsertQuery(String tableName) {
        String insertQuery = String.format("INSERT INTO `%s` VALUES ", tableName);

        List<String> tableData = tableDataHashMap.get(tableName);

        LinkedHashMap<String, HashMap<String, String>> tMeta = tableMetaHashMap.get(tableName);
        String [] tColumns = tMeta.keySet().toArray(new String[0]);

        ArrayList<String> columnDataTypeList = new ArrayList<>();
        for (int i = 0; i < tColumns.length; i++) {
            columnDataTypeList.add(tMeta.get(tColumns[i]).get("dataType"));
        }

        int size = tableData.size();

        for (int i = 0; i < size; i++) {
            if (i > 0) {
                String data = "(";

                String[] splittedRow = tableData.get(i).split(Globals.GET_SEPARATOR());

                int len = splittedRow.length;
                for (int j = 0; j < len; j++) {

                    // wrap data with double quotes if datatype is TEXT
                    if (columnDataTypeList.get(j).equalsIgnoreCase("TEXT")) {
                        data += String.format("'%s'", splittedRow[j]);
                    } else {
                        data += splittedRow[j];
                    }

                    data += (j == (len - 1)) ? "" : ", ";
                }

                data += (i == (size - 1)) ? ");" : "), ";

                insertQuery += data;
            }
        }

        return insertQuery + "\n";
    }

    private String handleColumns(String tableName) {
        LinkedHashMap<String, HashMap<String, String>> tableMetaData = tableMetaHashMap.get(tableName);

        String [] columns = tableMetaData.keySet().toArray(new String[0]);
        int columnCount = columns.length;

        String columnString = "";
        for (int j = 0; j < columnCount; j++) {
            HashMap<String, String> columnMeta = tableMetaData.get(columns[j]);

            String columnName = columnMeta.get("columnName");
            String dataType = columnMeta.get("dataType");
            String key = columnMeta.get("key");
            String unique = columnMeta.get("unique");

            String singleColumnString = "";
            if (j == (columnCount - 1)) {
                singleColumnString = String.format("\t`%s` %s", columnName, dataType);
            } else {
                singleColumnString = String.format("\t`%s` %s,\n", columnName, dataType);
            }

            columnString += singleColumnString;

            if (!key.trim().isEmpty()) {
                if (key.equalsIgnoreCase(Globals.PRIMARY_KEY)) {
                    primaryKeyColumnList.add(columnName);
                } else {
                    foreignKeyColumnList.add(columnName + Globals.GET_SEPARATOR() + tableName);
                }
            }

            if (!unique.trim().isEmpty() && unique.equalsIgnoreCase(Globals.TRUE)) {
                uniqueKeyColumnList.add(columnName);
            }
        }

        return columnString;
    }

    private String handlePrimaryKey() {
        String pkString = "\tPRIMARY KEY (";

        int len = primaryKeyColumnList.size();
        for (int j = 0; j < len; j++) {
            String columnName = primaryKeyColumnList.get(j);

            if (j == (len - 1)) {
                pkString += String.format("`%s`)", columnName);
            } else {
                pkString += String.format("`%s`, ", columnName);
            }
        }

        return pkString;
    }

    private String handleUniqueKey() {
        String uniqueKeyString = "";

        int len = uniqueKeyColumnList.size();
        for (int j = 0; j < len; j++) {
            String ukUUID = "UK_" + UUID.randomUUID().toString();
            String columnName = uniqueKeyColumnList.get(j);

            if (j == (len - 1)) {
                uniqueKeyString += String.format("\tUNIQUE KEY `%s` (`%s`)", ukUUID, columnName);
            } else {
                uniqueKeyString += String.format("\tUNIQUE KEY `%s` (`%s`),\n", ukUUID, columnName);
            }
        }

        return uniqueKeyString;
    }

    private String handleForeignKey() {
        String foreignKeyString = "";

        int len = foreignKeyColumnList.size();
        for (int j = 0; j < len; j++) {
            String fkUUID = "FK" + UUID.randomUUID().toString();
            String columnName = foreignKeyColumnList.get(j).split(Globals.GET_SEPARATOR())[0];
            String anotherTableName = foreignKeyColumnList.get(j).split(Globals.GET_SEPARATOR())[1];

            if (j == (len - 1)) {
                foreignKeyString += String.format("KEY `%s` (`%s`),", fkUUID, columnName);
                foreignKeyString += String.format("CONSTRAINT `%s` FOREIGN KEY (`%s`) REFERENCES `%s` (`%s`)", fkUUID, columnName, anotherTableName, columnName);
            } else {
                foreignKeyString += String.format("KEY `%s` (`%s`),", fkUUID, columnName);
                foreignKeyString += String.format("CONSTRAINT `%s` FOREIGN KEY (`%s`) REFERENCES `%s` (`%s`),", fkUUID, columnName, anotherTableName, columnName);
            }
        }

        return foreignKeyString;
    }

    private Boolean handleUserInputs() {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter the name of database: ");
        String dbName = sc.nextLine();
        System.out.println();

        long entriesWithSameName = globalMeta.stream().filter(row -> row.get("schema").equalsIgnoreCase(dbName)).count();
        if (entriesWithSameName == 0) {
            // TODO :: JAY :: LOG THIS INFORMATION
            System.err.println("Database with name: '" + dbName + "' doesn't exist.");
            return false;
        }

        // schemaName = dbName.toLowerCase();
        schemaName = dbName;

        return true;
    }
}
