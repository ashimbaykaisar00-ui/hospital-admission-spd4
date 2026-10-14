package com.hospital.facade;

import com.hospital.db.DatabaseManager;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ChainTrackerRepository {
    public enum ChainStatus {
        PENDING_A, PENDING_B, PENDING_C, COMPLETED, COMPENSATED_A, COMPENSATED_B_THEN_A
    }

    public void updateStatus(String admissionId, ChainStatus status) {
        String sql = "INSERT INTO admission_chain_tracker (admission_id, chain_status) VALUES (?, ?) " +
                "ON CONFLICT(admission_id) DO UPDATE SET chain_status = excluded.chain_status, last_updated = CURRENT_TIMESTAMP;";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, admissionId);
            pstmt.setString(2, status.name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating chain tracker status", e);
        }
    }

    public ChainStatus getStatus(String admissionId) {
        String sql = "SELECT chain_status FROM admission_chain_tracker WHERE admission_id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, admissionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return ChainStatus.valueOf(rs.getString("chain_status"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching chain status", e);
        }
        return null;
    }

    public Map<String, ChainStatus> getAllTrackers() {
        Map<String, ChainStatus> map = new HashMap<>();
        String sql = "SELECT admission_id, chain_status FROM admission_chain_tracker";
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("admission_id"), ChainStatus.valueOf(rs.getString("chain_status")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all trackers", e);
        }
        return map;
    }
}