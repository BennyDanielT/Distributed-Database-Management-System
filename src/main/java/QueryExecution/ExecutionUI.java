package QueryExecution;

import QueryParsing.InvalidQueryException;
import QueryParsing.Parser;
import QueryParsing.Query;
import com.dpg7.main.FileProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class ExecutionUI {

    FileProcessor LOCAL_FILE_PROCESSOR;
    FileProcessor REMOTE_FILE_PROCESSOR;

    public ExecutionUI(FileProcessor L, FileProcessor R) {
        LOCAL_FILE_PROCESSOR = L;
        REMOTE_FILE_PROCESSOR = R;
    }

    public void start() {

        ExecutionDriver executionDriver = new ExecutionDriver(LOCAL_FILE_PROCESSOR, REMOTE_FILE_PROCESSOR);
        System.out.println("Execution driver initiated");
        while (true) {
            final Scanner scanner = new Scanner(System.in);
            final String query = scanner.nextLine();
            if (query.equals("q")) {
                break;
            }

            Parser parser = new Parser(query);
            Query parsedQuery = parser.parse();

            try {
                switch (parsedQuery.getType()) {
                    case "CREATE":
                        if (parsedQuery.getDatabaseName().equals("")) {
                            executionDriver.executeCreateTable(parsedQuery);
                        } else {
                            executionDriver.executeCreateDatabase(parsedQuery);
                        }
                        break;
                    case "USE":
                        executionDriver.executeUse(parsedQuery);
                        System.out.println("Using database " + parsedQuery.getDatabaseName());
                        break;
                    case "INSERT":
                        executionDriver.executeInsert(parsedQuery);
                        System.out.println("Inserted successfully");
                        break;
                    case "DELETE":
                        executionDriver.executeDelete(parsedQuery);
                        System.out.println("Deleted successfully");
                        break;
                    case "UPDATE":
                        executionDriver.executeUpdate(parsedQuery);
                        System.out.println("Updated successfully");
                        break;
                    case "SELECT":
                        executionDriver.executeSelect(parsedQuery);
                }
            } catch (IOException | IllegalAccessException | InvalidQueryException e) {
                e.printStackTrace();
                System.out.println("Error in creating table");
            }

        }
    }
}
