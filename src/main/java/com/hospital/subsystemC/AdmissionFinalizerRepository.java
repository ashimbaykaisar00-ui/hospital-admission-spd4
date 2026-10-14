package com.hospital.subsystemC;

import com.hospital.db.DatabaseManager;
import java.sql.*;

public class AdmissionFinalizerRepository {
    public void save(AdmissionFinalization finalization) {
        String sql = "INSERT INTO admission_finalization (admission_id, lab_order_id, pharmacy_order_id, status) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT(admission_id) DO UPDATE SET status = excluded.status;";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, finalization.getAdmissionId());
            pstmt.setString(2, finalization.getLabOrderId());
            pstmt.setString(3, finalization.getPharmacyOrderId());
            pstmt.setString(4, finalization.getStatus().name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving AdmissionFinalization", e);
        }
    }

    public AdmissionFinalization findById(String admissionId) {
        String sql = "SELECT * FROM admission_finalization WHERE admission_id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, admissionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new AdmissionFinalization(
                        rs.getString("admission_id"),
                        rs.getString("lab_order_id"),
                        rs.getString("pharmacy_order_id"),
                        FinalizationStatus.valueOf(rs.getString("status"))
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching AdmissionFinalization", e);
        }
        return null;
    }
}