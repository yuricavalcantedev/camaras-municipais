package com.yuri.development.camaras.municipais.payload;

import lombok.Data;

@Data
public class SignupRequest {

    private String name;
    private String username;
    private String password;
}
