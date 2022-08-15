package QueryParsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Class to represent a particular query after tokenisation
public class Query {
    private String type;    // Represents type of query like select, update, insert, delete
    private String tableName;   // Represents the corresponding table
    private String databaseName;    // Represent the database
    private List<String> fields;  // The field names to perform the insert operation on
    private Map<String, String> valuesToUpdate; // Field names with corresponding update values
    private List<List<String>>  conditions; //All the conditions in where. Each entry is of type {Operand1, Operator, Operand2}
    private List<List<String>> inserts;   // Values to be inserted into fields
    private List<String> datatypes;   // Datatypes for create query
    private List<String> constraints;   // Constraints on columns for CREATE query

    // Default constructor
    public Query(){
        this.type = "";
        this.tableName = "";
        this.databaseName = "";
        this.fields = new ArrayList<>();
        this.valuesToUpdate = new HashMap<>();
        this.conditions = new ArrayList<>();
        this.inserts = new ArrayList<>();
        this.datatypes = new ArrayList<>();
        this.constraints = new ArrayList<>();
    }

    // Constructor with given parameters
    public Query(String type, String tableName, List<String> fields) {
        this.type = type;
        this.tableName = tableName;
        this.databaseName = "";
        this.fields = fields;
        this.valuesToUpdate = new HashMap<>();
        this.conditions = new ArrayList<>();
        this.inserts = new ArrayList<>();
    }

    // Constructor with all fields
    public Query(String type, String tableName, String databaseName, List<String> fields, Map<String, String> valuesToUpdate, List<List<String>> conditions, List<List<String>> inserts, List<String> datatypes) {
        this.type = type;
        this.tableName = tableName;
        this.databaseName = databaseName;
        this.fields = fields;
        this.valuesToUpdate = valuesToUpdate;
        this.conditions = conditions;
        this.inserts = inserts;
        this.datatypes = datatypes;
    }

    // Getters and setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public Map<String, String> getValuesToUpdate() {
        return valuesToUpdate;
    }

    public void setValuesToUpdate(Map<String, String> valuesToUpdate) {
        this.valuesToUpdate = valuesToUpdate;
    }

    public List<List<String>> getConditions() {
        return conditions;
    }

    public void setConditions(List<List<String>> conditions) {
        this.conditions = conditions;
    }

    public List<List<String>> getInserts() {
        return inserts;
    }

    public void setInserts(List<List<String>> inserts) {
        this.inserts = inserts;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public List<String> getDatatypes() {
        return datatypes;
    }

    public void setDatatypes(List<String> datatypes) {
        this.datatypes = datatypes;
    }

    public List<String> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<String> constraints) {
        this.constraints = constraints;
    }

    @Override
    public String toString() {
        return "Query{" +
                "type='" + type + '\'' +
                ", tableName='" + tableName + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", fields=" + fields +
                ", valuesToUpdate=" + valuesToUpdate +
                ", conditions=" + conditions +
                ", inserts=" + inserts +
                ", datatypes=" + datatypes +
                ", constraints=" + constraints +
                '}';
    }
}
