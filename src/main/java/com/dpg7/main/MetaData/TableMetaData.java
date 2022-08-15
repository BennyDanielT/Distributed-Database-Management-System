package com.dpg7.main.MetaData;

import QueryParsing.Query;
import com.dpg7.main.Globals;

import java.util.*;

public class TableMetaData extends MetaDataImp {

    public TableMetaData(String filePath) {
        super(filePath);
        System.out.println("file path....." + filePath);
    }

    /**
     * Query{type='CREATE', tableName='EMPLOYEE', databaseName='', fields=[ID,
     * NAME], valuesToUpdate={}, conditions=[], inserts=[], datatypes=[INTEGER,
     * VARCHAR]}
     * 
     * @param query
     *              columns for global data is instance name, schema/database name
     *              and table name.
     *              return: It would return a single row that would store the values
     *              of columns.
     */
    @Override
    public List<LinkedHashMap<String, String>> constructMetaInfo(Query query, String[] columns, String instanceName) {
        List<String> fields = query.getFields();
        List<String> constraints = query.getConstraints();
        for (int i = 0; i < fields.size(); i++) {
            LinkedHashMap<String, String> map = new LinkedHashMap<>();
            map.put(columns[0], query.getFields().get(i));
            map.put(columns[1], query.getDatatypes().get(i));
            String thisFieldConstraints = constraints.get(i);
            List<String> splitConstraints = List.of(thisFieldConstraints.split(Globals.GET_SEPARATOR()));
            if (splitConstraints.contains("PRIMARY KEY")){
                map.put("Key", Globals.PRIMARY_KEY);
            } else {
                map.put("Key", "null");
            }
            if (splitConstraints.contains("UNIQUE")) {
                map.put("Unique", Globals.TRUE);
            } else {
                map.put("Unique", "null");
            }
            for (String sc: splitConstraints) {
                if (!sc.equals("PRIMARY KEY") && !sc.equals("UNIQUE") && !sc.equals("")){
                    if (sc.equals("U")){
                        map.put("Unique", Globals.TRUE);
                    } else {
                        map.put("Key", sc);
                    }
                }
            }
            rows.add(map);
        }
        return rows;
    }
}
