package com.yuri.development.camaras.municipais.domain.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AuthorAPI {
    @JsonProperty("results")
    private List<ResultAuthorAPI> results;
}