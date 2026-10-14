package com.freezermanager.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.freezermanager.dto.ErrorResponse;

public class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
    @Test
    public void handleResourceNotFoundExceptionShouldReturnNotFound(){
        NotFoundException ex = new NotFoundException("Recurso no encontrado");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotFound(ex);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(ex.getMessage(), response.getBody().message());
        assertEquals(404, response.getBody().status());
    }

    @Test
    public void genericErrorReturn500InternalServerErrorAndDefaultMessage(){
        RuntimeException exception = new RuntimeException("Error inesperado");
        String expectedMessage = "Se ha producido un error inesperado";
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericError(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotEquals(exception.getMessage(), response.getBody().message());
        assertEquals(expectedMessage, response.getBody().message());
        assertEquals(500, response.getBody().status());
    }
}
