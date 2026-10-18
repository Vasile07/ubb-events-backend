package com.ubb.events.common.errors;

import com.ubb.events.dto.ErrorDetail;
import java.util.List;

public class ConflictException extends RuntimeException {
    private final List<ErrorDetail> errors;
    public ConflictException(String message) { super(message); this.errors = List.of(); }
    public ConflictException(String message, List<ErrorDetail> errors) { super(message); this.errors = errors; }
    public List<ErrorDetail> getErrors() { return errors; }
}
