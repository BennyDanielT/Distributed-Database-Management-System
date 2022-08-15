package QueryExecution;

import QueryParsing.InvalidQueryException;
import QueryParsing.Query;
import com.dpg7.main.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class ExecutionDriver {
    public Schema currentSchema = null;
    FileProcessor LOCAL_FILE_PROCESSOR = null;
    FileProcessor REMOTE_FILE_PROCESSOR = null;
    MetadataValidation metadataValidation = new MetadataValidation();

    public ExecutionDriver(FileProcessor LOCAL_FILE_PROCESSOR_i, FileProcessor REMOTE_FILE_PROCESSOR_i) {
        LOCAL_FILE_PROCESSOR = LOCAL_FILE_PROCESSOR_i;
        REMOTE_FILE_PROCESSOR = REMOTE_FILE_PROCESSOR_i;
    }

    public void executeInsert(Query query) throws IOException {
        State state = State.getInstance();
        int validation = metadataValidation.validateInsertQuery(query, state.getCurrentSchema());
        if (validation == -1)
            throw new InvalidQueryException("INSERT query validation failed");
        System.out.println("Data types validated");

        if (validation == LOCAL_FILE_PROCESSOR.currentVm) {
            LOCAL_FILE_PROCESSOR.insertToTable(query, query.getTableName(), state.getCurrentSchema());
        } else {
            REMOTE_FILE_PROCESSOR.insertToTable(query, query.getTableName(), state.getCurrentSchema());
        }
    }

    public void executeDelete(Query query) throws IOException {
        State state = State.getInstance();
        int validation = metadataValidation.validateDeleteQuery(query, state.getCurrentSchema());
        if (validation == -1)
            throw new InvalidQueryException("DELETE query validation failed");
        FileProcessor INNER_FP;
        Table table = null;
        LinkedHashMap<String, HashMap<String, String>> localMeta = null;
        if (validation == LOCAL_FILE_PROCESSOR.currentVm) {
            INNER_FP = LOCAL_FILE_PROCESSOR;
        } else {
            INNER_FP = REMOTE_FILE_PROCESSOR;
        }
        table = INNER_FP.loadTable(state.getCurrentSchema(), query.getTableName() + ".txt");
        localMeta = INNER_FP.readTableMeta(query.getTableName(), state.getCurrentSchema());
        List<LinkedHashMap<String, String>> tableRecords = table.getRecords();
        LinkedHashMap<String, String> tableHeader = tableRecords.get(0);
        tableRecords.remove(0);
        List<Integer> indicesToDelete = TableOperations.filterByWhere(tableRecords, localMeta, query.getConditions());

        if (indicesToDelete.size() > 0) {
            for (int x = tableRecords.size() - 1; x >= 0; x--) {
                if (indicesToDelete.contains(x))
                    tableRecords.remove(x);
            }
            tableRecords.add(0, tableHeader);
            Table newTable = new Table(tableRecords, state.getCurrentSchema());

            INNER_FP.writeTable(query.getTableName(), state.getCurrentSchema(), newTable);
        }

    }

    public void executeUse(Query query) throws IllegalAccessException {
        boolean validation = metadataValidation.validateSchema(query.getDatabaseName());
        if (!validation)
            throw new IllegalAccessException("Schema access failed");
        System.out.println(query.getDatabaseName());
        currentSchema = LOCAL_FILE_PROCESSOR.loadSchema(query.getDatabaseName());
        State state = State.getInstance();
        state.setCurrentSchema(currentSchema.getSchemaName());
    }

    public void executeCreateTable(Query query) throws IllegalAccessException, IOException, InvalidQueryException {
        if (currentSchema == null) {
            throw new IllegalAccessException("No selected schema!");
        }
        boolean validation = metadataValidation.validateSchema(currentSchema.getSchemaName());
        if (!validation)
            throw new IllegalAccessException("Schema access failed!");

        LOCAL_FILE_PROCESSOR.createTable(query.getTableName(), currentSchema.getSchemaName(), query.getFields());
        LOCAL_FILE_PROCESSOR.processGlobalMetaData(query, currentSchema.getSchemaName());
        LOCAL_FILE_PROCESSOR.processLocalMetaData(query, currentSchema.getSchemaName());

//        REMOTE_FILE_PROCESSOR.processGlobalMetaData(query, currentSchema.getSchemaName());
    }

    public void executeCreateDatabase(Query query) throws IOException {
        LOCAL_FILE_PROCESSOR.processGlobalMetaData(query, query.getDatabaseName());
        LOCAL_FILE_PROCESSOR.createSchema(query.getDatabaseName());
//        REMOTE_FILE_PROCESSOR.processGlobalMetaData(query, query.getDatabaseName());
        REMOTE_FILE_PROCESSOR.createSchema(query.getDatabaseName());
    }

    public void executeUpdate(Query query) throws IllegalAccessException, IOException {
        boolean validation = true;
        // Validate schema
        String schemaName = State.getInstance().getCurrentSchema();
        validation = validation & metadataValidation.validateSchema(schemaName);
        // Validate table and get instance number
        int instanceNumber = metadataValidation.validateTable(query.getTableName(), schemaName);
        validation = validation & (instanceNumber == -1 ? false : true);
        // Check the state's instance number and get corresponding table and it's
        // metadata
        // TODO: Check getCurrentVm
        FileProcessor INNER_FP;
        if (LOCAL_FILE_PROCESSOR.currentVm == instanceNumber) {
            INNER_FP = LOCAL_FILE_PROCESSOR;
        } else {
            INNER_FP = REMOTE_FILE_PROCESSOR;
        }
        Table table;
        LinkedHashMap<String, HashMap<String, String>> tableMeta;

        table = INNER_FP.loadTable(schemaName, query.getTableName() + ".txt");
        tableMeta = INNER_FP.readTableMeta(query.getTableName(), schemaName);

        List<String> cols = new ArrayList<>(query.getValuesToUpdate().keySet());
        // Validate columns to be updated
        validation = validation
                & (metadataValidation.validateColumns(cols, tableMeta));
        // validate columns in where condition
        List<String> whereCols = new ArrayList<>();
        if (query.getConditions().size() != 0) {
            whereCols = new ArrayList<>();
            for (List<String> condition : query.getConditions()) {
                whereCols.add(condition.get(0));
            }
            validation = validation & (metadataValidation.validateColumns(whereCols, tableMeta));
        }

        if (!validation) {
            throw new IllegalAccessException("Schema access failed");
        }

        List<LinkedHashMap<String, String>> out = table.getRecords();
        // If there's a where condition
        if (query.getConditions().size() != 0) {
            List<Integer> outIndicies = TableOperations.filterByWhere(out, tableMeta, query.getConditions());

            for (Integer index : outIndicies) {
                LinkedHashMap<String, String> currRow = new LinkedHashMap<>(out.get(index));

                // Execute update
                out.set(index, TableOperations.updateByWhere(currRow, query.getValuesToUpdate()));
            }
        }
        Table table2 = new Table(out, schemaName);
        INNER_FP.writeTable(query.getTableName(), schemaName, table2);

    }

    public void executeSelect(Query query)
            throws IllegalAccessException {
        boolean validation = true;
        // Validate schema
        String schemaName = State.getInstance().getCurrentSchema();
        validation = validation & metadataValidation.validateSchema(schemaName);
        // Validate table and get instance number
        int instanceNumber = metadataValidation.validateTable(query.getTableName(), schemaName);
        validation = validation & (instanceNumber == -1 ? false : true);
        // Check the state's instance number and get corresponding table and it's
        // metadata
        // TODO: Check getCurrentVm
        Table table;
        LinkedHashMap<String, HashMap<String, String>> tableMeta;
        FileProcessor INNER_FP;
        if (LOCAL_FILE_PROCESSOR.currentVm == instanceNumber) {
            INNER_FP = LOCAL_FILE_PROCESSOR;
        } else {
            INNER_FP = REMOTE_FILE_PROCESSOR;
        }

        table = INNER_FP.loadTable(schemaName, query.getTableName() + ".txt");
        tableMeta = INNER_FP.readTableMeta(query.getTableName(), schemaName);

        // Validate columns
        if (!query.getFields().get(0).equals("*")) {
            validation = validation & (metadataValidation.validateColumns(query.getFields(), tableMeta));
        }

        // validate columns in where condition
        List<String> whereCols = new ArrayList<>();
        if (query.getConditions().size() != 0) {
            whereCols = new ArrayList<>();
            for (List<String> condition : query.getConditions()) {
                whereCols.add(condition.get(0));
            }
            validation = validation & (metadataValidation.validateColumns(whereCols, tableMeta));
        }

        if (!validation) {
            throw new IllegalAccessException("Schema access failed");
        }

        List<LinkedHashMap<String, String>> out = table.getRecords();
        out.remove(0);
        // If there's a where condition
        if (query.getConditions().size() != 0) {
            List<Integer> outIndicies = TableOperations.filterByWhere(out, tableMeta, query.getConditions());
            List<LinkedHashMap<String, String>> currOut = new ArrayList<>(out);
            out = new ArrayList<>();
            for (Integer index : outIndicies) {
                out.add(currOut.get(index));
            }
        }

        // Filter by columns
        out = TableOperations.filterByColumns(out, query.getFields());
        for (LinkedHashMap<String, String> row : out) {
            System.out.println(row);
        }
    }

}
