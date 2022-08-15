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

class SelectQueryUnitTests {

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
    void nothingAfterSelectKeyword() {
        Parser parser = new Parser("select");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected a field name in query";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void nothingAfterFieldNames() {
        Parser parser = new Parser("select *");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected FROM keyword in query";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void noCommaBetweenFieldNames() {
        Parser parser = new Parser("select id emp from employee");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected a comma or FROM";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void noTableNameAfterFromKeyword() {
        Parser parser = new Parser("select id, emp from ");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "incomplete query";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void validQueryWithAsteriskAndSemicolon() {
        //Query mock object
        Query mockQuery =  mock(Query.class);

        //Mocking behaviour
        when(mockQuery.toString()).thenReturn("Query{type='SELECT', tableName='employee', databaseName='', fields=[*], valuesToUpdate={}, conditions=[], inserts=[], datatypes=[], constraints=[]}");

        // Parsing query
        Parser parser = new Parser("select * from employee;");
        Query parsedQuery = parser.parse();

        assertEquals(parsedQuery.toString(), mockQuery.toString());
    }

    @Test
    void validQueryWithAsteriskAndNoSemicolon() {
        //Query mock object
        Query mockQuery =  mock(Query.class);

        //Mocking behaviour
        when(mockQuery.toString()).thenReturn("Query{type='SELECT', tableName='employee', databaseName='', fields=[*], valuesToUpdate={}, conditions=[], inserts=[], datatypes=[], constraints=[]}");

        // Parsing query
        Parser parser = new Parser("select * from employee;");
        Query parsedQuery = parser.parse();

        assertEquals(parsedQuery.toString(), mockQuery.toString());
    }

    @Test
    void validQueryWithOneFieldAndSemicolon() {

        //Query mock object
        Query mockQuery =  mock(Query.class);

        //Mocking behaviour
        when(mockQuery.toString()).thenReturn("Query{type='SELECT', tableName='employee', databaseName='', fields=[id], valuesToUpdate={}, conditions=[], inserts=[], datatypes=[], constraints=[]}");

        // Parsing query
        Parser parser = new Parser("select id from employee;");
        Query parsedQuery = parser.parse();

        assertEquals(parsedQuery.toString(), mockQuery.toString());
    }

    @Test
    void validQueryWithOneFieldAndNoSemicolon() {

        //Query mock object
        Query mockQuery =  mock(Query.class);

        //Mocking behaviour
        when(mockQuery.toString()).thenReturn("Query{type='SELECT', tableName='employee', databaseName='', fields=[id], valuesToUpdate={}, conditions=[], inserts=[], datatypes=[], constraints=[]}");

        // Parsing query
        Parser parser = new Parser("select id from employee;");
        Query parsedQuery = parser.parse();

        assertEquals(parsedQuery.toString(), mockQuery.toString());
    }

    @Test
    void validQueryWithMultipleFieldsAndSemicolon() {

        //Query mock object
        Query mockQuery =  mock(Query.class);

        //Mocking behaviour
        when(mockQuery.toString()).thenReturn("Query{type='SELECT', tableName='employee', databaseName='', fields=[id, name], valuesToUpdate={}, conditions=[], inserts=[], datatypes=[], constraints=[]}");

        // Parsing query
        Parser parser = new Parser("select id, name from employee;");
        Query parsedQuery = parser.parse();

        assertEquals(parsedQuery.toString(), mockQuery.toString());
    }

    @Test
    void validQueryWithMultipleFieldsAndNoSemicolon() {

        //Query mock object
        Query mockQuery =  mock(Query.class);

        //Mocking behaviour
        when(mockQuery.toString()).thenReturn("Query{type='SELECT', tableName='employee', databaseName='', fields=[id, name], valuesToUpdate={}, conditions=[], inserts=[], datatypes=[], constraints=[]}");

        // Parsing query
        Parser parser = new Parser("select id, name from employee;");
        Query parsedQuery = parser.parse();

        assertEquals(parsedQuery.toString(), mockQuery.toString());
    }

    @Test
    void validQueryWithExtraComma() {
        Parser parser = new Parser("select id, name, from employee;");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "is not a valid SELECT query";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void FieldNameIsAReservedWord() {
        Parser parser = new Parser("select ( from employee;");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "is not a valid SELECT query";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void tableNameIsAReservedWord() {
        Parser parser = new Parser("select * from select;");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "invalid table name";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }
}

