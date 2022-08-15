package com.dpg7.main;

import com.dpg7.main.ERDiagram.ERDiagram;
import export.ExportDatabase;
import user.LoginUser;
import user.RegisterUser;

import QueryExecution.ExecutionUI;
import analytics.Analytics;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.dpg7.main.ERDiagram.ERDiagram;

public class Driver {

    public Driver() {

    }

    public static void main(String[] args) throws IOException {
        Initializer IZ = new Initializer();
        IZ.initializeRoot();

        try (InputStream input = FileProcessor.class.getClassLoader().getResourceAsStream("config.properties")) {

            Properties prop = new Properties();

            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }

            // load a properties file from class path, inside static method
            prop.load(input);

            // get the property value and print it out
            State.getInstance().setCurrentVM(prop.getProperty("vm"));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // LOCAL_FILE_PROCESSOR.printStructure();

        // LOCAL_FILE_PROCESSOR.createSchema("Customers");
        //
        // List<LinkedHashMap<String, String>> r = new ArrayList<>();
        //
        // LinkedHashMap<String, String> a = new LinkedHashMap<>();
        // a.put("ID", "1");
        // a.put("Name", "John Doe");
        // a.put("Amount", "45");
        //
        // LinkedHashMap<String, String> b = new LinkedHashMap<>();
        // b.put("ID", "2");
        // b.put("Name", "Jane Doe");
        // b.put("Amount", "94");
        //
        // r.add(a);
        // r.add(b);
        // Table table = new Table(r, "Customers");

        // QUERY GETS PARSED HERE....
        // Query query = "Query{type='CREATE', tableName='EMPLOYEE', databaseName='',
        // fields=[ID, NAME], valuesToUpdate={}, conditions=[], inserts=[],
        // datatypes=[INTEGER, VARCHAR]}";

        // EventLogManager eventLogManager = EventLogManager.getInstance();
        // eventLogManager.processLog(query,"Table Created Successfully");

        // GeneralLogManager generalLogManager = GeneralLogManager.getInstance();
        // generalLogManager.processLog(query);

        // read query, process data and write to file
        // LOCAL_FILE_PROCESSOR.processGlobalMetaData(query);
        // REMOTE_FILE_PROCESSOR.processGlobalMetaData(query);
        //
        // LOCAL_FILE_PROCESSOR.processLocalMetaData(query);

        // try {
        // LOCAL_FILE_PROCESSOR.writeTable("HardwareStore", "Customers", table);
        // // LOCAL_FILE_PROCESSOR.writeMetaTable("HardwareStore", "Customers",
        // // metadataTable);
        // } catch (IOException e) {
        // e.printStackTrace();
        // }

        // Initialize the state
        State state = State.getInstance();

        if (state.getIsUserLoggedIn()) {
            showMainMenu();
        } else {
            showRegisterLoginMenu();
        }
    }

    public static void showRegisterLoginMenu() {
        final Scanner scanner = new Scanner(System.in);

        for (;;) {
            System.out.println();
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.println();

            System.out.print("Select an option: ");

            final String userInput = scanner.nextLine();
            System.out.println();

            switch (userInput) {
                case "1":
                    RegisterUser registerUser = new RegisterUser();
                    registerUser.handleRegistration();
                    break;

                case "2":
                    LoginUser loginUser = new LoginUser();
                    loginUser.handleLogin();
                    break;

                case "3":
                    System.out.println("Bye bye.");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid input.");
                    break;
            }

            if (State.getInstance().getIsUserLoggedIn()) {
                break;
            }
        }

        showMainMenu();
    }

    public static void showMainMenu() {

        final Scanner scanner = new Scanner(System.in);
        FileProcessor LOCAL_FILE_PROCESSOR = new FileProcessor(false);
        FileProcessor REMOTE_FILE_PROCESSOR = new FileProcessor(true);

        for (;;) {
            System.out.println();
            System.out.println("1. Write queries");
            System.out.println("2. Export");
            System.out.println("3. Data model");
            System.out.println("4. Analytics");
            System.out.println("5. Exit");
            System.out.println();

            System.out.print("Select an option: ");

            final String userInput = scanner.nextLine();
            System.out.println();
            switch (userInput) {
                case "1":
                    // Handle Write queries
                    ExecutionUI executionUI = new ExecutionUI(LOCAL_FILE_PROCESSOR, REMOTE_FILE_PROCESSOR);
                    executionUI.start();
                    break;

                case "2":
                    // Handle Export
                    ExportDatabase exportDatabase = new ExportDatabase();
                    exportDatabase.handleExportDB();
                    break;

                case "3":
                    // Handle Data model
                    System.out.println("------------------ Handle Data model -----------------------");
                    System.out.println("Enter schema name: ");
                    String schemaInp = scanner.nextLine();
                    try {
                        ERDiagram.generateERD(schemaInp);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    break;

                case "4":
                    // Handle Analytics
                    Analytics.main();
                    break;

                case "5":
                    // TODO :: JAY :: LOG THIS INFORMATION
                    System.out.println("Bye bye " + State.getInstance().getLoggedInUser().getUserID());

                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid input.");
                    break;
            }
        }
    }

    // private static void processGlobalMetaData(Query query) throws IOException {
    // // Global Meta Data processing and entry to global file

    // String globalMetaFilePath = Globals.GET_METADATA() + "globalMetaData" +
    // Globals.GET_FILE_EXT();
    // GlobalMetaData globalMetaData =
    // GlobalMetaData.getGlobalMetaInstance(globalMetaFilePath);
    // globalMetaData.constructMetaInfo(query, new String[]{"instance", "schema",
    // "table"}, "GCP-1");
    // globalMetaData.formatMetaData();
    // globalMetaData.writeMetaTable();
    // }

    // private static void processLocalMetaData(Query query) throws IOException {
    // // Table Meta Data processing and entry to local file

    // String tableMetaFilePath = Globals.GET_SCHEMA_PATH() +
    // query.getDatabaseName() + File.separator + Globals.getMETADATA() +
    // File.separator + query.getTableName() + Globals.GET_FILE_EXT();
    // TableMetaData tableMetaData = new TableMetaData(tableMetaFilePath);
    // tableMetaData.constructMetaInfo(query, new String[]{"Column_Name",
    // "Datatype", "Key", "Unique"}, "GCP-1");
    // tableMetaData.formatMetaData();
    // tableMetaData.writeMetaTable();
    // }

}
