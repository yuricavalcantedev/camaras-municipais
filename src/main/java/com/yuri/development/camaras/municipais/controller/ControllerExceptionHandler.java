package com.yuri.development.camaras.municipais.controller;

import com.yuri.development.camaras.municipais.exception.ApiErrorException;
import com.yuri.development.camaras.municipais.exception.RSVException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;

import java.util.logging.Level;
import java.util.logging.Logger;

@ControllerAdvice
public class ControllerExceptionHandler {

    private final Logger logger = Logger.getLogger(ControllerExceptionHandler.class.getName());

    @ExceptionHandler
    public ResponseEntity<?> handleApiErrorException(ApiErrorException e){
        logger.info(e.getDescription());
        return new ResponseEntity<>(e.getDescription(), e.getHttpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<?> handleRSVException(RSVException e){
        logger.info(e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<?> handleRestClientException(RestClientException e){
        logger.log(Level.SEVERE, e.getMessage());
        return new ResponseEntity<>("Comunicação com o SAPL não foi estabelecida. " +
                "Verifique se o SAPL está funcionando e tente novamente.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<?> handleUnexpectedError(Exception e){
        logger.log(Level.SEVERE, e.getMessage());
        return new ResponseEntity<>("Algo inesperado aconteceu, contate o administrador", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
