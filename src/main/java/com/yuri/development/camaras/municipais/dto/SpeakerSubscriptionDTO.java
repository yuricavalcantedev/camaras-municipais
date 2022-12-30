package com.yuri.development.camaras.municipais.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SpeakerSubscriptionDTO {

    @NotNull
    private Long townhallId;

    @NotNull
    private Long parlamentarId;
}
