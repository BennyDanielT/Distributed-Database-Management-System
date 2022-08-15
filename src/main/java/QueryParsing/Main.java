package QueryParsing;

import QueryExecution.ExecutionDriver;
import com.dpg7.main.FileProcessor;
import com.dpg7.main.Globals;
import com.dpg7.main.Initializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IllegalAccessException, IOException {
//        Initializer IZ = new Initializer();
//        FileProcessor LOCAL_FILE_PROCESSOR = new FileProcessor(false);
//        FileProcessor REMOTE_FILE_PROCESSOR = new FileProcessor(true);
//        ExecutionDriver executionDriver = new ExecutionDriver(LOCAL_FILE_PROCESSOR, null);

//        String remoteTablePath = Globals.GET_TABLE_PATH("HardwareStore", "Customers");
//        String remoteTablePath = "/home/imaphong12/csci-5408-w2022-dpg7/DATABASE_ROOT/SCHEMAS/Customers/HardwareStore.txt";
//        System.out.println(remoteTablePath);
//        System.out.println(LOCAL_FILE_PROCESSOR.isRemote);
//        List<LinkedHashMap<String, String>> remoteFile = REMOTE_FILE_PROCESSOR.readFile(remoteTablePath);
//        Parser useQuery = new Parser("Use Customers");
//        try {
//            executionDriver.executeUse(useQuery.parse());
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
        Parser parser = new Parser("UPDATE Customers SET Name='Jeremiah' WHERE Name='John'");
        System.out.println(parser.parse());
//        executionDriver.executeCreateTable(parser.parse());
    }
}
