package TransactionManager;

import com.dpg7.main.FileProcessor;
import com.dpg7.main.State;

public class RollBack {
    public static void doARollback() {
        try {
            new FileProcessor(false).loadSchema(State.getInstance().getCurrentSchema());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
