package com.dpg7.main;

import RemoteConnector.RemoteConnector;
import com.jcraft.jsch.SftpException;

import java.io.*;
import user.UserProfile;

import QueryParsing.Query;
import com.dpg7.main.Logs.EventLogManager;
import com.dpg7.main.MetaData.GlobalMetaData;
import com.dpg7.main.MetaData.TableMetaData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileProcessor {
    private final File ROOT_FOLDER = new File(Globals.GET_ROOT());
    private final File SCHEMA_FOLDER = new File(Globals.GET_SCHEMA_PATH());
    private final File METADATA_FOLDER = new File(Globals.GET_GLOBAL_METADATA());
    public RemoteConnector remoteConnector = new RemoteConnector();
    public boolean isRemote;
    public int currentVm;

    public FileProcessor(boolean setRemote) {
        isRemote = setRemote;
        currentVm = Integer.parseInt(State.getInstance().getCurrentVM());
    }

    public void printStructure() {
        for (final File fileEntry : Objects.requireNonNull(ROOT_FOLDER.listFiles())) {
            if (fileEntry.isDirectory()) {
                System.out.print("Folder: ");
            } else {
                System.out.print("File: ");
            }
            System.out.println(fileEntry.getName());
        }
    }

    public Schema loadSchema(String schemaName) throws IllegalAccessException {
        File schemaFolder = new File(SCHEMA_FOLDER.getPath() + File.separator + schemaName);
        HashMap<String, Table> schemaCache = new HashMap<>();
        if (!schemaFolder.exists() || !schemaFolder.isDirectory()) {
            throw new IllegalAccessException("Issue with Schema access!");
        }

        for (final File fileEntry : Objects.requireNonNull(schemaFolder.listFiles())) {
            String fileName = fileEntry.getName();
            fileName.replaceAll(".txt", "");
            System.out.println("FILENAMEEEEE: " + fileName);
            if (!fileEntry.isDirectory())
                schemaCache.put(fileEntry.getName(), loadTable(schemaName,
                        fileName));
        }
        return new Schema(schemaName, schemaCache);
    }

    public List<LinkedHashMap<String, String>> readFile(String filePath) {
        List<LinkedHashMap<String, String>> tableCache = new ArrayList<>();
        String line;
        List<String> columnNames = new ArrayList<>();
        BufferedReader br = null;
        System.out.println("REMOTE STATUS: " + isRemote);
        try {
            if (!isRemote) {
                System.out.println("LOCAL");
                br = new BufferedReader(new FileReader(filePath));
            } else {
                br = new BufferedReader(new InputStreamReader(remoteConnector.getInputStream(filePath)));
                remoteConnector.closeConnection();
            }

            int idx = 0;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(Globals.GET_SEPARATOR(), 0);
                LinkedHashMap<String, String> temp = new LinkedHashMap<>();
                for (int i = 0; i < parts.length; i++) {
                    if (idx == 0) {
                        columnNames.add(parts[i]);
                    }

                    temp.put(columnNames.get(i), parts[i]);
                }
                tableCache.add(temp);
                idx++;
            }
        } catch (IOException | SftpException e) {
            e.printStackTrace();
        }
        return tableCache;
    }

    public static void submitTransaction(List<HashMap<String,String>> dataToCommit) {
        String path = Globals.GET_SCHEMA_PATH() +  File.separator + Globals.GET_FILE_EXT();
        Path tableFile = Paths.get(path);
        try {
            Files.write(tableFile, Collections.singleton(dataToCommit.toString()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> readLogFile(String filePath) {
        List<String> tableCache = new ArrayList<>();
        String line;
        BufferedReader br = null;
        System.out.println("REMOTE STATUS: " + isRemote);
        try {
            if (!isRemote) {
                System.out.println("LOCAL");
                br = new BufferedReader(new FileReader(filePath));
            } else {
                br = new BufferedReader(new InputStreamReader(remoteConnector.getInputStream(filePath)));
            }
            while ((line = br.readLine()) != null) {
                tableCache.add(line);
            }
        } catch (IOException | SftpException e) {
            e.printStackTrace();
        }
        return tableCache;
    }

    public Table loadTable(String schemaName, String tableName) {
        File tableFile = new File(Globals.GET_SCHEMA_PATH() + schemaName + File.separator + tableName);
        List<LinkedHashMap<String, String>> tableCache = readFile(tableFile.getPath());
        return new Table(tableCache, schemaName);
    }

    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public void createSchema(String schemaName) {
        File schemaFolder = new File(Globals.GET_SCHEMA_PATH() + schemaName);
        boolean deletion = false;
        boolean creation = false;
        if (schemaFolder.exists()) {
            deletion = deleteDirectory(schemaFolder);
        }
        creation = schemaFolder.mkdir();

        // Code for Creating Local Metadata folder in each schema
        File metadataFolder = new File(schemaFolder + File.separator + Globals.getMETADATA());
        boolean metaCreation = false;
        metaCreation = metadataFolder.mkdir();
    }

    public void writeTable(String tableName, String schemaName, Table records) throws IOException { // TODO: The records
                                                                                                    // table needs to be
                                                                                                    // accessed from
                                                                                                    // metadata.txt I
                                                                                                    // think?
        String path = Globals.GET_SCHEMA_PATH() + schemaName + File.separator + tableName + Globals.GET_FILE_EXT();
        Path tableFile = Paths.get(path);
        Files.write(tableFile, records.stringifyTable(), StandardCharsets.UTF_8);
    }

    // Code for Writing to the local metadata.txt folder corresponding to change in
    // a table

    public List<LinkedHashMap<String, String>> readGlobalMeta() {
        return this.readFile(Globals.GET_GLOBAL_METADATA());
    }

    // public void writeGlobalMeta(Table updatedGlobalMeta) throws IOException {
    // String globalMetaPath = Globals.GET_GLOBAL_METADATA();
    // Files.write(Path.of(globalMetaPath), updatedGlobalMeta.stringifyTable(),
    // StandardCharsets.UTF_8);
    // }

    public LinkedHashMap<String, HashMap<String, String>> readTableMeta(String tableName, String schemaName) {
        String metaTablePath = Globals.GET_LOCAL_METADATA_PATH(tableName, schemaName);
        List<LinkedHashMap<String, String>> metaEntries = readFile(metaTablePath);
        if (metaEntries.size() == 0) {
            return null;
        }
        metaEntries.remove(0);

        LinkedHashMap<String, HashMap<String, String>> localMeta = new LinkedHashMap<>();
        for (HashMap<String, String> map : metaEntries) {
            String columnName = map.get("Column_Name");
            String dataType = map.get("Datatype");
            String key = map.get("Key");
            String unique = map.get("Unique");

            HashMap<String, String> metaInnerMap = new HashMap<>();

            metaInnerMap.put("columnName", columnName);
            metaInnerMap.put("dataType", dataType);
            metaInnerMap.put("key", key);
            metaInnerMap.put("unique", unique);

            localMeta.put(columnName, metaInnerMap);
        }
        return localMeta;
    }
    // //Code for Writing to the local metadata folder corresponding to change in a
    // table
    // public void writeMetaTable(String tableName, String schemaName, MetadataTable
    // metaTable) throws IOException {
    // String tableMetaFilePath = Globals.GET_SCHEMA_PATH() + schemaName +
    // File.separator + Globals.getMETADATA() + File.separator + tableName +
    // Globals.GET_FILE_EXT();
    // Path tableMetaFile = Paths.get(tableMetaFilePath);
    //
    // Files.write(tableMetaFile, metaTable.getRows(), StandardCharsets.UTF_8);
    // }
    //
    // public void writeMetaTable(String path,String tableName, String schemaName,
    // MetadataTable metaTable) throws IOException {
    // String globalMetaFilePath = Globals.GET_METADATA()+ tableName+
    // Globals.GET_FILE_EXT();
    // Path globalMetaFile = Paths.get(globalMetaFilePath);
    // System.out.println(globalMetaFilePath);
    //
    // Files.write(globalMetaFile, metaTable.getRows(), StandardCharsets.UTF_8);
    // }

    public void replaceFileInRemote(String filePath) {
        try {
            remoteConnector.replaceFileInRemote(filePath);
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }

    public void processGlobalMetaData(Query query, String schemaName) throws IOException {
        // Global Meta Data processing and entry to global file

        String globalMetaFilePath = Globals.GET_METADATA() + "global" + Globals.GET_FILE_EXT();
        GlobalMetaData globalMetaData = GlobalMetaData.getGlobalMetaInstance(globalMetaFilePath);
        globalMetaData.constructGlobalMeta(query, new String[] { "instance", "schema", "table" }, "1", schemaName);
        // globalMetaData.formatMetaData();
        globalMetaData.writeGlobalMeta();
    }

    public void processLocalMetaData(Query query, String schemaName) throws IOException {
        // Table Meta Data processing and entry to local file

        String tableMetaFilePath = Globals.GET_LOCAL_METADATA_PATH(query.getTableName(), schemaName);
        System.out.println("TABLE META FILE PATH WOWIE" + tableMetaFilePath);
        String[] localMetaHeaders = new String[] { "Column_Name", "Datatype", "Key", "Unique" };
        String columnsString = String.join("\t", localMetaHeaders);
        Initializer IZ = new Initializer();
        IZ.fileInitHelper(tableMetaFilePath);
        BufferedWriter writer = new BufferedWriter(new FileWriter(tableMetaFilePath));
        writer.write(columnsString);
        writer.close();
        TableMetaData tableMetaData = new TableMetaData(tableMetaFilePath);

        tableMetaData.constructMetaInfo(query, localMetaHeaders, "1");
        // tableMetaData.formatMetaData();

        tableMetaData.writeMetaTable();
    }

    public void createTable(String tableName, String schemaName, List<String> columns) throws IOException {
        String tablePath = Globals.GET_TABLE_PATH(tableName, schemaName);
        Initializer IZ = new Initializer();
        IZ.fileInitHelper(tablePath);
        String columnsString = String.join("\t", columns);
        BufferedWriter writer = new BufferedWriter(new FileWriter(tablePath));
        writer.write(columnsString);
        writer.close();
    }

    public void appendToFile(String filePath, String row) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true));
        bw.write("\n" + row);
        bw.flush();
        bw.close();
    }

    public void insertToTable(Query query, String tableName, String schemaName) throws IOException {
        List<List<String>> inserts = query.getInserts();
        List<String> insertionStrings = new ArrayList<>();
        for (List<String> insert : inserts) {
            StringJoiner sj = new StringJoiner("\t");
            for (String i : insert) {
                sj.add(i);
            }
            insertionStrings.add(sj.toString());
        }

        for (String is : insertionStrings) {
            appendToFile(Globals.GET_TABLE_PATH(tableName, schemaName), is);
        }

    }

}
