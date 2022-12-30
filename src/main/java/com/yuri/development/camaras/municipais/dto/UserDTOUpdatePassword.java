package com.yuri.development.camaras.municipais.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTOUpdatePassword implements Serializable {

    private Long id;
    private String username;
    private String password;
    private String repeatedPassword;

}
