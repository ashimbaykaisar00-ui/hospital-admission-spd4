package com.hospital.subsystemB;

public class BedAllocator {
    public String allocateBed(String wardType) {
        if ("FULL".equalsIgnoreCase(wardType)) {
            throw new RuntimeException("No beds available in ward: " + wardType);
        }
        return "BED-" + (int)(Math.random() * 900 + 100);
    }
}