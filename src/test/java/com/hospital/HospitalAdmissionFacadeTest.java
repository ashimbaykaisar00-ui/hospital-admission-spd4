package com.hospital;

import com.hospital.db.DatabaseManager;
import com.hospital.exceptions.HospitalAdmissionException;
import com.hospital.facade.AdmissionAdminFacade;
import com.hospital.facade.ChainTrackerRepository;
import com.hospital.facade.HospitalAdmissionFacade;
import com.hospital.subsystemA.IntakeStatus;
import com.hospital.subsystemA.PatientIntakeRepository;
import com.hospital.subsystemB.AllocationStatus;
import com.hospital.subsystemB.BedAllocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HospitalAdmissionFacadeTest {
    private HospitalAdmissionFacade facade;
    private ChainTrackerRepository trackerRepo;
    private PatientIntakeRepository intakeRepo;
    private BedAllocationRepository bedRepo;
    private AdmissionAdminFacade adminFacade;

    @BeforeEach
    void setUp() {
        DatabaseManager.initializeDatabase();
        DatabaseManager.clearDatabase();
        facade = new HospitalAdmissionFacade();
        trackerRepo = new ChainTrackerRepository();
        intakeRepo = new PatientIntakeRepository();
        bedRepo = new BedAllocationRepository();
        adminFacade = new AdmissionAdminFacade();
    }

    // --- DSL Helper Method for F.I.R.S.T compliance ---
    private void assertAdmissionState(String id, ChainTrackerRepository.ChainStatus expectedChainStatus, IntakeStatus expectedIntakeStatus) {
        assertEquals(expectedChainStatus, trackerRepo.getStatus(id));
        assertEquals(expectedIntakeStatus, intakeRepo.findById(id).getStatus());
    }

    @Test
    @DisplayName("Happy Path: Full Admission Chain Succeeds")
    void testHappyPath() throws Exception {
        String id = "ADM-SUCCESS";
        boolean result = facade.admitPatient(id, "P1", "POL123", "GENERAL", "MONITOR", "BLOOD", "MEDS");

        assertTrue(result);
        assertAdmissionState(id, ChainTrackerRepository.ChainStatus.COMPLETED, IntakeStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Compensation Depth 1: B Fails after A -> Rollback A Alone")
    void testBFailsRollbackA() {
        String id = "ADM-FAIL-B";
        assertThrows(HospitalAdmissionException.class, () ->
                facade.admitPatient(id, "P1", "POL123", "FULL", "MONITOR", "BLOOD", "MEDS")
        );

        assertAdmissionState(id, ChainTrackerRepository.ChainStatus.COMPENSATED_A, IntakeStatus.CANCELLED);
        assertNull(bedRepo.findById(id)); // Bed allocation never saved
    }

    @Test
    @DisplayName("Compensation Depth 2: C Fails after A & B -> Rollback B then A")
    void testCFailsRollbackBThenA() {
        String id = "ADM-FAIL-C";
        assertThrows(HospitalAdmissionException.class, () ->
                facade.admitPatient(id, "P1", "POL123", "GENERAL", "MONITOR", "BLOOD", "OUT_OF_STOCK")
        );

        assertAdmissionState(id, ChainTrackerRepository.ChainStatus.COMPENSATED_B_THEN_A, IntakeStatus.CANCELLED);
        assertEquals(AllocationStatus.RELEASED, bedRepo.findById(id).getStatus());
    }

    @Test
    @DisplayName("Persistence Reload Test: Recover Unfinished Chain From Disk After System Restart")
    void testPersistenceReloadAndRecovery() {
        String id = "ADM-RESTART";
        // Simulate crash mid-way by forcing database state to PENDING_C
        trackerRepo.updateStatus(id, ChainTrackerRepository.ChainStatus.PENDING_C);
        intakeRepo.save(new com.hospital.subsystemA.PatientIntake(id, "P99", "POL99", IntakeStatus.CONFIRMED));
        bedRepo.save(new com.hospital.subsystemB.BedAllocation(id, "BED-1", "EQ-1", AllocationStatus.ALLOCATED));

        // Re-initialize (simulating application restart)
        adminFacade.recoverUnfinishedAdmissions();

        // Assert state was picked up from DB and unwound properly
        assertAdmissionState(id, ChainTrackerRepository.ChainStatus.COMPENSATED_B_THEN_A, IntakeStatus.CANCELLED);
        assertEquals(AllocationStatus.RELEASED, bedRepo.findById(id).getStatus());
    }
}