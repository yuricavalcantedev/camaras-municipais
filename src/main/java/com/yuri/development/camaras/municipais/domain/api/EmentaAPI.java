package com.yuri.development.camaras.municipais.domain.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EmentaAPI {

    @JsonProperty("ementa")
    private String content;
}
