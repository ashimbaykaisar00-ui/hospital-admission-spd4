package com.hospital.subsystemB;

import com.hospital.db.DatabaseManager;
import java.sql.*;

public class BedAllocationRepository {
    public void save(BedAllocation allocation) {
        String sql = "INSERT INTO bed_allocation (admission_id, bed_number, equipment_id, status) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT(admission_id) DO UPDATE SET status = excluded.status, bed_number = excluded.bed_number, equipment_id = excluded.equipment_id;";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, allocation.getAdmissionId());
            pstmt.setString(2, allocation.getBedNumber());
            pstmt.setString(3, allocation.getEquipmentId());
            pstmt.setString(4, allocation.getStatus().name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving BedAllocation", e);
        }
    }

    public BedAllocation findById(String admissionId) {
        String sql = "SELECT * FROM bed_allocation WHERE admission_id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, admissionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new BedAllocation(
                        rs.getString("admission_id"),
                        rs.getString("bed_number"),
                        rs.getString("equipment_id"),
                        AllocationStatus.valueOf(rs.getString("status"))
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching BedAllocation", e);
        }
        return null;
    }
}