package com.hospital.facade;

import com.hospital.exceptions.HospitalAdmissionException;
import com.hospital.subsystemA.*;
import com.hospital.subsystemB.*;
import com.hospital.subsystemC.*;

public class HospitalAdmissionFacade {
    private final InsuranceVerifier insuranceVerifier = new InsuranceVerifier();
    private final PatientIntakeRepository intakeRepo = new PatientIntakeRepository();

    private final BedAllocator bedAllocator = new BedAllocator();
    private final EquipmentReserver equipmentReserver = new EquipmentReserver();
    private final BedAllocationRepository bedRepo = new BedAllocationRepository();

    private final LabOrderSystem labOrderSystem = new LabOrderSystem();
    private final PharmacyDispatcher pharmacyDispatcher = new PharmacyDispatcher();
    private final AdmissionFinalizerRepository finalizerRepo = new AdmissionFinalizerRepository();

    private final ChainTrackerRepository trackerRepo = new ChainTrackerRepository();

    public boolean admitPatient(String admissionId, String patientId, String policy, String wardType, String equipmentType, String labTest, String medPackage) throws HospitalAdmissionException {

        // --- PRE-CHECK VALIDATION ---
        if (!insuranceVerifier.verifyInsurance(policy)) {
            throw new HospitalAdmissionException("Invalid insurance policy. Admission rejected before processing.");
        }

        // --- STEP A: INTAKE REGISTRATION ---
        trackerRepo.updateStatus(admissionId, ChainTrackerRepository.ChainStatus.PENDING_A);
        PatientIntake intake = new PatientIntake(admissionId, patientId, policy, IntakeStatus.CONFIRMED);
        intakeRepo.save(intake);

        // Transition to B
        trackerRepo.updateStatus(admissionId, ChainTrackerRepository.ChainStatus.PENDING_B);

        // --- STEP B: BED & EQUIPMENT ALLOCATION ---
        String bedNum = null;
        String eqId = null;
        try {
            bedNum = bedAllocator.allocateBed(wardType);
            eqId = equipmentReserver.reserveEquipment(equipmentType);
            BedAllocation allocation = new BedAllocation(admissionId, bedNum, eqId, AllocationStatus.ALLOCATED);
            bedRepo.save(allocation);
        } catch (Exception e) {
            // STEP B FAILED -> COMPENSATE A
            rollbackSubsystemA(admissionId);
            trackerRepo.updateStatus(admissionId, ChainTrackerRepository.ChainStatus.COMPENSATED_A);
            throw new HospitalAdmissionException("Subsystem B failed. Rollback A performed: " + e.getMessage(), e);
        }

        // Transition to C
        trackerRepo.updateStatus(admissionId, ChainTrackerRepository.ChainStatus.PENDING_C);

        // --- STEP C: LAB & PHARMACY FINALIZATION ---
        try {
            String labId = labOrderSystem.createLabOrder(labTest);
            String pharmId = pharmacyDispatcher.dispatchMeds(medPackage);
            AdmissionFinalization finalization = new AdmissionFinalization(admissionId, labId, pharmId, FinalizationStatus.DISPATCHED);
            finalizerRepo.save(finalization);
        } catch (Exception e) {
            // STEP C FAILED -> COMPENSATE B THEN A
            rollbackSubsystemB(admissionId);
            rollbackSubsystemA(admissionId);
            trackerRepo.updateStatus(admissionId, ChainTrackerRepository.ChainStatus.COMPENSATED_B_THEN_A);
            throw new HospitalAdmissionException("Subsystem C failed. Two-step Rollback (B then A) performed: " + e.getMessage(), e);
        }

        // CHAIN COMPLETED
        trackerRepo.updateStatus(admissionId, ChainTrackerRepository.ChainStatus.COMPLETED);
        return true;
    }

    public void rollbackSubsystemA(String admissionId) {
        PatientIntake intake = intakeRepo.findById(admissionId);
        if (intake != null) {
            intake.setStatus(IntakeStatus.CANCELLED);
            intakeRepo.save(intake);
        }
    }

    public void rollbackSubsystemB(String admissionId) {
        BedAllocation allocation = bedRepo.findById(admissionId);
        if (allocation != null) {
            allocation.setStatus(AllocationStatus.RELEASED);
            bedRepo.save(allocation);
        }
    }
}