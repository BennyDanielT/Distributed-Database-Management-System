package com.dpg7.main.Logs;

import com.dpg7.main.Globals;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public abstract class LogManager
{
    public String instanceName;
    public static String logEntry;
    public static Map<String, String> rawLogDetails = new LinkedHashMap();

    public Map<String, String> constructLogEntry(AbstractLog log, String instanceName)
    {
        return rawLogDetails;
    }

    public static String formatLogEntry()
    {
        StringJoiner sb = new StringJoiner(Globals.GET_SEPARATOR());
        for (String record : rawLogDetails.values())
        {
            sb.add(record);
        }
        logEntry = sb.toString();
        return logEntry;
    }

    public void writeLogTable() throws LogException
    {
    }

    public void processLog(AbstractLog log) throws IOException
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
