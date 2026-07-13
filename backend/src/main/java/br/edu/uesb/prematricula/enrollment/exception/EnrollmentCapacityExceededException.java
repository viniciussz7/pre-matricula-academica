package br.edu.uesb.prematricula.enrollment.exception;

public class EnrollmentCapacityExceededException extends RuntimeException {
    public EnrollmentCapacityExceededException(String message) {
        super(message);
    }
    
}
