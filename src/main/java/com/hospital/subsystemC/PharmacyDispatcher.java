package com.hospital.subsystemC;

public class PharmacyDispatcher {
    public String dispatchMeds(String medPackage) {
        if ("OUT_OF_STOCK".equalsIgnoreCase(medPackage)) {
            throw new RuntimeException("Pharmacy medications out of stock!");
        }
        return "PHARM-" + (int)(Math.random() * 900 + 100);
    }
}