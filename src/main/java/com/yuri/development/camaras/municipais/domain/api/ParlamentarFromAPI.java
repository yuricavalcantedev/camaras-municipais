package com.yuri.development.camaras.municipais.domain.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ParlamentarFromAPI {

    private Long id;

    @JsonProperty("nome_parlamentar")
    private String nomeParlamentar;

    private String fotografia;
    private String ativo;
    private String titular;
    private String partido;
}
