package com.dpg7.main.MetaData;

import QueryParsing.Query;
import com.dpg7.main.Globals;
import com.dpg7.main.Logs.EventLogManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class MetaDataImp implements MetaData {
    public String filePath;
    List<String> formattedMeta;
    BufferedWriter bw;
BufferedWriter bufferedWriter;
    public List<String> getFormattedMeta() {
        return formattedMeta;
    }

    public List<LinkedHashMap<String, String>> getRows() {
        return rows;
    }

    List<LinkedHashMap<String, String>> rows;
    LinkedHashMap<String, String> newlyAddedRow;

    public MetaDataImp(String filePath) {
        this.rows = new ArrayList<>();
        this.formattedMeta = new ArrayList<>();
        this.filePath = filePath;
    }

    /**
     * This function writes information to meta file (either global or local)
     * @throws IOException
     */

    public void writeMetaTable() throws IOException {
        System.out.println("[INFO] INSERTING TO METAFILE");
        bw = new BufferedWriter(new FileWriter(filePath, true));
        for (HashMap<String, String> record : rows) {
            System.out.println("WRITE META ROWS RECORD: " + record);
            StringJoiner sb = new StringJoiner(Globals.GET_SEPARATOR());
            for (var entry : record.values()) {
                sb.add(entry);
            }
            System.out.println("FINAL ADDING STRING: "+sb.toString());
            bw.write("\n" + sb.toString());

        }
        bw.flush();
        bw.close();

        System.out.println("[INFO] SUCCESSFULLY INSERTED IN THE META FILE");
    }


    /**
     * Construct the Meta Info after the query has been parsed
     * @param query
     * @param columns
     * @param instanceName
     * @return adds a row to existing meta records and returns the latest list of records (i.e., rows)
     */
    public List<LinkedHashMap<String, String>> constructMetaInfo(Query query, String[] columns, String instanceName) {
        String tableName = query.getTableName();
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (String column : columns) {
            map.put(column, instanceName);
            map.put(column, query.getDatabaseName());
            map.put(column, query.getTableName());
        }
        rows.add(map);
        return rows;
    }

    /**
     * formatMetaData converts List of Hashmap to a string separated by delimiter "\t"
     *
     * @return
     */

    public List<String> formatMetaData() {
        StringJoiner headers = new StringJoiner(Globals.GET_SEPARATOR());
        for (String columnHeaders : rows.get(0).keySet()) {
            headers.add(columnHeaders);
        }
        formattedMeta.add(headers.toString());
        for (HashMap<String, String> record : rows) {
            StringJoiner sb = new StringJoiner(Globals.GET_SEPARATOR());
            for (var entry : record.values()) {
                sb.add(entry);
            }
            formattedMeta.add(sb.toString());
        }
        return formattedMeta;
    }

}
