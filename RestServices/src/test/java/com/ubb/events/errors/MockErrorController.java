package com.ubb.events.errors;

import com.ubb.events.common.errors.BusinessRuleException;
import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.common.errors.ForbiddenException;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.common.errors.ValidationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test/errors")
public class MockErrorController {
    @GetMapping("/business")
    @ApiResponse(responseCode = "400", description = "Business rule violation",
            content = @Content(schema = @Schema(implementation = com.ubb.events.dto.ErrorResponse.class)))
    public void business() { throw new BusinessRuleException("Rule failed"); }
    @GetMapping("/not-found") public void notFound() { throw new NotFoundException("Missing resource"); }
    @GetMapping("/conflict") public void conflict() { throw new ConflictException("State conflict"); }
    @GetMapping("/forbidden") public void forbidden() { throw new ForbiddenException("Access denied"); }
    @GetMapping("/validation-exception") public void validationException() { throw new ValidationException("Invalid operation"); }
    @GetMapping("/unexpected") public void unexpected() { throw new IllegalStateException("secret implementation detail"); }

    @PostMapping("/validate")
    public ResponseEntity<String> validate(@Valid @RequestBody Input input) { return ResponseEntity.ok(input.getValue()); }

    public static class Input {
        @NotBlank(message = "value is required")
        private String value;

        public Input() { }

        public String getValue() { return value; }

        public void setValue(String value) { this.value = value; }
    }
}
