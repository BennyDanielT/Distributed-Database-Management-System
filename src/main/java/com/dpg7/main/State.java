package com.dpg7.main;

import user.UserProfile;

public class State {

    private static State state = null;
    private Boolean isUserLoggedIn = false;
    private UserProfile loggedInUser = null;
    private String currentSchema = null;
    private String currentVM;

    // // class SchemaMeta {
    // // // e.g.
    // // // "DB1"
    // // String schemaName;
    // //
    // // // e.g.
    // // // [
    // // // { vmInstance: "VM1", tableName: "T1"}
    // // // { vmInstance: "VM1", tableName: "T2"}
    // // // { vmInstance: "VM2", tableName: "T1"}
    // // // ]
    // // ArrayList<HashMap<String, String>> instanceTableMapList;
    // // }
    // private SchemaMeta selectedSchema = null;

    private State() {
    }

    public static State getInstance() {
        if (state == null) {
            state = new State();
        }
        return state;
    }

    public Boolean getIsUserLoggedIn() {
        return isUserLoggedIn;
    }

    public void setIsUserLoggedIn(Boolean userLoggedIn) {
        isUserLoggedIn = userLoggedIn;
    }

    public UserProfile getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(UserProfile loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public String getCurrentSchema() {
        return currentSchema;
    }

    public void setCurrentSchema(String currentSchema) {
        this.currentSchema = currentSchema;
    }

    public void setCurrentVM(String vm) {
        currentVM = vm;
    }

    public String getCurrentVM() {
        return currentVM;
    }
}
