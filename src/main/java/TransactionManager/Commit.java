package TransactionManager;

import com.dpg7.main.FileProcessor;

import java.util.HashMap;
import java.util.List;

public class Commit {
    public static void doCommit(List<HashMap<String,String>> dataInMemory) {
        FileProcessor.submitTransaction(dataInMemory);
    }
}
