package com.yuri.development.camaras.municipais.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRoleDTO {

    private Long parlamentarId;
    private Long townHallId;
}
