package QueryParsing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// A class to parse a given query
public class Parser {
    private String sql; // The query to be parsed
    private String orgSql;  // Original query input by user
    private int currPos;    // The current position in query
    private Query query;    // The query after parsing
    private ParserConstants.ParsingStep step;   // Current step, as defined in enum
    private String nextUpdateField;

    public Parser(String sql) {
        this.orgSql = sql;
        this.sql = sql.replaceAll(";*$","");
        this.currPos = 0;
        this.query = new Query();
        this.step = ParserConstants.ParsingStep.stepType;
    }

    public Query parse() throws InvalidQueryException{

        while(this.currPos < this.sql.length()){
            String nextToken;

            switch(this.step) {

                case stepType:
                    switch(this.peek().toLowerCase()){
                        case "select":
                            this.query.setType("SELECT");
                            this.step = ParserConstants.ParsingStep.stepSelectField;
                            this.pop();
                            break;
                        case "update":
                            this.query.setType("UPDATE");
                            this.step = ParserConstants.ParsingStep.stepUpdateTable;
                            this.pop();
                            break;
                        case "insert into":
                            this.query.setType("INSERT");
                            this.step = ParserConstants.ParsingStep.stepInsertTable;
                            this.pop();
                            break;
                        case "delete from":
                            this.query.setType("DELETE");
                            this.step = ParserConstants.ParsingStep.stepDeleteFromTable;
                            this.pop();
                            break;
                        case "use":
                            this.query.setType("USE");
                            this.step = ParserConstants.ParsingStep.stepInputDatabase;
                            this.pop();
                            break;
                        case "create":
                            this.query.setType("CREATE");
                            this.step = ParserConstants.ParsingStep.stepCreateDatabaseOrTable;
                            this.pop();
                            break;
                        case "begin transaction":
                            this.query.setType("BEGIN TRANSACTION");
                            this.step = ParserConstants.ParsingStep.stepEndDBQuery;
                            this.pop();
                            break;
                        case "end transaction":
                            this.query.setType("END TRANSACTION");
                            this.step = ParserConstants.ParsingStep.stepEndDBQuery;
                            this.pop();
                            break;
                        case "commit":
                            this.query.setType("COMMIT");
                            this.step = ParserConstants.ParsingStep.stepEndDBQuery;
                            this.pop();
                            break;
                        case "rollback":
                            this.query.setType("ROLLBACK");
                            this.step = ParserConstants.ParsingStep.stepEndDBQuery;
                            this.pop();
                            break;
                        default:
                            throw new InvalidQueryException(orgSql + ":is not a valid query");

                    }
                    break;

                case stepInputDatabase:
                    nextToken = this.peek();
                    if(nextToken.length()==0){
                        throw new InvalidQueryException(orgSql + ": expected a valid database name");
                    }
                    // Set database name in query
                    this.query.setDatabaseName(nextToken);
                    this.step = ParserConstants.ParsingStep.stepEndDBQuery;
                    this.pop();
                    break;

                case stepCreateTableName:
                    nextToken = this.peek();
                    if(nextToken.length()==0){
                        throw new InvalidQueryException(orgSql + ": expected a valid table name");
                    }
                    if(!isAnIdentifier(nextToken)){
                        throw new InvalidQueryException(orgSql + ": expected a valid table name");
                    }
                    // Set table name in query
                    this.query.setTableName(nextToken);
                    this.step = ParserConstants.ParsingStep.stepCreateFieldOpeningParenth;
                    this.pop();
                    break;

                case stepCreateFieldOpeningParenth:
                    nextToken = this.peek();
                    if(nextToken.equals("(")){
                        this.pop();
                        this.step = ParserConstants.ParsingStep.stepCreateFields;
                    }else{
                        throw new InvalidQueryException(orgSql + ": expected opening parenthesis (");
                    }
                    break;

                case stepCreateFields:
                    nextToken = this.peek();
                    if(!isAnIdentifier(nextToken)) {
                        throw new InvalidQueryException(orgSql + ": expected a valid column name");
                    }
                    this.query.getFields().add(nextToken);
                    this.pop();
                    this.step = ParserConstants.ParsingStep.stepCreateDataType;
                    break;

                case stepCreateDataType:
                    nextToken = this.peek();
                    if(!isDataType(nextToken)) {
                        throw new InvalidQueryException(orgSql + ": expected a valid data type");
                    }
                    this.query.getDatatypes().add(nextToken.toUpperCase());
                    this.pop();
                    this.step = ParserConstants.ParsingStep.stepCreateConstraint;
                    break;

                case stepCreateConstraint:
                    nextToken = this.peek();
                    if(nextToken.equals(",")||nextToken.equals(")")){
                        this.query.getConstraints().add("");
                    }else if(isConstraint(nextToken)){
                        this.query.getConstraints().add(nextToken);
                        this.pop();
                    }else if(isAnIdentifier(nextToken)){
                        // Foreign key constraint
                        String tableName = String.valueOf(nextToken);
                        this.pop();
                        nextToken = this.peek();
                        // Foreign key with unique keyword
                        if(nextToken.length()==0){
                            throw new InvalidQueryException(orgSql + ": expected a coma, constraint or closing parenthesis");
                        }else if(nextToken.equalsIgnoreCase("UNIQUE")){
                            this.query.getConstraints().add(tableName+"\tU");
                            this.pop();
                        }else{
                            this.query.getConstraints().add(tableName);
                        }
                    }else {
                        throw new InvalidQueryException(orgSql + ": expected a coma, constraint or closing parenthesis");
                    }
                    this.step = ParserConstants.ParsingStep.stepCreateCommaOrClosingParenth;
                    break;

                case stepCreateCommaOrClosingParenth:
                    nextToken = this.peek();
                    this.pop();
                    if(nextToken.equals(",")) {
                        this.step = ParserConstants.ParsingStep.stepCreateFields;
                    }else if(nextToken.equals(")")){
                        this.step = ParserConstants.ParsingStep.stepEndDBQuery;
                    } else {
                        throw new InvalidQueryException(orgSql + ": a comma, a constraint or a closing parenthesis is expected");
                    }
                    break;

                case stepEndDBQuery:
                    // No new token expected
                    nextToken = this.peek();
                    if(nextToken.length()!=0||this.currPos<this.sql.length()){
                        throw new InvalidQueryException(orgSql + ": invalid expression at end of query");
                    }
                    break;

                case stepSelectField:
                    nextToken = this.peek();
                    // Expecting a * or a fieldname
                    if(nextToken.equalsIgnoreCase("*")||this.isAnIdentifier(nextToken)){
                        // It is a field name
                        this.query.getFields().add(nextToken);
                        this.pop();
                        nextToken = this.peek();

                        if(nextToken.toLowerCase().equals("")){
                            throw new InvalidQueryException(orgSql + ": expected FROM keyword in query");
                        }else if(nextToken.toLowerCase().equals("from")){
                            this.step = ParserConstants.ParsingStep.stepSelectFrom;
                        }else{
                            this.step = ParserConstants.ParsingStep.stepSelectComma;
                        }
                    }else{
                        throw new InvalidQueryException(orgSql + ":is not a valid SELECT query");
                    }
                    break;

                case stepSelectComma:
                    nextToken = this.peek();
                    if(!nextToken.equals(",")){
                        throw new InvalidQueryException(orgSql + ": expected a comma or FROM");
                    }
                    this.pop();
                    this.step = ParserConstants.ParsingStep.stepSelectField;
                    break;

                case stepSelectFrom:
                    nextToken = this.peek();
                    if(!nextToken.equalsIgnoreCase("FROM")){
                        throw new InvalidQueryException(orgSql + ": FROM is expected");
                    }
                    this.pop();
                    this.step = ParserConstants.ParsingStep.stepSelectTable;
                    break;

                case stepSelectTable:
                    nextToken = this.peek();
                    if(nextToken.length()==0){
                        throw new InvalidQueryException(orgSql + ": expected a valid table name");
                    }
                    if(!isAnIdentifier(nextToken)){
                        throw new InvalidQueryException(orgSql + ": invalid table name");
                    }
                    // Set table name in query
                    this.query.setTableName(nextToken);
                    this.pop();
                    this.step = ParserConstants.ParsingStep.stepWhere;
                    break;

                case stepInsertTable:
                    nextToken = this.peek();
                    if(nextToken.length()==0){
                        throw new InvalidQueryException(orgSql + ": expected a valid table name");
                    }
                    if(!isAnIdentifier(nextToken)){
                        throw new InvalidQueryException(orgSql + ": expected a valid table name");
                    }
                    // Set table name in query
                    this.query.setTableName(nextToken);
                    this.pop();
                    this.step = ParserConstants.ParsingStep.stepInsertFieldOpeningParenth;
                    break;

                case stepDeleteFromTable:
                    nextToken = this.peek();
                    if (nextToken.length() == 0) {
                        throw new InvalidQueryException(orgSql + ": expected a valid table name");
                    }
                    if(!isAnIdentifier(nextToken)){
                        throw new InvalidQueryException(orgSql + ": expected a valid table name");
                    }
                    // Set table name in query
                    this.query.setTableName(nextToken);
                    this.pop();
                    this.step = ParserConstants.ParsingStep.stepWhere;
                    break;

                case stepUpdateTable:
                    nextToken = this.peek();
                    if(nextToken.length()==0) {
                        throw new InvalidQueryException(orgSql + ": expected a valid table name");
                    }
                    if(!isAnIdentifier(nextToken)){
                        throw new InvalidQueryException(orgSql + ": expected a valid table name");
                    }
                    // Set table name in query
                    this.query.setTableName(nextToken);
                    this.pop();
                    this.step = ParserConstants.ParsingStep.stepUpdateSet;
                    break;

                case stepUpdateSet:
                    nextToken = this.peek();
                    if (!nextToken.equalsIgnoreCase("SET")) {
                        throw new InvalidQueryException(orgSql + ": expected SET in query");
                    }

                    this.pop();
                    this.step = ParserConstants.ParsingStep.stepUpdateField;
                    break;

                case stepUpdateField:
                    nextToken = this.peek();
                    if (!isAnIdentifier(nextToken)){
                        throw new InvalidQueryException(orgSql + ": expected a valid field to update in query");
                    }
                    this.nextUpdateField = nextToken;
                    this.pop();
                    this.step = ParserConstants.ParsingStep.stepUpdateEquals;
                    break;

                case stepUpdateEquals:
                    nextToken = this.peek();
                    if (!nextToken.equals("=")) {
                        throw new InvalidQueryException(orgSql + ": expected = symbol in query");
                    }
                    this.pop();
                    this.step = ParserConstants.ParsingStep.stepUpdateValue;
                    break;

                case stepUpdateValue:
                    nextToken = this.peek();
                    if(nextToken.equals("")){
                        throw new InvalidQueryException(orgSql + ": expected the value to update in quotes");
                    }
                    this.query.getValuesToUpdate().put(this.nextUpdateField, nextToken);
                    this.nextUpdateField = "";
                    this.pop();

                    // Check if a WHERE condition is present
                    nextToken = this.peek();
                    if(nextToken.equalsIgnoreCase("WHERE")){
                        this.step = ParserConstants.ParsingStep.stepWhere;
                    }else{
                        this.step = ParserConstants.ParsingStep.stepUpdateComma;
                    }
                    break;

                case stepUpdateComma:
                    nextToken = this.peek();
                    if(!nextToken.equals(",")){
                        throw new InvalidQueryException(orgSql + ": expected a ,");
                    }
                    this.pop();
                    this.step = ParserConstants.ParsingStep.stepUpdateField;
                    break;

                case stepWhere:
                    nextToken = this.peek();
                    if(!nextToken.equalsIgnoreCase("WHERE")){
                        throw new InvalidQueryException(orgSql + ": expected a WHERE clause");
                    }
                    this.pop();
                    this.step = ParserConstants.ParsingStep.stepWhereField;
                    this.query.getConditions().add(new ArrayList<>());
                    break;

                case stepWhereField:
                    nextToken = this.peek();
                    if(!isAnIdentifier(nextToken)) {
                        // Expecting an identifier in nextToken
                        throw new InvalidQueryException(orgSql + ": expected a valid field in WHERE clause");
                    }

                    // Add to conditions in query
                    if(this.query.getConditions().get(this.query.getConditions().size()-1).size()==3){
                        // Check the size of last inserted condition
                        this.query.getConditions().add(new ArrayList<String>());
                    }
                    this.query.getConditions().get(this.query.getConditions().size()-1).add(nextToken);

                    this.pop();
                    this.step = ParserConstants.ParsingStep.stepWhereOperator;
                    break;

                case stepWhereOperator:
                    nextToken = this.peek();

                    if(!isOperatorValid(nextToken)){
                        throw new InvalidQueryException(orgSql + ": the operator is unknown");
                    }

                    this.query.getConditions().get(this.query.getConditions().size() - 1).add(nextToken);
                    this.pop();
                    this.step = ParserConstants.ParsingStep.stepWhereValue;
                    break;

                case stepWhereValue:
                    nextToken = this.peek();
                    if(nextToken.length()==0){
                        // Value not valid
                        throw new InvalidQueryException(orgSql + ": expected a valid value in WHERE clause");
                    }
                    else {
                        this.query.getConditions().get(this.query.getConditions().size() - 1).add(nextToken);
                    }
                    this.pop();
                    this.step = ParserConstants.ParsingStep.stepWhereAnd;
                    break;

                case stepWhereAnd:
                    nextToken = this.peek();
                    if(!nextToken.equalsIgnoreCase("AND")) {
                        throw new InvalidQueryException(orgSql + ": expected AND in query");
                    }
                    this.pop();
                    this.step = ParserConstants.ParsingStep.stepWhereField;
                    break;

                case stepInsertFieldOpeningParenth:
                    nextToken = this.peek();
                    if(nextToken.equals("(")){
                        this.pop();
                        this.step = ParserConstants.ParsingStep.stepInsertFields;
                    }else{
                        throw new InvalidQueryException(orgSql + ": expected opening parenthesis (");
                    }
                    break;

                case stepInsertFields:
                    nextToken = this.peek();
                    if(!isAnIdentifier(nextToken)) {
                        throw new InvalidQueryException(orgSql + ": expected a valid column name");
                    }
                    this.query.getFields().add(nextToken);
                    this.pop();
                    this.step = ParserConstants.ParsingStep.stepInsertCommaOrClosingParenth;
                    break;

                case stepInsertCommaOrClosingParenth:
                    nextToken = this.peek();
                    this.pop();
                    if(nextToken.equals(",")) {
                        this.step = ParserConstants.ParsingStep.stepInsertFields;
                    }else if(nextToken.equals(")")){
                        this.step = ParserConstants.ParsingStep.stepInsertValuesReservedWord;
                    } else{
                        throw new InvalidQueryException(orgSql + ": a comma or a closing parenthesis is expected");
                    }
                    break;

                case stepInsertValuesReservedWord:
                    nextToken = this.peek();
                    if(!nextToken.equalsIgnoreCase("VALUES")){
                        throw new InvalidQueryException(orgSql + ": 'VALUES' is expected in query");
                    }
                    this.pop();
                    this.step = ParserConstants.ParsingStep.stepInsertValueOpeningParenth;
                    break;

                case stepInsertValueOpeningParenth:
                    nextToken = this.peek();
                    if(!nextToken.equals("(")){
                        throw new InvalidQueryException(orgSql + ": opening parenthesis is expected");
                    }
                    List<String> currentInsert = new ArrayList<>();
                    this.query.getInserts().add(currentInsert);

                    // p.query.Inserts = append(p.query.Inserts, []string{})
                    this.pop();
                    this.step = ParserConstants.ParsingStep.stepInsertValue;
                    break;

                case stepInsertValue:
                    nextToken = this.peek();
                    if (nextToken.length()==0) {
                        throw new InvalidQueryException(orgSql + ": a quoted value is expected");
                    }
                    if(nextToken.equals(")")){
                        this.step = ParserConstants.ParsingStep.stepInsertValuesCommaOrClosingParenth;
                        break;
                    }
                    List<String> currInsert;
                    if(this.query.getInserts().size()==0){
                        currInsert = new ArrayList<>();
                        currInsert.add(nextToken);
                        this.query.getInserts().add(currInsert);
                    }else{
                        currInsert = this.query.getInserts().get(this.query.getInserts().size() - 1);
                        currInsert.add(nextToken);
                        //this.query.getInserts().add(this.query.getInserts().size() - 1, currInsert);
                    }

                    // p.query.Inserts[len(p.query.Inserts)-1] = append(p.query.Inserts[len(p.query.Inserts)-1], quotedValue)
                    this.pop();
                    this.step = ParserConstants.ParsingStep.stepInsertValuesCommaOrClosingParenth;
                    break;

                case stepInsertValuesCommaOrClosingParenth:
                    nextToken = this.peek();
                    this.pop();

                    if(nextToken.equals(",")){
                        this.step = ParserConstants.ParsingStep.stepInsertValue;
                    }else if(nextToken.equals(")")){
                        int size_inserts = this.query.getInserts().size();
                        size_inserts = size_inserts!=0?this.query.getInserts().get(size_inserts - 1).size():0;
                        if(size_inserts<this.query.getFields().size()){
                            throw new InvalidQueryException(orgSql + ": field count doesn't match with value count");
                        }
                        this.step= ParserConstants.ParsingStep.stepInsertValuesCommaBeforeOpeningParenth;
                    }else{
                        throw new InvalidQueryException(orgSql + ": a comma or closing parenthesis is expected");
                    }
                    break;

                case stepInsertValuesCommaBeforeOpeningParenth:
                    nextToken = this.peek();
                    if(!nextToken.equals(",")){
                        throw new InvalidQueryException(orgSql + ": a comma , is expected");
                    }
                    this.pop();
                    this.step = ParserConstants.ParsingStep.stepInsertValueOpeningParenth;
                    break;

                case stepCreateDatabaseOrTable:
                    nextToken = this.peek();
                    this.pop();
                    if(nextToken.equalsIgnoreCase("DATABASE")){
                        this.step = ParserConstants.ParsingStep.stepInputDatabase;
                    }else if(nextToken.equalsIgnoreCase("TABLE")){
                        this.step = ParserConstants.ParsingStep.stepCreateTableName;
                    }else{
                        throw new InvalidQueryException(orgSql + ": expected keyword DATABASE or TABLE");
                    }
                    break;

                default:
                    break;
            }
        }

        // Additional validations
        this.validateQuery();

        // Return the parsed query
        return this.query;
    }

