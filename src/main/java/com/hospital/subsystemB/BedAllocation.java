package com.hospital.subsystemB;

public class BedAllocation {
    private final String admissionId;
    private String bedNumber;
    private String equipmentId;
    private AllocationStatus status;

    public BedAllocation(String admissionId, String bedNumber, String equipmentId, AllocationStatus status) {
        this.admissionId = admissionId;
        this.bedNumber = bedNumber;
        this.equipmentId = equipmentId;
        this.status = status;
    }

    public String getAdmissionId() { return admissionId; }
    public String getBedNumber() { return bedNumber; }
    public String getEquipmentId() { return equipmentId; }
    public AllocationStatus getStatus() { return status; }

    public void setStatus(AllocationStatus newStatus) {
        this.status = newStatus;
    }
}