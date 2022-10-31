package com.yuri.development.camaras.municipais.domain;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class UserTH {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String login;
    private String password;
    private String secretPhrase;
    private Boolean firstAccess;
}
