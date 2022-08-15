package com.dpg7.main;

import com.dpg7.main.Logs.LogException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringJoiner;

public class Initializer {

    public void initializeRoot() throws IOException {
        final File ROOT_FOLDER = new File(Globals.GET_ROOT());
        final File SCHEMAS_FOLDER = new File(Globals.GET_SCHEMA_PATH());
        final File METADATA = new File(Globals.GET_ROOT() + File.separator + "METADATA");
        final File LOG = new File(Globals.GET_LOG_PATH());
        final String QUERY_LOG = Globals.GET_QUERY_LOG_PATH();
        final String EVENT_LOG = Globals.GET_EVENT_LOG_PATH();
        final String GENERAL_LOG = Globals.GET_GENERAL_LOG_PATH();

        initHelper(ROOT_FOLDER);
        initHelper(SCHEMAS_FOLDER);
        initHelper(METADATA);
        fileInitHelper(Globals.GET_GLOBAL_METADATA());

        fileInitHelper(Globals.GET_USER_PROFILE_FILE());
        initHelper(LOG);
        fileInitHelper(QUERY_LOG);
        fileInitHelper(EVENT_LOG);
        fileInitHelper(GENERAL_LOG);

        // initQueryLogHeader();
    }

    private void initGlobalMetaFileColumns() throws IOException {
        String globalMetaColumns = "instance\tschema\ttable";
        BufferedWriter writer = new BufferedWriter(new FileWriter(Globals.GET_GLOBAL_METADATA()));
        writer.write(globalMetaColumns);
        writer.close();
    }

    private void initHelper(File dir) {
        boolean initializationResult = false;

        if (!dir.exists()) {
            initializationResult = dir.mkdir();
        }
        if (initializationResult) {
            System.out.println("Created " + dir.getName() + " directory successfully");
        } else {
            System.out.println(dir.getName() + " directory already exists");
        }
    }

    public void fileInitHelper(String file) throws IOException {
        boolean initializationResult = false;

        File fileObject = new File(file);
        if (!fileObject.exists()) {

            Path newFilePath = Paths.get(file);
            try {
                Files.createFile(newFilePath);
                initializationResult = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        if (initializationResult) {
            if (file.equals(Globals.GET_GLOBAL_METADATA())) {
                initGlobalMetaFileColumns();
            }
            System.out.println("Created " + fileObject.getName() + " file successfully");
        } else {
            System.out.println(fileObject.getName() + " file already exists");
        }
    }
     public void initQueryLogHeader()
     {
     StringJoiner headers = new StringJoiner(Globals.GET_SEPARATOR());
     String queryLogPath = Globals.GET_QUERY_LOG_PATH();
     for (String columnHeaders : Globals.queryLogHeaders)
     {
     headers.add(columnHeaders);
     }
     BufferedWriter logWriter = null;
     try {
     logWriter = new BufferedWriter(new FileWriter(queryLogPath,true));
     logWriter.write(headers.toString());
     logWriter.newLine();
     logWriter.flush();
     } catch (IOException e) {
     e.printStackTrace();
     }
     finally { // always close the file
     if (logWriter != null) try {
     logWriter.close();
     } catch (IOException ioe2) {
     // just ignore it
     }
     }
     }

}
