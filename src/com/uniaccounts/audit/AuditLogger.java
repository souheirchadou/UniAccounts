package com.uniaccounts.audit;

import com.uniaccounts.db.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * PATTERN: Singleton -> ROLE: Singleton (one audit log for the whole program).
 * Every entry is stored in the table audit_log of the database.
 * Implementation: initialization-on-demand holder (lazy, thread-safe).
 */
public final class AuditLogger {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AuditLogger() { }   // nobody else can call new

    private static class Holder {
        private static final AuditLogger INSTANCE = new AuditLogger();
    }

    public static AuditLogger getInstance() {
        return Holder.INSTANCE;
    }

    public synchronized void log(String action, String details) {
        String sql = "INSERT INTO audit_log(ts, action, details) VALUES(?, ?, ?)";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, LocalDateTime.now().format(FMT));
            ps.setString(2, action);
            ps.setString(3, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot write audit log: " + e.getMessage(), e);
        }
    }

    public synchronized int count() {
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM audit_log")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot read audit log: " + e.getMessage(), e);
        }
    }

    public synchronized void print() {
        System.out.println("===== AUDIT LOG (" + count() + " entries, read from the database) =====");
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT ts, action, details FROM audit_log ORDER BY id")) {
            while (rs.next()) {
                System.out.println("[" + rs.getString("ts") + "] " + rs.getString("action")
                        + " - " + rs.getString("details"));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot read audit log: " + e.getMessage(), e);
        }
    }

    /** Used only by the demo to start from a clean state. */
    public synchronized void clearHistory() {
        try (Statement st = Database.getConnection().createStatement()) {
            st.executeUpdate("DELETE FROM audit_log");
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot clear audit log: " + e.getMessage(), e);
        }
    }
}
