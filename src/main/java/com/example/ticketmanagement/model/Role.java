package com.example.ticketmanagement.model;

public enum Role {
    Employee, ITSupport;

    public String getAuthority() {
        return "ROLE_" + this.name(); // Returns "ROLE_EMPLOYEE" or "ROLE_IT_SUPPORT"
    }
}
