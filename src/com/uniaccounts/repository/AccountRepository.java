package com.uniaccounts.repository;

import com.uniaccounts.audit.AuditLogger;
import com.uniaccounts.db.Database;
import com.uniaccounts.model.StudentAccount;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * PATTERN: Singleton -> ROLE: Singleton (one central repository for all accounts).
 * Accounts are stored in the table "students" of the SQLite database, so they
 * survive after the program stops. Every creation is written to the audit log.
 */
public final class AccountRepository {

    private AccountRepository() { }

    private static class Holder {
        private static final AccountRepository INSTANCE = new AccountRepository();
    }

    public static AccountRepository getInstance() {
        return Holder.INSTANCE;
    }

    /** Inserts an account. The id is the primary key, so it must be unique. */
    public synchronized void add(StudentAccount a) {
        if (a == null)
            throw new IllegalArgumentException("account cannot be null");
        if (findById(a.getId()).isPresent())
            throw new IllegalStateException("An account with id " + a.getId() + " already exists");

        String sql = "INSERT INTO students(id, full_name, email, phone, level, major, scholarship, courses) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, a.getId());
            ps.setString(2, a.getFullName());
            ps.setString(3, a.getEmail());
            ps.setString(4, a.getPhone());
            ps.setString(5, a.getLevel());
            ps.setString(6, a.getMajor());
            ps.setInt(7, a.isScholarship() ? 1 : 0);
            ps.setString(8, String.join("|", a.getCourses()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot save account: " + e.getMessage(), e);
        }
        AuditLogger.getInstance().log("CREATE", "account " + a.getId() + " (" + a.getFullName() + ")");
    }

    public synchronized Optional<StudentAccount> findById(String id) {
        String sql = "SELECT * FROM students WHERE id = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(fromRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot read account: " + e.getMessage(), e);
        }
    }

    public synchronized List<StudentAccount> findAll() {
        List<StudentAccount> list = new ArrayList<>();
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM students ORDER BY rowid")) {
            while (rs.next()) list.add(fromRow(rs));
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot read accounts: " + e.getMessage(), e);
        }
        return list;
    }

    public synchronized int count() {
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM students")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot count accounts: " + e.getMessage(), e);
        }
    }

    /** Used only by the demo to start from a clean state. */
    public synchronized void deleteAll() {
        try (Statement st = Database.getConnection().createStatement()) {
            st.executeUpdate("DELETE FROM students");
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot clear accounts: " + e.getMessage(), e);
        }
    }

    /** Rebuilds a StudentAccount from a database row (using the Builder). */
    private StudentAccount fromRow(ResultSet rs) throws SQLException {
        String coursesText = rs.getString("courses");
        List<String> courses = (coursesText == null || coursesText.isEmpty())
                ? new ArrayList<>() : Arrays.asList(coursesText.split("\\|"));
        return new StudentAccount.Builder(rs.getString("id"), rs.getString("full_name"), rs.getString("email"))
                .phone(rs.getString("phone"))
                .level(rs.getString("level"))
                .major(rs.getString("major"))
                .scholarship(rs.getInt("scholarship") == 1)
                .courses(courses)
                .build();
    }
}
