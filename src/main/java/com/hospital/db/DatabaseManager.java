package com.hospital.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:hospital.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS patient_intake (
                    admission_id TEXT PRIMARY KEY,
                    patient_id TEXT NOT NULL,
                    insurance_policy TEXT NOT NULL,
                    status TEXT NOT NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bed_allocation (
                    admission_id TEXT PRIMARY KEY,
                    bed_number TEXT,
                    equipment_id TEXT,
                    status TEXT NOT NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS admission_finalization (
                    admission_id TEXT PRIMARY KEY,
                    lab_order_id TEXT,
                    pharmacy_order_id TEXT,
                    status TEXT NOT NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS admission_chain_tracker (
                    admission_id TEXT PRIMARY KEY,
                    chain_status TEXT NOT NULL,
                    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);
        } catch (SQLException e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public static void clearDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM patient_intake;");
            stmt.execute("DELETE FROM bed_allocation;");
            stmt.execute("DELETE FROM admission_finalization;");
            stmt.execute("DELETE FROM admission_chain_tracker;");
        } catch (SQLException e) {
            throw new RuntimeException("Database cleanup failed", e);
        }
    }
}