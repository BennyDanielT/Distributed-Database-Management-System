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

class CreateQueryUnitTests {

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
    void nothingAfterCreateKeyword() {
        Parser parser = new Parser("create ");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "incomplete query";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void nothingAfterDatabaseKeyword() {
        Parser parser = new Parser("create database");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "incomplete query";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void nothingAfterTableKeyword() {
        Parser parser = new Parser("create table");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "incomplete query";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void validCreateDatabaseQuery() {
        //Query mock object
        Query mockQuery =  mock(Query.class);

        //Mocking behaviour
        when(mockQuery.toString()).thenReturn("Query{type='CREATE', tableName='', databaseName='aman', fields=[], valuesToUpdate={}, conditions=[], inserts=[], datatypes=[], constraints=[]}");

        // Parsing query
        Parser parser = new Parser("create database aman");
        Query parsedQuery = parser.parse();

        assertEquals(parsedQuery.toString(), mockQuery.toString());
    }


    @Test
    void nothingAfterTableName() {
        Parser parser = new Parser("create table employee ");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected an opening parenthesis";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void nothingAfterOpeningParenthesis() {
        Parser parser = new Parser("create table employee(");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected a valid column name";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void noDataTypeSpecified() {
        Parser parser = new Parser("create table employee(id");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected a valid data type";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void noClosingParenth() {
        Parser parser = new Parser("create table employee(id FLOAT");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "a comma, a constraint or a closing parenthesis is expected";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void closingParenthAfterComma() {
        Parser parser = new Parser("create table employee(id FLOAT,)");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected a valid column name";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void validCreateTableQueryWithOneColumn() {
        //Query mock object
        Query mockQuery =  mock(Query.class);

        //Mocking behaviour
        when(mockQuery.toString()).thenReturn("Query{type='CREATE', tableName='employee', databaseName='', fields=[id], valuesToUpdate={}, conditions=[], inserts=[], datatypes=[FLOAT], constraints=[PRIMARY KEY]}");

        // Parsing query
        Parser parser = new Parser("create table employee(id FLOAT PRIMARY KEY)");
        Query parsedQuery = parser.parse();

        assertEquals(parsedQuery.toString(), mockQuery.toString());
    }

    @Test
    void validCreateTableQueryWithTwoColumn() {
        //Query mock object
        Query mockQuery =  mock(Query.class);

        //Mocking behaviour
        when(mockQuery.toString()).thenReturn("Query{type='CREATE', tableName='employee', databaseName='', fields=[id, name], valuesToUpdate={}, conditions=[], inserts=[], datatypes=[FLOAT, TEXT], constraints=[PRIMARY KEY, UNIQUE]}");

        // Parsing query
        Parser parser = new Parser("create table employee(id FLOAT PRIMARY KEY, name TEXT UNIQUE)");
        Query parsedQuery = parser.parse();

        assertEquals(parsedQuery.toString(), mockQuery.toString());
    }

    @Test
    void invalidQueryWithExtraCharAtEnd() {
        Parser parser = new Parser("create table employee(id FLOAT PRIMARY KEY, name TEXT UNIQUE):");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "invalid expression at end of query";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void fieldNameIsAReservedWord() {
        Parser parser = new Parser("create table employee(table FLOAT PRIMARY KEY, name TEXT UNIQUE)");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected a valid column name";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void tableNameIsAReservedWord() {
        Parser parser = new Parser("create table where(id FLOAT PRIMARY KEY, name TEXT UNIQUE)");
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            Query query = parser.parse();
        });

        String expectedMessage = "expected a valid table name";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    // Tests for foreign key constrain
    @Test
    void validCreateTableQueryWithOneColumn_FK() {
        //Query mock object
        Query mockQuery =  mock(Query.class);

        //Mocking behaviour
        when(mockQuery.toString()).thenReturn("Query{type='CREATE', tableName='employee', databaseName='', fields=[id], valuesToUpdate={}, conditions=[], inserts=[], datatypes=[FLOAT], constraints=[idd]}");

        // Parsing query
        Parser parser = new Parser("create table employee(id FLOAT idd)");
        Query parsedQuery = parser.parse();

        assertEquals(parsedQuery.toString(), mockQuery.toString());
    }

    @Test
    void validCreateTableQueryWithMultipleColumns_FK() {
        //Query mock object
        Query mockQuery =  mock(Query.class);

        //Mocking behaviour
        when(mockQuery.toString()).thenReturn("Query{type='CREATE', tableName='employee', databaseName='', fields=[id, idd, name, xyz], valuesToUpdate={}, conditions=[], inserts=[], datatypes=[FLOAT, TEXT, TEXT, FLOAT], constraints=[PRIMARY KEY, , UNIQUE, appy]}");

        // Parsing query
        Parser parser = new Parser("create table employee(id FLOAT PRIMARY KEY,idd TEXT, name text UNIQUE, xyz FLOAT appy)");
        Query parsedQuery = parser.parse();

        assertEquals(parsedQuery.toString(), mockQuery.toString());
    }

    // Tests for foreign key constrain
    @Test
    void validCreateTableQueryWithOneColumn_FK_Unique() {
        //Query mock object
        Query mockQuery =  mock(Query.class);

        //Mocking behaviour
        when(mockQuery.toString()).thenReturn("Query{type='CREATE', tableName='employee', databaseName='', fields=[id], valuesToUpdate={}, conditions=[], inserts=[], datatypes=[FLOAT], constraints=[idd\tU]}");

        // Parsing query
        Parser parser = new Parser("create table employee(id FLOAT idd Unique)");
        Query parsedQuery = parser.parse();

        assertEquals(parsedQuery.toString(), mockQuery.toString());
    }

    @Test
    void validCreateTableQueryWithMultipleColumns_FK_Unique() {
        //Query mock object
        Query mockQuery =  mock(Query.class);

        //Mocking behaviour
        when(mockQuery.toString()).thenReturn("Query{type='CREATE', tableName='employee', databaseName='', fields=[id, idd, name, xyz], valuesToUpdate={}, conditions=[], inserts=[], datatypes=[FLOAT, TEXT, TEXT, FLOAT], constraints=[PRIMARY KEY, , UNIQUE, appy\tU]}");

        // Parsing query
        Parser parser = new Parser("create table employee(id FLOAT PRIMARY KEY,idd TEXT, name text UNIQUE, xyz FLOAT appy Unique)");
        Query parsedQuery = parser.parse();

        assertEquals(parsedQuery.toString(), mockQuery.toString());
    }
}

