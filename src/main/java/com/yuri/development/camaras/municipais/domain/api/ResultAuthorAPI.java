package com.yuri.development.camaras.municipais.domain.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ResultAuthorAPI {
    private int id;

    @JsonProperty("__str__")
    private String str;

    @JsonProperty("primeiro_autor")
    private boolean primeiroAutor;

    private int autor;
}
