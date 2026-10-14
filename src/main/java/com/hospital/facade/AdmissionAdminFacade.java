package com.hospital.facade;

import com.hospital.subsystemA.PatientIntakeRepository;
import com.hospital.subsystemB.BedAllocationRepository;
import com.hospital.subsystemC.AdmissionFinalizerRepository;

import java.util.Map;

public class AdmissionAdminFacade {
    private final ChainTrackerRepository trackerRepo = new ChainTrackerRepository();
    private final PatientIntakeRepository intakeRepo = new PatientIntakeRepository();
    private final BedAllocationRepository bedRepo = new BedAllocationRepository();
    private final AdmissionFinalizerRepository finalizerRepo = new AdmissionFinalizerRepository();
    private final HospitalAdmissionFacade admissionFacade = new HospitalAdmissionFacade();

    public Map<String, ChainTrackerRepository.ChainStatus> getAllAdmissionStatuses() {
        return trackerRepo.getAllTrackers();
    }

    public String getFullDetails(String admissionId) {
        var intake = intakeRepo.findById(admissionId);
        var bed = bedRepo.findById(admissionId);
        var finalization = finalizerRepo.findById(admissionId);
        var chain = trackerRepo.getStatus(admissionId);

        return String.format(
                "=== Admission Details [%s] ===\nChain Status: %s\nSubsystem A (Intake): %s\nSubsystem B (Bed): %s\nSubsystem C (Finalization): %s\n",
                admissionId, chain,
                intake != null ? intake.getStatus() : "N/A",
                bed != null ? bed.getStatus() + " (Bed: " + bed.getBedNumber() + ")" : "N/A",
                finalization != null ? finalization.getStatus() : "N/A"
        );
    }

    public void recoverUnfinishedAdmissions() {
        Map<String, ChainTrackerRepository.ChainStatus> all = trackerRepo.getAllTrackers();
        for (var entry : all.entrySet()) {
            String id = entry.getKey();
            ChainTrackerRepository.ChainStatus status = entry.getValue();

            if (status == ChainTrackerRepository.ChainStatus.PENDING_B) {
                System.out.println("Recovering unfinished admission [" + id + "] stuck at PENDING_B -> Unwinding A...");
                admissionFacade.rollbackSubsystemA(id);
                trackerRepo.updateStatus(id, ChainTrackerRepository.ChainStatus.COMPENSATED_A);
            } else if (status == ChainTrackerRepository.ChainStatus.PENDING_C) {
                System.out.println("Recovering unfinished admission [" + id + "] stuck at PENDING_C -> Unwinding B then A...");
                admissionFacade.rollbackSubsystemB(id);
                admissionFacade.rollbackSubsystemA(id);
                trackerRepo.updateStatus(id, ChainTrackerRepository.ChainStatus.COMPENSATED_B_THEN_A);
            }
        }
    }
}