package com.dpg7.main;

import QueryExecution.WrongQueryException;
import QueryParsing.InvalidQueryException;
import QueryParsing.Query;

import java.util.*;
import java.util.stream.Stream;

// Filtering rows and columns
public class TableOperations {
    public static List<LinkedHashMap<String, String>> filterByColumns(List<LinkedHashMap<String, String>> rows,
            List<String> requiredColumns) {
        if (requiredColumns.get(0).equals("*")) {
            return rows;
        }
        if (requiredColumns.size() == 0) {
            return rows;
        }
        List<LinkedHashMap<String, String>> output = new ArrayList<>();
        for (LinkedHashMap<String, String> row : rows) {
            LinkedHashMap<String, String> currentRow = new LinkedHashMap<>();
            for (String col : requiredColumns) {
                currentRow.put(col, row.get(col));
            }
            output.add(currentRow);
        }
        return output;
    }

    public static List<Integer> filterByWhere(List<LinkedHashMap<String, String>> rows,
            LinkedHashMap<String, HashMap<String, String>> tableMeta, List<List<String>> conditions) {
        List<List<Integer>> satisfyingList = new ArrayList<>();

        for (List<String> condition : conditions) {
            int i = 0;
            List<Integer> conditionSatisfying = new ArrayList<>();
            for (LinkedHashMap<String, String> row : rows) {
                if (evaluateWhereExpression(condition, tableMeta.get(condition.get(0)).get("dataType"),
                        row.get(condition.get(0)))) {
                    conditionSatisfying.add(i);
                }
                i++;
            }
            satisfyingList.add(conditionSatisfying);
        }
        List<Integer> col1 = satisfyingList.get(0);
        for (List<Integer> row : satisfyingList) {
            col1.retainAll(row);
        }

        return col1;
    }

    private static boolean evaluateWhereExpression(List<String> condition, String datatype, String actualValue) {
        String comparisonValue = condition.get(2);
        String operator = condition.get(1);

        // For text strings
        if (datatype.equals("TEXT")) {
            switch (operator) {
                case "=":
                    return comparisonValue.equals(actualValue);
                case "!=":
                    return !comparisonValue.equals(actualValue);
                default:
                    throw new WrongQueryException(operator + " operator not valid on TEXT datatype");
            }
        }

        // For numeric values
        float comparisonDouble = Float.parseFloat(comparisonValue);
        float actualDouble = Float.parseFloat(actualValue);

        switch (operator) {
            case "=":
                return comparisonDouble == actualDouble;
            case ">":
                return actualDouble > comparisonDouble;
            case "<":
                return actualDouble < comparisonDouble;
            case ">=":
                return actualDouble >= comparisonDouble;
            case "<=":
                return actualDouble <= comparisonDouble;
            case "!=":
                return actualDouble != comparisonDouble;
            default:
                throw new WrongQueryException(operator + " operator not valid on FLOAT datatype");
        }
    }

    public static LinkedHashMap<String, String> updateByWhere(LinkedHashMap<String, String> row,
            Map<String, String> fieldsToUpdate) {
        if (fieldsToUpdate.size() == 0) {
            return row;
        }

        LinkedHashMap<String, String> updatedRow = new LinkedHashMap<>(row);
        for (String colToUpdate : fieldsToUpdate.keySet()) {
            updatedRow.put(colToUpdate, fieldsToUpdate.get(colToUpdate));
        }
        return updatedRow;
    }
}
