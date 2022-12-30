package com.yuri.development.camaras.municipais.payload;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LoginRequest {

    @NotNull
    private String username;

    @NotNull
    private String password;
}
