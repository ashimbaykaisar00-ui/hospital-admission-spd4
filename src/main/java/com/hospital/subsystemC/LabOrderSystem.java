package com.hospital.subsystemC;

public class LabOrderSystem {
    public String createLabOrder(String testType) {
        if ("INVALID_TEST".equalsIgnoreCase(testType)) {
            throw new RuntimeException("Invalid lab test requested");
        }
        return "LAB-" + (int)(Math.random() * 900 + 100);
    }
}