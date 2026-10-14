package com.hospital.exceptions;

public class HospitalAdmissionException extends Exception {
    public HospitalAdmissionException(String message) { super(message); }
    public HospitalAdmissionException(String message, Throwable cause) { super(message, cause); }
}