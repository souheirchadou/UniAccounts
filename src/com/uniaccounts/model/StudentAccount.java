package com.uniaccounts.model;

import com.uniaccounts.prototype.Prototype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Student account.
 *
 * PATTERN: Builder   -> ROLE: Product (built by the nested class Builder).
 * PATTERN: Prototype -> ROLE: Concrete Prototype (implements copy()).
 *
 * Constructors are private: an account comes only from Builder.build() or copy().
 */
public class StudentAccount implements Prototype<StudentAccount> {

    public static final Set<String> LEVELS = Set.of("L1", "L2", "L3", "M1", "M2");

    private String id;
    private String fullName;
    private String email;
    private String phone;                 // optional, default null
    private String level;                 // optional, default "L1"
    private String major;                 // optional
    private boolean scholarship;          // optional, default false
    private final List<String> courses;   // optional, default empty

    private StudentAccount(Builder b) {
        this.id = b.id;
        this.fullName = b.fullName;
        this.email = b.email;
        this.phone = b.phone;
        this.level = b.level;
        this.major = b.major;
        this.scholarship = b.scholarship;
        this.courses = new ArrayList<>(b.courses);
    }

    /** Copy constructor used by copy(): DEEP copy of the courses list. */
    private StudentAccount(StudentAccount o) {
        this.id = o.id;
        this.fullName = o.fullName;
        this.email = o.email;
        this.phone = o.phone;
        this.level = o.level;
        this.major = o.major;
        this.scholarship = o.scholarship;
        this.courses = new ArrayList<>(o.courses); // new list, not the same reference
    }

    /** PATTERN: Prototype - clone this account (used to copy a batch template). */
    @Override
    public StudentAccount copy() {
        return new StudentAccount(this);
    }

    /** Changes only the identity of a copy; the rest comes from the template. */
    public StudentAccount withIdentity(String id, String fullName, String email) {
        requireText(id, "id");
        requireText(fullName, "fullName");
        requireEmail(email);
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = null; // personal data is never inherited from a template
        return this;
    }

    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getLevel() { return level; }
    public String getMajor() { return major; }
    public boolean isScholarship() { return scholarship; }
    public List<String> getCourses() { return Collections.unmodifiableList(courses); }

    @Override
    public String toString() {
        return id + " | " + fullName + " | " + email + " | phone=" + phone + " | " + level
                + " | " + major + " | scholarship=" + scholarship + " | " + courses;
    }

    private static void requireText(String v, String field) {
        if (v == null || v.trim().isEmpty())
            throw new IllegalArgumentException(field + " is required and cannot be empty");
    }

    private static void requireEmail(String email) {
        requireText(email, "email");
        if (!email.contains("@"))
            throw new IllegalArgumentException("email must contain '@': " + email);
    }

    // =====================================================================
    // PATTERN: Builder -> ROLE: ConcreteBuilder
    // Mandatory fields go in the Builder constructor; optional fields use
    // fluent methods with default values; build() validates everything.
    // =====================================================================
    public static class Builder {
        private final String id;
        private final String fullName;
        private final String email;
        private String phone = null;
        private String level = "L1";
        private String major = null;
        private boolean scholarship = false;
        private final List<String> courses = new ArrayList<>();

        public Builder(String id, String fullName, String email) {
            this.id = id;
            this.fullName = fullName;
            this.email = email;
        }

        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder level(String level) { this.level = level; return this; }
        public Builder major(String major) { this.major = major; return this; }
        public Builder scholarship(boolean scholarship) { this.scholarship = scholarship; return this; }
        public Builder addCourse(String course) { this.courses.add(course); return this; }
        public Builder courses(List<String> list) { this.courses.addAll(list); return this; }

        public StudentAccount build() {
            requireText(id, "id");
            requireText(fullName, "fullName");
            requireEmail(email);
            if (!LEVELS.contains(level))
                throw new IllegalArgumentException("level must be one of " + LEVELS + " but was: " + level);
            return new StudentAccount(this);
        }
    }
}
