package QueryParsing;

// A custom exception for invalid queries
// The query will only be detected during Runtime, hence runtime exception
public class InvalidQueryException extends RuntimeException{
    public InvalidQueryException(String errorMessage) {
        super(errorMessage);
    }
}
