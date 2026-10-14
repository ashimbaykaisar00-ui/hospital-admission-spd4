package com.hospital.subsystemA;

public class InsuranceVerifier {
    public boolean verifyInsurance(String policyNumber) {
        if (policyNumber == null || policyNumber.isBlank()) return false;
        if (policyNumber.startsWith("INVALID")) return false;
        return true;
    }
}