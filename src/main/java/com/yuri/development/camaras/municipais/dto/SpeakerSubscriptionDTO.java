package com.yuri.development.camaras.municipais.dto;

import com.yuri.development.camaras.municipais.enums.ESpeakerType;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SpeakerSubscriptionDTO {

    @NotNull
    private Long townhallId;

    @NotNull
    private Long parlamentarId;

    @NotNull
    private ESpeakerType type;
}
