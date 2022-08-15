import QueryParsing.InvalidQueryException;
import QueryParsing.Parser;
import QueryParsing.Query;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UseQueryUnitTests {

    @Test
    void emptyQuery() {
        Parser parser = new Parser("");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "query cannot be empty or null";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void nothingAfterUseKeyword() {
        Parser parser = new Parser("use");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "incomplete query";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void validUseQuery() {
        //Query mock object
        Query mockQuery =  mock(Query.class);

        //Mocking behaviour
        when(mockQuery.toString()).thenReturn("Query{type='USE', tableName='', databaseName='amar', fields=[], valuesToUpdate={}, conditions=[], inserts=[], datatypes=[], constraints=[]}");

        // Parsing query
        Parser parser = new Parser("use amar");
        Query parsedQuery = parser.parse();

        assertEquals(parsedQuery.toString(), mockQuery.toString());
    }



}

