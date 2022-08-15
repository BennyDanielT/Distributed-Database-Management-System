import QueryParsing.InvalidQueryException;
import QueryParsing.Parser;
import QueryParsing.Query;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InsertQueryUnitTests {

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
    void nothingAfterInsertIntoKeyword() {
        Parser parser = new Parser("insert into");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "incomplete query";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void noRowsSpecified() {
        Parser parser = new Parser("insert into employee");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "no rows to insert";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void noColumnsSpecified() {
        Parser parser = new Parser("insert into employee()");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected a valid column name";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void noRowsAfterColumns() {
        Parser parser = new Parser("insert into employee(id)");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "no rows to insert";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void noRowsAfterValuesKeyword() {
        Parser parser = new Parser("insert into employee(id) values");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "no rows to insert";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void emptyParenthAfterValuesKeyword() {
        Parser parser = new Parser("insert into employee(id) values ()");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "field count doesn't match with value count";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void noCommaBetweenFieldNames() {
        Parser parser = new Parser("insert into employee(id name) values ()");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "a comma or a closing parenthesis is expected";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void columnParenthMissing() {
        Parser parser = new Parser("insert into employee values (1,2)");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected opening parenthesis (";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }


    @Test
    void validQueryWithOneRow() {
        //Query mock object
        Query mockQuery =  mock(Query.class);

        //Mocking behaviour
        when(mockQuery.toString()).thenReturn("Query{type='INSERT', tableName='employee', databaseName='', fields=[id, name], valuesToUpdate={}, conditions=[], inserts=[[12, 'aman']], datatypes=[], constraints=[]}");

        // Parsing query
        Parser parser = new Parser("insert into employee(id, name) values(12,'aman')");
        Query parsedQuery = parser.parse();

        assertEquals(parsedQuery.toString(), mockQuery.toString());
    }

    @Test
    void validQueryWithTwoRows() {
        //Query mock object
        Query mockQuery =  mock(Query.class);

        //Mocking behaviour
        when(mockQuery.toString()).thenReturn("Query{type='INSERT', tableName='employee', databaseName='', fields=[id, name], valuesToUpdate={}, conditions=[], inserts=[[12, 'aman'], [13, 'amar']], datatypes=[], constraints=[]}");

        // Parsing query
        Parser parser = new Parser("insert into employee(id, name) values(12,'aman'),(13,'amar')");
        Query parsedQuery = parser.parse();

        assertEquals(parsedQuery.toString(), mockQuery.toString());
    }

    @Test
    void invalidQueryWithExtraComma() {
        Parser parser = new Parser("insert into employee(id, name) values(12,'aman'),(13,'amar'),");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected an opening parenthesis";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void invalidQueryWithExtraCharAtEnd() {
        Parser parser = new Parser("insert into employee(id, name) values(12,'aman'),(13,'amar'):");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "a comma , is expected";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void fieldNameIsAReservedWord() {
        Parser parser = new Parser("insert into employee(table, name) values(12,'aman'),(13,'amar')");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected a valid column name";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void tableNameIsAReservedWord() {
        Parser parser = new Parser("insert into values(id, name) values(12,'aman'),(13,'amar')");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected a valid table name";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }
}

