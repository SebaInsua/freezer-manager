package com.freezermanager.dto;

public record ErrorResponse (
    String message,
    int status
) {}
