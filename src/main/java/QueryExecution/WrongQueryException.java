package QueryExecution;

public class WrongQueryException extends RuntimeException {
    public WrongQueryException(String errorMessage) {
        super(errorMessage);
    }
}
