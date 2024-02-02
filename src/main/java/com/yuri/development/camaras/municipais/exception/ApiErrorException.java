package com.yuri.development.camaras.municipais.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiErrorException extends Exception {

    private int code;
    private String description;
}
