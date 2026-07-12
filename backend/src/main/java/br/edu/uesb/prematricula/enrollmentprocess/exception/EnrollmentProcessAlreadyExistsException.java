package br.edu.uesb.prematricula.enrollmentprocess.exception;

public class EnrollmentProcessAlreadyExistsException extends RuntimeException {
     
    public EnrollmentProcessAlreadyExistsException(String message) {
        super(message);
    }
}
