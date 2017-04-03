package com.example;

public class User {
    private String firstname;
    private String email;
    private Role role;

    public User(String firstname, String email) {
        this(firstname, email, Role.SALES_REP);
    }

    public User(String firstname, String email, Role role) {
        this.firstname = firstname;
        this.email = email;
        this.role = role;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getEmail() {
        return email;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
