package com.yuri.development.camaras.municipais.domain.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EmentaAPI {

    @JsonProperty("ementa")
    private String content;

    @JsonProperty("texto_original")
    private String originalTextUrl;

    @JsonProperty("autores")
    private ArrayList<Number> authors;
}
