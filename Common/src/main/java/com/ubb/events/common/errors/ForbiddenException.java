package com.ubb.events.common.errors;

import com.ubb.events.dto.ErrorDetail;
import java.util.List;

public class ForbiddenException extends RuntimeException {
    private final List<ErrorDetail> errors;
    public ForbiddenException(String message) { super(message); this.errors = List.of(); }
    public ForbiddenException(String message, List<ErrorDetail> errors) { super(message); this.errors = errors; }
    public List<ErrorDetail> getErrors() { return errors; }
}