    // Returns the next token to parse
    public String peek(){
        if(this.currPos >= this.sql.length()){
            // End of query
            return "";
        }
        // Check if present in reserved words
        for(String reserved: ParserConstants.RESERVED_PHRASES){
            String possibleToken = sql.substring(this.currPos, Math.min(this.sql.length(), this.currPos + reserved.length()));
            if(possibleToken.equalsIgnoreCase(reserved)){
                return possibleToken;
            }
        }
        // Check if present in CONSTRAINTS
        for(String reserved: ParserConstants.VALID_CONSTRAINTS){
            String possibleToken = sql.substring(this.currPos, Math.min(this.sql.length(), this.currPos + reserved.length()));
            if(possibleToken.equalsIgnoreCase(reserved)){
                return possibleToken;
            }
        }
        // Check if present in DATA TYPES
        for(String reserved: ParserConstants.DATA_TYPES){
            String possibleToken = sql.substring(this.currPos, Math.min(this.sql.length(), this.currPos + reserved.length()));
            if(possibleToken.equalsIgnoreCase(reserved)){
                return possibleToken;
            }
        }
        // Check if quoted string
        if(this.sql.charAt(this.currPos)=='\''){
            int endPos=this.currPos+1;
            while(endPos<this.sql.length() && this.sql.charAt(endPos)!='\''){
                endPos++;
            }
            // No closing quote was found
            if(endPos==this.sql.length()){
                return "";
            }
            // Closing quote found
            return this.sql.substring(this.currPos, endPos+1);
        }

        // Parse identifier
        Pattern pattern = Pattern.compile("[*_A-Za-z0-9]");
        int endPos;
        for(endPos=this.currPos;endPos<this.sql.length();endPos++){
            if(!pattern.matcher(Character.toString(this.sql.charAt(endPos))).find()){
                break;
            }
        }
        return this.sql.substring(this.currPos, endPos);
    }

