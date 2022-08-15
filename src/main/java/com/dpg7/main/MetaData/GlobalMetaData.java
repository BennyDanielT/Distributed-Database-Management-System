package com.dpg7.main.MetaData;

import QueryParsing.Query;
import com.dpg7.main.FileProcessor;
import com.dpg7.main.Globals;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringJoiner;

public class GlobalMetaData extends MetaDataImp {
    public LinkedHashMap<String, String> newlyAddedGlobalRow;

    //singleton object because it is a global meta data

    private static GlobalMetaData globalMetaInstance = null;

    private GlobalMetaData(String filePath) {
        super(filePath);
    }

    /**
     * Get a single instance of the global meta data object
     * @param filePath
     * @return
     */
   public static GlobalMetaData getGlobalMetaInstance(String filePath) {
        if(globalMetaInstance == null){
            globalMetaInstance = new GlobalMetaData(filePath);
        }
        return globalMetaInstance;
   }
    /**
     * Query{type='CREATE', tableName='EMPLOYEE', databaseName='', fields=[ID, NAME], valuesToUpdate={}, conditions=[], inserts=[], datatypes=[INTEGER, VARCHAR]}
     * @param query
     * columns for global data is instance name, schema/database name and table name.
     * return: It would return a single row that would store the values of columns.
     */
    public void constructGlobalMeta(Query query, String[] columns, String instanceName, String schemaName) {
        LinkedHashMap<String,String> map = new LinkedHashMap<>();
        map.put(columns[0],instanceName);
        map.put(columns[1],schemaName);
        map.put(columns[2],query.getTableName());
        newlyAddedGlobalRow = map;
        rows.add(map);
    }

    public void writeGlobalMeta() throws IOException {
        System.out.println("[INFO] Inserting to Global Meta");
        bw = new BufferedWriter(new FileWriter(filePath, true));
        StringJoiner sb = new StringJoiner(Globals.GET_SEPARATOR());
        for (var entry: newlyAddedGlobalRow.values()){
            sb.add(entry);
        }
        bw.write("\n" + sb.toString());
        bw.flush();
        bw.close();

        new FileProcessor(true).replaceFileInRemote(filePath);
    }


}
