package com.dpg7.main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Schema {
    private HashMap<String, Table> schema;
    private String schemaName;
    private File schemaFolder;
    private List<String> relatedTables;

    public Schema(String schemaName, HashMap<String, Table> schemaData) {
        schemaFolder = new File(Globals.GET_SCHEMA_PATH() + schemaName);
        this.schemaName = schemaName;
        schema = schemaData;
        relatedTables= List.copyOf(schema.keySet());
    }

    public String getSchemaName() {
        return schemaName;
    }

    public HashMap<String, Table> getSchema() {
        return schema;
    }

    public List<String> getTables() {
        return new ArrayList<>(schema.keySet());
    }

    public void loadTable(String tableName) throws IllegalAccessException {
        if (!schemaFolder.isDirectory()) {
            throw new IllegalAccessException("Schema access failure!");
        }
        relatedTables.add(tableName);
    }

}
