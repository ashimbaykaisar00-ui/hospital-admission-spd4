package com.hospital.subsystemA;

public class PatientIntake {
    private final String admissionId;
    private final String patientId;
    private final String insurancePolicy;
    private IntakeStatus status;

    public PatientIntake(String admissionId, String patientId, String insurancePolicy, IntakeStatus status) {
        this.admissionId = admissionId;
        this.patientId = patientId;
        this.insurancePolicy = insurancePolicy;
        this.status = status;
    }

    public String getAdmissionId() { return admissionId; }
    public String getPatientId() { return patientId; }
    public String getInsurancePolicy() { return insurancePolicy; }
    public IntakeStatus getStatus() { return status; }

    public void setStatus(IntakeStatus newStatus) {
        if (this.status == IntakeStatus.CANCELLED && newStatus == IntakeStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot confirm a cancelled intake.");
        }
        this.status = newStatus;
    }
}