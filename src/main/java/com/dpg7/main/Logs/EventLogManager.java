package com.dpg7.main.Logs;

import QueryParsing.Query;
import com.dpg7.main.Globals;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class EventLogManager extends LogManager
{
    private static EventLogManager INSTANCE;
//    private String logTimestamp;//Log Entry Timestamp
//    //InstanceName
//    private String databaseName;
//    private String tableName;
//    private String userId;
//    private String eventTimestamp; //Event Occurrence timestamp
//    private String eventMessage;

    private EventLogManager()
    {
    }
    public static EventLogManager getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new EventLogManager();
        }
        return INSTANCE;
    }

    public  Map<String,String> constructLogEntry(EventLog eventLog, String instanceName)
    {
        rawLogDetails = new LinkedHashMap();
        String[] eventLogHeaders = Globals.eventLogHeaders;
        rawLogDetails.put(eventLogHeaders[0], eventLog.logTimestamp);
        rawLogDetails.put(eventLogHeaders[1],eventLog.instanceName);
        rawLogDetails.put(eventLogHeaders[2],eventLog.databaseName);
        rawLogDetails.put(eventLogHeaders[3],eventLog.tableName);
        rawLogDetails.put(eventLogHeaders[4],eventLog.userId); //@todo
        rawLogDetails.put(eventLogHeaders[5],eventLog.eventTimestamp); //@todo
        rawLogDetails.put(eventLogHeaders[6],eventLog.eventMessage); //@todo
        return rawLogDetails;
    }

    @Override
    public void writeLogTable() throws LogException
    {
        String eventLogPath = Globals.GET_LOG_PATH() + Globals.EVENT_LOG + Globals.GET_FILE_EXT();

        BufferedWriter logWriter = null;
        try {
            logWriter = new BufferedWriter(new FileWriter(eventLogPath,true));
            logWriter.write(logEntry);
            logWriter.newLine();
            logWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            final String errorMessage = "Error: {" + e.getMessage() + "}!";
            throw new LogException(errorMessage);
        }
        finally {
            if (logWriter != null) try {
                logWriter.close();
            } catch (IOException ioe2) {
                //ignore it
            }
        }
    }
    public void processLog(EventLog log) throws IOException
    {
        constructLogEntry(log, "GCP-1");
        formatLogEntry();

        try {
            writeLogTable();
        } catch (LogException e) {
            e.printStackTrace();
        }
    }
}
