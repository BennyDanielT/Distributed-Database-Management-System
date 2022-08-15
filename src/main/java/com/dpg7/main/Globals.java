package com.dpg7.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Globals {
    private static String ROOT = "DATABASE_ROOT/";
    private static String SCHEMA_PATH = "SCHEMAS/";

    public static String PRIMARY_KEY = "PRIMARY KEY";
    public static String TRUE = "True";
    public static String FALSE = "False";
    public static String INSTANCE_ONE = "1";
    public static String INSTANCE_TWO = "2";

    private static String METADATA = "METADATA/";
    private static String LOGS = "LOGS/";
    private static String LOGS_PATH = "LOGS/";
    public static String QUERY_LOG = "QUERY_LOG";
    public static String EVENT_LOG = "EVENT_LOG";
    public static String GENERAL_LOG = "GENERAL_LOG";
    private static String SEPARATOR = "\t";
    private static String FILE_EXT = ".txt";
    public static String[] queryLogHeaders = new String[] { "logTimestamp", "instanceName", "databaseName", "tableName",
            "userId", "queryValidity", "queryType", "queryExecutionTime", "query" };
    public static String[] eventLogHeaders = new String[] { "logTimestamp", "instanceName", "databaseName", "tableName",
            "userId", "eventTimestamp", "eventMessage" };
    public static String[] generalLogHeaders = new String[] { "logTimestamp", "instanceName", "databaseName",
            "tableName",
            "userId", "numberOfTables", "numberOfRecords" };

    public static String GET_FILE_EXT() {
        return FILE_EXT;
    }

    public static String GET_ROOT() {
        System.out.println("Current dir: " + System.getProperty("user.dir"));
        return System.getProperty("user.dir") + File.separator + Globals.ROOT;
    }

    public static String GET_SCHEMA_PATH() {
        return System.getProperty("user.dir") + File.separator + Globals.ROOT + Globals.SCHEMA_PATH;
    }

    public static String GET_GLOBAL_METADATA() {
        return System.getProperty("user.dir") + File.separator + Globals.ROOT + Globals.METADATA + "global.txt";
    }

    public static String GET_LOG_PATH() {
        return GET_ROOT() + Globals.LOGS_PATH;
    }

    public static String GET_QUERY_LOG_PATH() {
        return GET_ROOT() + Globals.LOGS_PATH + QUERY_LOG + Globals.FILE_EXT;
    }

    public static String GET_EVENT_LOG_PATH() {
        return GET_ROOT() + Globals.LOGS_PATH + EVENT_LOG + Globals.FILE_EXT;
    }

    public static String GET_GENERAL_LOG_PATH() {
        return GET_ROOT() + Globals.LOGS_PATH + GENERAL_LOG + Globals.FILE_EXT;
    }

    public static String GET_METADATA() {
        return GET_ROOT() + Globals.METADATA;
    }

    public static String GET_USER_PROFILE_FILE() {
        return GET_ROOT() + File.separator + "USER_PROFILE" + Globals.GET_FILE_EXT();
    }

    public static String getMETADATA() {
        return METADATA;
    }

    public static String GET_SEPARATOR() {
        return SEPARATOR;
    }

    public static String GET_SCHEMA_METADATA_PATH(String schemaName) {
        return GET_SCHEMA_PATH() + schemaName + File.separator + METADATA;
    }

    public static String GET_LOCAL_METADATA_PATH(String tableName, String schemaName) {
        return GET_SCHEMA_METADATA_PATH(schemaName) + tableName + ".txt";
    }

    public static String GET_TABLE_PATH(String tableName, String schemaName) {
        String schemaPath = GET_SCHEMA_PATH() + schemaName;
        return schemaPath + File.separator + tableName + ".txt";
    }

}
