package br.edu.uesb.prematricula.classgroup.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ClassGroupNotFoundException extends RuntimeException {
    public ClassGroupNotFoundException(String message) {
        super(message);
    }
}