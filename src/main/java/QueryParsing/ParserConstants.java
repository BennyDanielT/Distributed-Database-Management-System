package QueryParsing;

import com.dpg7.main.Globals;

// Defines all transition states while parsing, reserved keywords, valid operators, datatypes and valid constraints
public final class ParserConstants {
    static final String[] RESERVED_PHRASES = {
            "CREATE", "USE", "SELECT", "UPDATE", "DELETE FROM", "FROM", "INSERT INTO", "SET", "VALUES", "WHERE",
            "BEGIN TRANSACTION", "END TRANSACTION", "COMMIT", "ROLLBACK", "=", ",", ">", "<", "<=", ">=", "(", ")",
            "TABLE"
    }; // All the reserved words used in SQL
    static final String[] VALID_OPERATORS = {
            "=", "<", ">", ">=", "<=", "!="
    }; // Valid operators
    static final String[] DATA_TYPES = {
            "TEXT", "FLOAT"
    };// All the valid datatypes
    static final String[] VALID_CONSTRAINTS = {
            Globals.PRIMARY_KEY, "UNIQUE"
    };// All the valid datatypes

    // Enum to describe the current state of parser
    public enum ParsingStep {
        stepType,
        stepSelectField, stepSelectFrom, stepSelectComma, stepSelectTable,
        stepUpdateField, stepUpdateTable, stepUpdateSet, stepUpdateEquals, stepUpdateValue, stepUpdateComma,
        stepInsertTable, stepInsertFieldOpeningParenth, stepInsertFields, stepInsertCommaOrClosingParenth,
        stepInsertValuesReservedWord, stepInsertValue, stepInsertValuesCommaOrClosingParenth,
        stepInsertValuesCommaBeforeOpeningParenth, stepInsertValueOpeningParenth,
        stepDeleteFromTable,
        stepWhere, stepWhereField, stepWhereOperator, stepWhereValue, stepWhereAnd,
        stepInputDatabase,
        stepCreate, stepCreateDatabaseOrTable, stepCreateTableName, stepCreateFieldOpeningParenth, stepCreateFields,
        stepCreateDataType, stepCreateConstraint, stepCreateCommaOrClosingParenth,
        stepEndDBQuery
    }
}