    // Returns the next token to parse and advances the index
    public String pop(){
        String peekedToken = this.peek();
        this.currPos += peekedToken.length();
        //Remove whitespace
        this.popWhiteSpace();

        // Check for semi colon
        if(this.currPos==(this.sql.length()-1) && this.sql.charAt(this.currPos)==';'){
            // Semi-colon at end, advance ahead
            this.currPos++;
        }
        return peekedToken;
    }

    // Move ahead any whitespaces
    public void popWhiteSpace(){
        while(this.currPos<this.sql.length() && this.sql.charAt(this.currPos)==' '){
            this.currPos++;
        }
    }


    // Tells if the current token is a reserved word
    private boolean isReserved(String token){
        for(String phrase: ParserConstants.RESERVED_PHRASES){
            // Match found
            if(token.equalsIgnoreCase(phrase)){
                return true;
            }
        }
        return false;
    }

    // Tells if the current token is a valid datatype
    private boolean isDataType(String token){
        for(String phrase: ParserConstants.DATA_TYPES){
            // Match found
            if(token.equalsIgnoreCase(phrase)){
                return true;
            }
        }
        return false;
    }

    // Tells if the current token is a valid constraint
    private boolean isConstraint(String token){
        for(String phrase: ParserConstants.VALID_CONSTRAINTS){
            // Match found
            if(token.equalsIgnoreCase(phrase)){
                return true;
            }
        }
        return false;
    }

