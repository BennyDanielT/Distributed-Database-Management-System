package QueryExecution;

import QueryParsing.Query;
import com.dpg7.main.FileProcessor;
import com.dpg7.main.Globals;
import com.dpg7.main.Table;

import java.util.*;

public class MetadataValidation {
    public FileProcessor LOCAL_FILE_PROCESSOR = new FileProcessor(false);
    public FileProcessor REMOTE_FILE_PROCESSOR = new FileProcessor(true);

    public boolean validateSchema(String schemaName) {
        List<LinkedHashMap<String, String>> globalMeta = LOCAL_FILE_PROCESSOR.readGlobalMeta();
        for (LinkedHashMap<String, String> entry : globalMeta) {
            if (entry.containsValue(schemaName))
                return true;
        }
        return false;
    }

    public int getInstanceNumber(String tableName, String schemaName) {
        if (!validateSchema(schemaName))
            return -1;
        List<LinkedHashMap<String, String>> globalMeta = LOCAL_FILE_PROCESSOR.readGlobalMeta();
        for (LinkedHashMap<String, String> entry : globalMeta) {
            if (entry.get("table") == null) {
                continue;
            }
            if (entry.get("table").equals(tableName)) {
                return Integer.parseInt(entry.get("instance"));
            }
        }
        return -1;
    }

    public int validateTable(String tableName, String schemaName) {
        return getInstanceNumber(tableName, schemaName);
    }

    public boolean validateSingleColumn(String columnName, String columnInsertValue,
            LinkedHashMap<String, HashMap<String, String>> meta, Table table) {
        if (!meta.containsKey(columnName)) {
            return false;
        }

        String dataType = meta.get(columnName).get("dataType");

        if (dataType.equals("FLOAT")) {
            try {
                float temp = Float.parseFloat(columnInsertValue);
            } catch (NumberFormatException ex) {
                return false;
            }
        }

        List<LinkedHashMap<String, String>> tableRecords = table.getRecords();
        for (LinkedHashMap<String, String> entry : tableRecords) {
            if (entry.get(columnName).equals(columnInsertValue)) {
                return true;
            }
        }
        System.out.println("FK Constraint failed!");
        return false;
    }

