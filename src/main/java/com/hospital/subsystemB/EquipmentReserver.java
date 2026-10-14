package com.hospital.subsystemB;

public class EquipmentReserver {
    public String reserveEquipment(String requiredType) {
        if ("UNAVAILABLE".equalsIgnoreCase(requiredType)) {
            throw new RuntimeException("Equipment unavailable: " + requiredType);
        }
        return "EQ-" + (int)(Math.random() * 900 + 100);
    }
}