package com.yuri.development.camaras.municipais.domain.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubjectAPI {

    @JsonProperty("__str__")
    private String content;

    @JsonProperty("materia")
    private Integer materiaId;

    @JsonProperty("numero_ordem")
    private Integer order;
}
