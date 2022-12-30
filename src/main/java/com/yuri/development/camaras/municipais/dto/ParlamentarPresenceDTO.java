package com.yuri.development.camaras.municipais.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParlamentarPresenceDTO {

    @NotNull
    private Long parlamentarId;

    @NotNull
    private String status;
}
