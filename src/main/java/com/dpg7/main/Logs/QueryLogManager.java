package com.dpg7.main.Logs;
import com.dpg7.main.Globals;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class QueryLogManager extends LogManager
{
    private static QueryLogManager INSTANCE;

    private QueryLogManager()
    {
    }
    public static QueryLogManager getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new QueryLogManager();
        }
        return INSTANCE;
    }

    public  Map<String,String> constructLogEntry(QueryLog querylog, String instanceName)
    {
        rawLogDetails = new LinkedHashMap();

        String[] queryLogHeaders = Globals.queryLogHeaders;
        rawLogDetails.put(queryLogHeaders[0], querylog.logTimestamp);
        rawLogDetails.put(queryLogHeaders[1], querylog.instanceName);
        rawLogDetails.put(queryLogHeaders[2],querylog.databaseName);
        rawLogDetails.put(queryLogHeaders[3],querylog.tableName);
        rawLogDetails.put(queryLogHeaders[4],querylog.userId);
        rawLogDetails.put(queryLogHeaders[5],querylog.queryValidity); //@todo
        rawLogDetails.put(queryLogHeaders[6],querylog.queryType); //@todo
        rawLogDetails.put(queryLogHeaders[7],querylog.queryExecutionTime); //@todo
        rawLogDetails.put(queryLogHeaders[8],querylog.query); //@todo
        return rawLogDetails;
    }

    @Override
    public void writeLogTable() throws LogException
    {
        String queryLogPath = Globals.GET_LOG_PATH() + Globals.QUERY_LOG + Globals.GET_FILE_EXT();

        BufferedWriter logWriter = null;
        try {
            logWriter = new BufferedWriter(new FileWriter(queryLogPath,true));
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

    public void processLog(QueryLog log) throws IOException
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


