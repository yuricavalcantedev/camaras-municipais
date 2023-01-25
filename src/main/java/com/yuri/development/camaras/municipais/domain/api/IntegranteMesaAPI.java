package com.yuri.development.camaras.municipais.domain.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties (ignoreUnknown = true)
@Getter
@Setter
public class IntegranteMesaAPI {

    private Long id;

    @JsonProperty("__str__")
    private String content;

    @JsonProperty("cargo")
    private Integer cargo;

    @JsonProperty("mesa_diretora")
    private Integer mesaDiretora;

}
