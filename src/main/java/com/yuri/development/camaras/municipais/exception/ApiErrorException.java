package com.yuri.development.camaras.municipais.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiErrorException extends Exception {

    private int code;
    private String description;
    private HttpStatus httpStatus;

    public ApiErrorException(int code, String description){
        this.code = code;
        this.description = description;
    }
}