    // Tells if the current token is an identifier
    private boolean isAnIdentifier(String token){
        if(isReserved(token)){
            // Is a reserved word
            return false;
        }
        // Identifier can either start with an underscore or an alphabet and can be followed by alphanumeric characters
        return Pattern.compile("[a-zA-Z_][A-Za-z0-9_]*").matcher(token).find();
    }

    // Tells if the operator in a where condition is valid
    private boolean isOperatorValid(String operator){
        if(operator.length()==0){
            return false;
        }
        for(String op: ParserConstants.VALID_OPERATORS){
            if(op.equals(operator)){
                return true;
            }
        }
        return false;
    }

    // Validate the final generated query object
    private void validateQuery(){
        // Exempt transactional statements
        if(this.query.getType()=="BEGIN TRANSACTION"||this.query.getType()=="END TRANSACTION"||this.query.getType()=="COMMIT"||this.query.getType()=="ROLLBACK"){
            return;
        }

        if(this.sql==null||this.sql.length()==0) {
            // The parser is expecting a field
            throw new InvalidQueryException(orgSql + ": query cannot be empty or null");
        }

        if(this.query.getConditions().size()==0 && this.step == ParserConstants.ParsingStep.stepWhereField) {
            // The parser is expecting a field
            throw new InvalidQueryException(orgSql + ": expected a valid field in WHERE clause");
        }

        if(this.query.getType()=="SELECT" && this.query.getFields().size()==0){
            // No field names after select query
            throw new InvalidQueryException(orgSql + ": expected a field name in query");
        }


        if(this.query.getDatabaseName().length()==0 && this.query.getTableName().length()==0){
            // No table name
            throw new InvalidQueryException(orgSql + ": incomplete query");
        }

        for(List<String> condition: this.query.getConditions()){
            if(condition.size()!=3){
                throw new InvalidQueryException(orgSql + ": invalid condition in WHERE clause");
            }
        }

        if(this.query.getType().equals("INSERT") && this.query.getInserts().size()==0){
            // No data provided for insertion
            throw new InvalidQueryException(orgSql + ": no rows to insert");
        }

        if(this.query.getType().equals("INSERT")){
            for(List<String> row: this.query.getInserts()){
                if(this.query.getFields().size()!=row.size()){
                    throw new InvalidQueryException(orgSql + ": field count doesn't match with value count");
                }
            }
        }

        if(this.query.getType().equals("INSERT") && this.step== ParserConstants.ParsingStep.stepInsertValueOpeningParenth){
            throw new InvalidQueryException(orgSql + ": expected an opening parenthesis");
        }

        // Additional UPDATE validations
        if(this.query.getType().equals("UPDATE") && this.step== ParserConstants.ParsingStep.stepUpdateSet && this.peek().length()==0){
            throw new InvalidQueryException(orgSql + ": expected a SET keyword");
        }
        if(this.query.getType().equals("UPDATE") && this.step== ParserConstants.ParsingStep.stepUpdateField && this.peek().length()==0){
            throw new InvalidQueryException(orgSql + ": expected fields to update");
        }
        if(this.query.getType().equals("UPDATE") && this.step== ParserConstants.ParsingStep.stepUpdateEquals && this.peek().length()==0){
            throw new InvalidQueryException(orgSql + ": expected an equal sign");
        }
        if(this.query.getType().equals("UPDATE") && this.step== ParserConstants.ParsingStep.stepUpdateValue && this.peek().length()==0){
            throw new InvalidQueryException(orgSql + ": expected a valid value to update");
        }

        // Additional USE validations
        if(this.query.getType().equals("USE") && this.step== ParserConstants.ParsingStep.stepUpdateValue && this.peek().length()==0){
            throw new InvalidQueryException(orgSql + ": expected a valid value to update");
        }

        // Additional CREATE validations
        if(this.query.getType().equals("CREATE") && this.step== ParserConstants.ParsingStep.stepCreateFieldOpeningParenth && this.peek().length()==0){
            throw new InvalidQueryException(orgSql + ": expected an opening parenthesis");
        }
        if(this.query.getType().equals("CREATE") && this.step== ParserConstants.ParsingStep.stepCreateFields && this.peek().length()==0){
            throw new InvalidQueryException(orgSql + ": expected a valid column name");
        }
        if(this.query.getType().equals("CREATE") && this.step== ParserConstants.ParsingStep.stepCreateDataType && this.peek().length()==0){
            throw new InvalidQueryException(orgSql + ": expected a valid data type");
        }
        if(this.query.getType().equals("CREATE") && this.step== ParserConstants.ParsingStep.stepCreateConstraint && this.peek().length()==0){
            throw new InvalidQueryException(orgSql + ": a comma, a constraint or a closing parenthesis is expected");
        }
    }
}
