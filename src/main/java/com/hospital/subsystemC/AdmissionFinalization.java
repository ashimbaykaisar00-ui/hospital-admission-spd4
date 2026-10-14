package com.hospital.subsystemC;

public class AdmissionFinalization {
    private final String admissionId;
    private String labOrderId;
    private String pharmacyOrderId;
    private FinalizationStatus status;

    public AdmissionFinalization(String admissionId, String labOrderId, String pharmacyOrderId, FinalizationStatus status) {
        this.admissionId = admissionId;
        this.labOrderId = labOrderId;
        this.pharmacyOrderId = pharmacyOrderId;
        this.status = status;
    }

    public String getAdmissionId() { return admissionId; }
    public String getLabOrderId() { return labOrderId; }
    public String getPharmacyOrderId() { return pharmacyOrderId; }
    public FinalizationStatus getStatus() { return status; }

    public void setStatus(FinalizationStatus newStatus) {
        this.status = newStatus;
    }
}