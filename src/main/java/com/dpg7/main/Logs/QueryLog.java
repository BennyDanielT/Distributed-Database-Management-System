package com.dpg7.main.Logs;

import QueryParsing.Query;
import com.dpg7.main.Globals;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class QueryLog extends AbstractLog
{
    public String logTimestamp;//Log Entry Timestamp
    public String instanceName;
    public String databaseName;
    public String tableName;
    public String userId;
    public String queryValidity;
    public String queryType;
    public String queryExecutionTime;
    public String query;

    }
