package com.busgo;

/**
 * Passenger entity representing a traveler in the BusGo system.
 * Demonstrates OOP Encapsulation with private fields and validated accessors.
 */
public class Passenger {
    private String name;
    private int age;
    private String gender;
    private String mobile;
    private String email;

    public Passenger(String name, int age, String gender, String mobile, String email) {
        this.name = sanitize(name);
        this.age = (age >= 1 && age <= 120) ? age : 25;
        this.gender = (gender != null && !gender.trim().isEmpty()) ? gender : "Male";
        this.mobile = (mobile != null && mobile.matches("^[0-9]{10}$")) ? mobile : "0000000000";
        this.email = (email != null && email.contains("@")) ? email.trim() : (this.mobile + "@busgo.com");
    }

    public Passenger(String name, int age, String gender, String mobile) {
        this(name, age, gender, mobile, null);
    }

    private String sanitize(String input) {
        if (input == null) return "";
        return input.replace("|", " ").trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = sanitize(name);
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        if (age >= 1 && age <= 120) {
            this.age = age;
        }
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        if (mobile != null && mobile.matches("^[0-9]{10}$")) {
            this.mobile = mobile;
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email != null && email.contains("@")) {
            this.email = email.trim();
        }
    }

    /**
     * Serializes passenger data for file storage (parity with tickets.txt format)
     */
    public String toFileString() {
        return name + "|" + age + "|" + gender + "|" + mobile + "|" + email;
    }

    @Override
    public String toString() {
        return name + " (" + gender + ", " + age + " yrs) - Ph: " + mobile + " - Email: " + email;
    }
}
