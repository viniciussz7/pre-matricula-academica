package br.edu.uesb.prematricula.student.exception;

public class RegistrationNumberAlreadyExistsException extends RuntimeException {
    public RegistrationNumberAlreadyExistsException(String message) {
        super(message);
    }
}
