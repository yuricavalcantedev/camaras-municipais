package com.yuri.development.camaras.municipais.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponse {

    private Long id;
    private String name;
    private String username;
    private String password;
    private List<String> roles;

    public UserInfoResponse(Long id, String name, String username, List<String> roles){
        this.id = id;
        this.name = name;
        this.username = username;
        this.roles = roles;
    }
}