    public boolean validateColumns(List<String> columns, LinkedHashMap<String, HashMap<String, String>> localMeta) {
        for (String column : columns) {
            if (!localMeta.containsKey(column)) {
                if (!column.equals("ID")) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean validateInsertDataTypes(List<List<String>> inserts,
            LinkedHashMap<String, HashMap<String, String>> localMeta, List<String> columns) {
        for (List<String> vals : inserts) {
            if (columns.size() != vals.size()) {
                System.out.println("Insert Size != Column Size");
                return false;
            }

            for (int i = 0; i < vals.size(); i++) {
                String col = columns.get(i);
                String val = vals.get(i);
                String dataType = localMeta.get(col).get("dataType");

                if (dataType.equals("FLOAT")) {
                    try {
                        float integer = Float.parseFloat(val);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean validateUniqueValueInColumn(String columnName, String value, String tableName, String schemaName) {
        Table requiredTable = LOCAL_FILE_PROCESSOR.loadTable(schemaName, tableName + ".txt");
        List<LinkedHashMap<String, String>> tableRecords = requiredTable.getRecords();
        for (LinkedHashMap<String, String> record : tableRecords) {
            String requiredValue = record.get(columnName);
            if (requiredValue.equals(value)) {
                return false;
            }
        }
        return true;
    }

    public boolean validateUniqueInsert(Query query, LinkedHashMap<String, HashMap<String, String>> localMeta,
            String schemaName) {
        List<List<String>> inserts = query.getInserts();

        List<String> columns = query.getFields();

        for (Map.Entry<String, HashMap<String, String>> localMetaEntry : localMeta.entrySet()) {
            if (localMetaEntry.getValue().get("unique").equals("True")
                    || localMetaEntry.getValue().get("key").equals("PRIMARY KEY")) {
                String uniqueColumnName = localMetaEntry.getKey();
                int requiredInsertColumnIndex = columns.indexOf(uniqueColumnName);
                for (List<String> queryInsert : inserts) {
                    String uniqueValueInsert = queryInsert.get(requiredInsertColumnIndex);
                    if (!validateUniqueValueInColumn(uniqueColumnName, uniqueValueInsert, query.getTableName(),
                            schemaName))
                        return false;

                }
            }
        }
        return true;
    }

    public boolean validateUniqueUpdate(Query query, LinkedHashMap<String, HashMap<String, String>> localMeta,
            String schemaName) {

        for (Map.Entry<String, HashMap<String, String>> localMetaEntry : localMeta.entrySet()) {
            if (localMetaEntry.getValue().get("Unique").equals("True")) {
                String uniqueColumnName = localMetaEntry.getKey();

                String valueToUpdateTo = query.getValuesToUpdate().get(uniqueColumnName);
                if (!validateUniqueValueInColumn(uniqueColumnName, valueToUpdateTo, query.getTableName(), schemaName))
                    return false;
            }
        }
        return true;
    }

    public boolean validateForeignKeys(Query query, LinkedHashMap<String, HashMap<String, String>> localMeta,
            String schemaName) {
        List<LinkedHashMap<String, String>> globalMeta = LOCAL_FILE_PROCESSOR.readGlobalMeta();
        List<HashMap<String, String>> foreignKeys = new ArrayList<>();
        for (Map.Entry<String, HashMap<String, String>> localMetaEntry : localMeta.entrySet()) {
            if (!localMetaEntry.getValue().get("key").equals("null")
                    && !localMetaEntry.getValue().get("key").equals("PRIMARY KEY")) {
                String foreignKeyColumnName = localMetaEntry.getKey();
                String foreignKeyTableName = localMetaEntry.getValue().get("key");
                int tableInstanceNumber = validateTable(foreignKeyTableName, schemaName);
                if (tableInstanceNumber == -1) {
                    return false;
                }

                LinkedHashMap<String, HashMap<String, String>> foreignKeyTableMeta = null;
                Table foreignTable;
                if (tableInstanceNumber == LOCAL_FILE_PROCESSOR.currentVm) {
                    foreignKeyTableMeta = LOCAL_FILE_PROCESSOR.readTableMeta(foreignKeyTableName, schemaName);
                    foreignTable = LOCAL_FILE_PROCESSOR.loadTable(schemaName, foreignKeyTableName + ".txt");
                } else {
                    foreignKeyTableMeta = REMOTE_FILE_PROCESSOR.readTableMeta(foreignKeyTableName, schemaName);
                    foreignTable = REMOTE_FILE_PROCESSOR.loadTable(schemaName, foreignKeyTableName + ".txt");
                }

                List<String> queryCols = query.getFields();
                List<List<String>> queryInserts = query.getInserts();
                int requiredInsertColumnIndex = queryCols.indexOf(foreignKeyColumnName);
                for (List<String> queryInsert : queryInserts) {
                    String foreignKeyInsert = queryInsert.get(requiredInsertColumnIndex);
                    if (!validateSingleColumn(foreignKeyColumnName, foreignKeyInsert, foreignKeyTableMeta,
                            foreignTable)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public int validateInsertQuery(Query insertQuery, String schemaName) {
        String tableName = insertQuery.getTableName();
        int instanceNumber = validateTable(tableName, schemaName);
        if (instanceNumber == -1) {
            System.out.println("Global metadata nonexistent");
            return -1;
        }

        LinkedHashMap<String, HashMap<String, String>> localMeta = null;
        if (instanceNumber == LOCAL_FILE_PROCESSOR.currentVm)
            localMeta = LOCAL_FILE_PROCESSOR.readTableMeta(tableName, schemaName);
        else
            localMeta = REMOTE_FILE_PROCESSOR.readTableMeta(tableName, schemaName);

        List<String> columns = insertQuery.getFields();

        if (!validateColumns(columns, localMeta))
            return -1;

        System.out.println("Columns validated");
        List<List<String>> inserts = insertQuery.getInserts();

        if (!validateInsertDataTypes(inserts, localMeta, columns)) {
            System.out.println("Data type validation failed");
            return -1;
        }

        if (!validateUniqueInsert(insertQuery, localMeta, schemaName)) {
            System.out.println("Unique key constraint failed");
            return -1;
        }

        if (validateForeignKeys(insertQuery, localMeta, schemaName)) {
            return instanceNumber;
        } else {
            return -1;
        }
    }

    public int validateSelectQuery(Query selectQuery, String schemaName) {
        String tableName = selectQuery.getTableName();
        int instanceNumber = validateTable(tableName, schemaName);
        if (instanceNumber == -1) {
            System.out.println("Global metadata entry nonexistent!");
            return -1;
        }
        LinkedHashMap<String, HashMap<String, String>> localMeta = null;

        if (instanceNumber == LOCAL_FILE_PROCESSOR.currentVm)
            localMeta = LOCAL_FILE_PROCESSOR.readTableMeta(tableName, schemaName);
        else
            localMeta = REMOTE_FILE_PROCESSOR.readTableMeta(tableName, schemaName);

        List<String> columns = selectQuery.getFields();

        if (!validateColumns(columns, localMeta)) {
            System.out.println("[Table Metadata] Column validation failed!");
            return -1;
        }

        return instanceNumber;
    }

    public boolean validateUpdateDataTypes(Map<String, String> valuesToUpdate,
            LinkedHashMap<String, HashMap<String, String>> localMeta) {
        for (Map.Entry<String, String> entry : valuesToUpdate.entrySet()) {
            String columnName = entry.getKey();
            String metaDataType = localMeta.get(columnName).get("dataType");

            if (metaDataType.equals("FLOAT")) {
                try {
                    float value = Float.parseFloat(entry.getValue());
                } catch (NumberFormatException ex) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean validateConditionDataType(Query query, LinkedHashMap<String, HashMap<String, String>> localMeta) {
        List<List<String>> conditions = query.getConditions();

        for (List<String> condition : conditions) {
            String columnName = condition.get(0);
            if (localMeta.get(columnName) == null)
                return false;

            String metaDataType = localMeta.get(columnName).get("dataType");

            if (metaDataType.equals("FLOAt")) {
                try {
                    float value = Float.parseFloat(condition.get(2));
                } catch (NumberFormatException ex) {
                    return false;
                }
            } else {
                String operator = condition.get(1);
                if (!operator.equals("=")) {
                    return false;
                }
            }
        }
        return true;
    }

    public int validateUpdateQuery(Query updateQuery, String schemaName) {
        String tableName = updateQuery.getTableName();
        int instanceNumber = validateTable(tableName, schemaName);
        if (instanceNumber == -1) {
            System.out.println("[Global Metadata] Entry nonexistent");
        }

        LinkedHashMap<String, HashMap<String, String>> localMeta = null;

        if (instanceNumber == LOCAL_FILE_PROCESSOR.currentVm)
            localMeta = LOCAL_FILE_PROCESSOR.readTableMeta(tableName, schemaName);
        else
            localMeta = REMOTE_FILE_PROCESSOR.readTableMeta(tableName, schemaName);

        List<String> columns = updateQuery.getFields();

        if (!validateColumns(columns, localMeta)) {
            System.out.println("[Table Metadata] Column validation failed");
            return -1;
        }
        Map<String, String> updates = updateQuery.getValuesToUpdate();
        if (!validateUpdateDataTypes(updates, localMeta)) {
            return -1;
        }

        if (!validateConditionDataType(updateQuery, localMeta)) {
            return -1;
        }

        if (!validateUniqueUpdate(updateQuery, localMeta, schemaName)) {
            System.out.println("unique key constraint failed");
            return -1;
        }

        if (validateForeignKeys(updateQuery, localMeta, schemaName)) {
            return instanceNumber;
        } else {
            System.out.println("Foreign key constraint failed");
            return -1;
        }

    }

    public int validateDeleteQuery(Query deleteQuery, String schemaName) {
        String tableName = deleteQuery.getTableName();
        int instanceNumber = validateTable(tableName, schemaName);
        if (instanceNumber == -1) {
            System.out.println("[Global Metadata] Entry nonexistent");
        }

        LinkedHashMap<String, HashMap<String, String>> localMeta = null;

        if (instanceNumber == LOCAL_FILE_PROCESSOR.currentVm)
            localMeta = LOCAL_FILE_PROCESSOR.readTableMeta(tableName, schemaName);
        else
            localMeta = REMOTE_FILE_PROCESSOR.readTableMeta(tableName, schemaName);

        if (!validateConditionDataType(deleteQuery, localMeta)) {
            return -1;
        }

        return instanceNumber;
    }
}
