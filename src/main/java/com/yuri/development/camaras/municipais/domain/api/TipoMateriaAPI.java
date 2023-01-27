package com.yuri.development.camaras.municipais.domain.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties (ignoreUnknown = true)
public class TipoMateriaAPI {

    @JsonProperty("__str__")
    private String name;

    @JsonProperty("sequencia_regimental")
    private Integer sequenceFromSAPL;
}
