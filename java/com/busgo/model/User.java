package com.busgo.model;

/**
 * User account model representing passenger or admin in MySQL database.
 */
public class User {
    private int userId;
    private String fullName;
    private String mobile;
    private String email;
    private int age;
    private String gender;
    private String passwordHash;
    private String role; // 'PASSENGER' or 'ADMIN'

    public User(int userId, String fullName, String mobile, String email, int age, String gender, String passwordHash, String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.mobile = mobile;
        this.email = email;
        this.age = age;
        this.gender = gender;
        this.passwordHash = passwordHash;
        this.role = (role != null) ? role : "PASSENGER";
    }

    public User(String fullName, String mobile, String email, int age, String gender, String passwordHash, String role) {
        this(0, fullName, mobile, email, age, gender, passwordHash, role);
    }

    public int getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getMobile() { return mobile; }
    public String getEmail() { return email; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public boolean isAdmin() { return "ADMIN".equalsIgnoreCase(role); }
}
