package com.hospital.subsystemA;

import com.hospital.db.DatabaseManager;
import java.sql.*;

public class PatientIntakeRepository {
    public void save(PatientIntake intake) {
        String sql = "INSERT INTO patient_intake (admission_id, patient_id, insurance_policy, status) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT(admission_id) DO UPDATE SET status = excluded.status;";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, intake.getAdmissionId());
            pstmt.setString(2, intake.getPatientId());
            pstmt.setString(3, intake.getInsurancePolicy());
            pstmt.setString(4, intake.getStatus().name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving PatientIntake", e);
        }
    }

    public PatientIntake findById(String admissionId) {
        String sql = "SELECT * FROM patient_intake WHERE admission_id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, admissionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new PatientIntake(
                        rs.getString("admission_id"),
                        rs.getString("patient_id"),
                        rs.getString("insurance_policy"),
                        IntakeStatus.valueOf(rs.getString("status"))
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching PatientIntake", e);
        }
        return null;
    }
}