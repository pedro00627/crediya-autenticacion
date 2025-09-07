package co.com.pragma.api.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorBody(
        int status,
        String error,
        String message,
        Map<String, String> messages
) {
}