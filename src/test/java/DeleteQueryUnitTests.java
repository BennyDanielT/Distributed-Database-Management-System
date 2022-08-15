import QueryParsing.InvalidQueryException;
import QueryParsing.Parser;
import QueryParsing.Query;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeleteQueryUnitTests {

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
    void nothingAfterDeleteKeyword() {
        Parser parser = new Parser("delete from");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "incomplete query";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void validQueryWithNoWhere() {
        //Query mock object
        Query mockQuery =  mock(Query.class);

        //Mocking behaviour
        when(mockQuery.toString()).thenReturn("Query{type='DELETE', tableName='employee', databaseName='', fields=[], valuesToUpdate={}, conditions=[], inserts=[], datatypes=[], constraints=[]}");

        // Parsing query
        Parser parser = new Parser("delete from employee");
        Query parsedQuery = parser.parse();

        assertEquals(parsedQuery.toString(), mockQuery.toString());
    }

    @Test
    void noConditionsAfterWhereClause() {
        Parser parser = new Parser("delete from employee where");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "invalid condition in WHERE clause";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void conditionPartiallySpecified() {
        Parser parser = new Parser("delete from employee where id");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "invalid condition in WHERE clause";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void invalidOperatorInCondition() {
        Parser parser = new Parser("delete from employee where id*2");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "invalid condition in WHERE clause";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void tableNameIsAReservedKeyword() {
        Parser parser = new Parser("delete from table where id=2");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected a valid table name";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }


    @Test
    void validQueryWithOneCondition() {
        //Query mock object
        Query mockQuery =  mock(Query.class);

        //Mocking behaviour
        when(mockQuery.toString()).thenReturn("Query{type='DELETE', tableName='employee', databaseName='', fields=[], valuesToUpdate={}, conditions=[[id, =, 1]], inserts=[], datatypes=[], constraints=[]}");

        // Parsing query
        Parser parser = new Parser("delete from employee where id=1");
        Query parsedQuery = parser.parse();

        assertEquals(parsedQuery.toString(), mockQuery.toString());
    }

    @Test
    void validQueryWithMultipleConditions() {
        //Query mock object
        Query mockQuery =  mock(Query.class);

        //Mocking behaviour
        when(mockQuery.toString()).thenReturn("Query{type='DELETE', tableName='employee', databaseName='', fields=[], valuesToUpdate={}, conditions=[[id, =, 1], [name, =, aman]], inserts=[], datatypes=[], constraints=[]}");

        // Parsing query
        Parser parser = new Parser("delete from employee where id=1 and name=aman");
        Query parsedQuery = parser.parse();

        assertEquals(parsedQuery.toString(), mockQuery.toString());
    }



    @Test
    void invalidQueryWithExtraCharAtEnd() {
        Parser parser = new Parser("delete from employee :");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected a WHERE clause";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void fieldNameIsAReservedWord() {
        Parser parser = new Parser("delete from employee where table=2");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected a valid field in WHERE clause";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void tableNameIsAReservedWord() {
        Parser parser = new Parser("delete from where");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected a valid table name";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }
}

