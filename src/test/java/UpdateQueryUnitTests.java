import QueryParsing.InvalidQueryException;
import QueryParsing.Parser;
import QueryParsing.Query;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateQueryUnitTests {

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
    void nothingAfterUpdateKeyword() {
        Parser parser = new Parser("update");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "incomplete query";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void noSetKeyword() {
        Parser parser = new Parser("update employee");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected a SET keyword";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void noColumnsToSet() {
        Parser parser = new Parser("update employee set");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected fields to update";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void noEqualToSign() {
        Parser parser = new Parser("update employee set id");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected an equal sign";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void noValueToSet() {
        Parser parser = new Parser("update employee set id=");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected a valid value to update";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void noFieldAfterComma() {
        Parser parser = new Parser("update employee set id=2,");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected fields to update";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void noConditionAfterWhere() {
        Parser parser = new Parser("update employee set id=2,");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected fields to update";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void noConditionsAfterWhereClause() {
        Parser parser = new Parser("update employee set id=2 where");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "invalid condition in WHERE clause";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void conditionPartiallySpecified() {
        Parser parser = new Parser("update employee set id=2 where name=");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "invalid condition in WHERE clause";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void invalidOperatorInCondition() {
        Parser parser = new Parser("update employee set id=2 where name*2");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "invalid condition in WHERE clause";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void tableNameIsAReservedKeyword() {
        Parser parser = new Parser("update from set id=2");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected a valid table name";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void fieldNameIsAReservedKeyword() {
        Parser parser = new Parser("update employee set table=2");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected a valid field to update in query";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }


    @Test
    void validQueryWithOneCondition() {
        //Query mock object
        Query mockQuery =  mock(Query.class);

        //Mocking behaviour
        when(mockQuery.toString()).thenReturn("Query{type='UPDATE', tableName='employee', databaseName='', fields=[], valuesToUpdate={id=2}, conditions=[[name, =, 'aman']], inserts=[], datatypes=[], constraints=[]}");

        // Parsing query
        Parser parser = new Parser("update employee set id=2 where name='aman'");
        Query parsedQuery = parser.parse();

        assertEquals(parsedQuery.toString(), mockQuery.toString());
    }

    @Test
    void validQueryWithMultipleConditions() {
        //Query mock object
        Query mockQuery =  mock(Query.class);

        //Mocking behaviour
        when(mockQuery.toString()).thenReturn("Query{type='UPDATE', tableName='employee', databaseName='', fields=[], valuesToUpdate={id=2}, conditions=[[name, =, 'aman'], [salary, >, 1000]], inserts=[], datatypes=[], constraints=[]}");

        // Parsing query
        Parser parser = new Parser("update employee set id=2 where name='aman' and salary>1000");
        Query parsedQuery = parser.parse();

        assertEquals(parsedQuery.toString(), mockQuery.toString());
    }


    @Test
    void validQueryWithMultipleUpdateFields() {
        //Query mock object
        Query mockQuery =  mock(Query.class);

        //Mocking behaviour
        when(mockQuery.toString()).thenReturn("Query{type='UPDATE', tableName='employee', databaseName='', fields=[], valuesToUpdate={id=2, salary=5000}, conditions=[[name, =, 'aman']], inserts=[], datatypes=[], constraints=[]}");

        // Parsing query
        Parser parser = new Parser("update employee set id=2,salary=5000 where name='aman'");
        Query parsedQuery = parser.parse();

        assertEquals(parsedQuery.toString(), mockQuery.toString());
    }

    @Test
    void invalidQueryWithExtraCharAtEnd() {
        Parser parser = new Parser("update employee set id=2,salary=5000 where name='aman'$");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected AND in query";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }
}

