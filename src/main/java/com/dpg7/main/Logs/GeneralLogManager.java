package com.dpg7.main.Logs;

/*Design Decision:
General Logs will be invoked only when any Schema related changes occur in the system*/

import QueryParsing.Query;
import com.dpg7.main.Globals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GeneralLogManager extends LogManager
{
    private static GeneralLogManager INSTANCE;

    private GeneralLogManager()
    {
    }
    public static GeneralLogManager getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new GeneralLogManager();
        }
        return INSTANCE;
    }

    public  Map<String,String> constructLogEntry(GeneralLog generalLog, String instanceName)
    {
        rawLogDetails = new LinkedHashMap();
        String[] queryLogHeaders = Globals.queryLogHeaders;
        rawLogDetails.put(queryLogHeaders[0], generalLog.logTimestamp);
        rawLogDetails.put(queryLogHeaders[1],generalLog.instanceName);
        rawLogDetails.put(queryLogHeaders[2],generalLog.databaseName);
        rawLogDetails.put(queryLogHeaders[3],generalLog.userId);//@todo
        rawLogDetails.put(queryLogHeaders[4],generalLog.numberOfTables); //@todo
        rawLogDetails.put(queryLogHeaders[5],generalLog.numberOfRecords); //@todo
        return rawLogDetails;
    }

    @Override
    public void writeLogTable() throws LogException
    {
        String generalLogPath = Globals.GET_LOG_PATH() + Globals.GENERAL_LOG + Globals.GET_FILE_EXT();

        BufferedWriter logWriter = null;
        try {
            logWriter = new BufferedWriter(new FileWriter(generalLogPath,true));
            logWriter.write(logEntry);
            logWriter.newLine();
            logWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            final String errorMessage = "Error: {" + e.getMessage() + "}!";
            throw new LogException(errorMessage);
        }
        finally {                       // always close the file
            if (logWriter != null) try {
                logWriter.close();
            } catch (IOException ioe2) {
                // just ignore it
            }
        }
    }

    public void processLog(GeneralLog log) throws IOException
    {
        constructLogEntry(log, "GCP-1");
        formatLogEntry();

        try {
            writeLogTable();
        } catch (LogException e) {
            e.printStackTrace();
        }
    }

    public Integer getNumTables(String schemaName)
    {
        File schemaDirectory = new File(Globals.GET_SCHEMA_PATH() + schemaName);
        int numberOfTables=0;

        File[] fileArray = schemaDirectory.listFiles();

        if(fileArray!=null)
        {
            for (File file : fileArray) {
                if (!file.isDirectory()) {
                    numberOfTables++;
                    System.out.println("File: " + file.getAbsolutePath());
                }
            }
        }

        return numberOfTables;

    }

    public Integer getNumRows(String schemaName)
    {
        File schemaDirectory = new File(Globals.GET_SCHEMA_PATH() + schemaName);
        int numberOfRecords=0;
        Path path;

        File[] fileArray = schemaDirectory.listFiles();

        if(fileArray!=null)
        {
            for (File file : fileArray)
            {
                if (!file.isDirectory())
                {
                    path = Paths.get(String.valueOf(file));
                    try {
                        numberOfRecords+=Files.lines(path).count();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    numberOfRecords++;
                }
            }
        }
        System.out.println("Schema: " + schemaName + "-" + (numberOfRecords-1));
        return numberOfRecords-1; //To eliminate last row


    }

}
