package analytics;

import com.dpg7.main.FileProcessor;
import com.dpg7.main.Globals;
import com.dpg7.main.MetaData.GlobalMetaData;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


public class Analytics {
    public static void main() {
        final Scanner scanner = new Scanner(System.in);

        System.out.println("1. Queries Submitted By Database.");
        System.out.println("2. Update/Select/Create Operations By Tables.");
        System.out.println("");
        System.out.print("Select Option:");

        String userInput = scanner.nextLine();
        switch (userInput) {
            case "1":
                // Handle Write queries
                System.out.println("Generating Reports...");
                String logPath = Globals.GET_QUERY_LOG_PATH();
                System.out.println();
                getUserAnalytics(Globals.GET_QUERY_LOG_PATH());
                break;
            case "2":
                System.out.println("Enter Operation [UPDATE,SELECT,INSERT,CREATE] :");
                String operation = scanner.nextLine();

                System.out.println("Enter databaseName :");
                String databaseName = scanner.nextLine();
                System.out.println("");

                getQueryAnalytics(Globals.GET_QUERY_LOG_PATH(), operation, databaseName);
                break;
            default:
                System.out.println("Invalid input.");
                break;
        }
    }

    public static List<String> getAllLogs(String filePath) {
        List<String> vm1Logs = new ArrayList<>();
        try {
            FileProcessor localfileProcessor = new FileProcessor(false);
            FileProcessor remoteFileProcessor = new FileProcessor(true);

            vm1Logs = localfileProcessor.readLogFile(filePath);
            List<String> vm2Logs = remoteFileProcessor.readLogFile(filePath);
            vm1Logs.addAll(vm2Logs);
            System.out.println("recieved all logs" + vm1Logs.size());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return vm1Logs;
    }

    public static void getUserAnalytics(String fileName) {
        List<String> userList = new ArrayList<>();

        BufferedReader br = null;

        List<String> userAnalyticsResult = new ArrayList<>();
        String content="";

        // for databases
        try {
            br = new BufferedReader(new FileReader(Globals.GET_USER_PROFILE_FILE()));
            String line = "";
            while ((line = br.readLine()) != null) {
                String tokens[] = line.split(Globals.GET_SEPARATOR());
                userList.add(tokens[0]);
            }
            HashMap<String, List<String>> instanceRecordsMap = populateDataFromLog(fileName);
            Set<String> databases = getAllDatabaseNames(instanceRecordsMap);

            for (String instanceName : instanceRecordsMap.keySet()) {
                List<String> instanceRecords = instanceRecordsMap.get(instanceName);
                for (String db : databases) {
                    List<String> currentDbs = new ArrayList<>();
                    for (String vmRecord : instanceRecords) {
                        if (vmRecord.contains(db)) {
                            currentDbs.add(vmRecord);
                        }
                    }
                    for (String user : userList) {
                        int count = 0;
                        for (String currentDbRecord : currentDbs) {
                            String[] tokens = currentDbRecord.split(Globals.GET_SEPARATOR());
                            if (tokens[4].equalsIgnoreCase(user)) {
                                count++;
                            }
                        }
                        if (count == 0) continue;
                        String result = "user " + user + " submitted " + count + " queries for " + db + " running on " + instanceName;

                        System.out.println(result);
                        userAnalyticsResult.add(result);
                        content+= result+"\n";
                    }
                }
            }

            generateOutput(content,"userAnalytics.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static HashMap<String, List<String>> populateDataFromLog(String fileName) {
        HashMap<String, List<String>> logsFilterByInstance = new HashMap<>();
        List<String> logs = getAllLogs(fileName);

        List<String> instanceOneRecords = logs.stream().filter(row -> {
            String[] splittedRow = row.split(Globals.GET_SEPARATOR());
            return splittedRow[1].equalsIgnoreCase(Globals.INSTANCE_ONE);
        }).collect(Collectors.toList());

        List<String> instanceTwoRecords = logs.stream().filter(row -> {
            String[] splittedRow = row.split(Globals.GET_SEPARATOR());
            return splittedRow[1].equalsIgnoreCase(Globals.INSTANCE_TWO);
        }).collect(Collectors.toList());

        logsFilterByInstance.put("1", instanceOneRecords);
        logsFilterByInstance.put("2", instanceTwoRecords);
        return logsFilterByInstance;
    }

    //fileName of log
    public static void getQueryAnalytics(String fileName, String operation, String databaseName) {
        String queryType = operation;
        int count = 0;
        try {
            List<String> allRecordsForDB = new ArrayList<>();
            HashMap<String, List<String>> instanceRecordsMap = populateDataFromLog(fileName);

            allRecordsForDB = filterRecordsByDbQuery(databaseName, queryType, instanceRecordsMap);
            Set<String> tables = getAllTablesByDB(databaseName, queryType, instanceRecordsMap);
            String result = "";
            String content = "";
            // Get counts for a given table
            for (String table : tables) {
                count = 0;
                for (String record : allRecordsForDB) {
                    String[] tokens = record.split(Globals.GET_SEPARATOR());
                    if (table.equalsIgnoreCase(tokens[3]))
                        count++;
                }
                if (count != 0) {
                    result = "Total " + count + " " + queryType + " operations are performed on " + table;
                    System.out.println(result);
                    content+=result+"\n";
                }

            }
            generateOutput(content,"queryAnalytics.txt");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Set<String> getAllTablesByDB(String databaseName, String queryType, HashMap<String, List<String>> instanceRecordsMap) {

        Set<String> tables = new HashSet<>();

        List<String> filterRecordsByQuery = filterRecordsByDbQuery(databaseName, queryType, instanceRecordsMap);


        for (String record : filterRecordsByQuery) {
            String[] splittedRow = record.split(Globals.GET_SEPARATOR());
            String table = splittedRow[3];
            if (table != "NULL") {
                tables.add(table);
            }
        }
        return tables;
    }

    private static List<String> filterRecordsByDbQuery(String databaseName, String queryType, HashMap<String, List<String>> instanceRecordsMap) {

        List<String> totalRecords = new ArrayList<>();
        for (String instanceName : instanceRecordsMap.keySet()) {
            List<String> instanceRecords = instanceRecordsMap.get(instanceName);
            List<String> records = instanceRecords.stream().filter(row -> {
                String[] splittedRow = row.split(Globals.GET_SEPARATOR());
                return splittedRow[2].equalsIgnoreCase(databaseName);
            }).collect(Collectors.toList());
            totalRecords.addAll(records);
        }

        List<String> recordsByQuery = totalRecords.stream().filter(row -> {
            String[] splittedRow = row.split(Globals.GET_SEPARATOR());
            return splittedRow[6].equalsIgnoreCase(queryType);
        }).collect(Collectors.toList());
        return recordsByQuery;
    }


    public static Set<String> getAllDatabaseNames ( HashMap<String, List<String>> instanceRecordsMap) {
        Set<String> databases = new HashSet<>();
        for (String instanceName : instanceRecordsMap.keySet()) {
            List<String> instanceRecords = instanceRecordsMap.get(instanceName);
            for (String record : instanceRecords) {
                String[] tokens = record.split(Globals.GET_SEPARATOR());
                databases.add(tokens[2]);
            }
        }
        return databases;
    }

    private static void generateOutput(String result, String analyticsFile) {

        String filePath = Globals.GET_ROOT() + File.separator + analyticsFile;

        FileWriter fileWriter = null;
        try {
            File analyticFile = new File(filePath);
            if (!analyticFile.exists()) {
                Path newFilePath = Paths.get(filePath);
                Files.createFile(newFilePath);
            }
            fileWriter = new FileWriter(analyticFile);
            fileWriter.write(result);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
