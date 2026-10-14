package com.hospital;

import com.hospital.db.DatabaseManager;
import com.hospital.facade.AdmissionAdminFacade;
import com.hospital.facade.HospitalAdmissionFacade;

import java.util.Scanner;

public class MainApp {
    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();
        HospitalAdmissionFacade mainFacade = new HospitalAdmissionFacade();
        AdmissionAdminFacade adminFacade = new AdmissionAdminFacade();
        Scanner scanner = new Scanner(System.in);

        System.out.println("==================================================");
        System.out.println("   HOSPITAL ADMISSION FACADE SYSTEM (DEMO RUNNER) ");
        System.out.println("==================================================");

        while (true) {
            System.out.println("\nSelect Scenario/Action:");
            System.out.println("1. Success Path (A -> B -> C Complete)");
            System.out.println("2. Subsystem B Failure (Trigger Rollback A)");
            System.out.println("3. Subsystem C Failure (Trigger Rollback B -> A)");
            System.out.println("4. Pre-check Failure (Invalid Insurance)");
            System.out.println("5. Inspect Persisted Database");
            System.out.println("6. Run Recovery Check for Unfinished Chains (Crash Recovery Test)");
            System.out.println("7. Clear/Reset Database");
            System.out.println("0. Exit");
            System.out.print("> ");

            String choice = scanner.nextLine();
            String id = "ADM-" + System.currentTimeMillis() % 10000;

            try {
                switch (choice) {
                    case "1":
                        System.out.println("\n[Exec] Starting Normal Admission...");
                        mainFacade.admitPatient(id, "PAT-101", "POL-999", "GENERAL", "MONITOR", "BLOOD_TEST", "STANDARD_MEDS");
                        System.out.println(">>> Admission Completed Successfully! ID: " + id);
                        break;
                    case "2":
                        System.out.println("\n[Exec] Starting Admission with Ward=FULL (Subsystem B Fail)...");
                        mainFacade.admitPatient(id, "PAT-102", "POL-999", "FULL", "MONITOR", "BLOOD_TEST", "STANDARD_MEDS");
                        break;
                    case "3":
                        System.out.println("\n[Exec] Starting Admission with Meds=OUT_OF_STOCK (Subsystem C Fail)...");
                        mainFacade.admitPatient(id, "PAT-103", "POL-999", "GENERAL", "MONITOR", "BLOOD_TEST", "OUT_OF_STOCK");
                        break;
                    case "4":
                        System.out.println("\n[Exec] Starting Admission with Invalid Insurance...");
                        mainFacade.admitPatient(id, "PAT-104", "INVALID_POL", "GENERAL", "MONITOR", "BLOOD_TEST", "STANDARD_MEDS");
                        break;
                    case "5":
                        System.out.println("\n--- Current DB Trackers ---");
                        adminFacade.getAllAdmissionStatuses().forEach((admId, status) -> {
                            System.out.println(adminFacade.getFullDetails(admId));
                        });
                        break;
                    case "6":
                        System.out.println("\n[Admin] Checking and recovering incomplete chains...");
                        adminFacade.recoverUnfinishedAdmissions();
                        break;
                    case "7":
                        DatabaseManager.clearDatabase();
                        System.out.println("\nDatabase cleared successfully.");
                        break;
                    case "0":
                        System.out.println("Exiting System.");
                        return;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.err.println(">>> CAUGHT EXPECTED EXCEPTION: " + e.getMessage());
            }
        }
    }
}