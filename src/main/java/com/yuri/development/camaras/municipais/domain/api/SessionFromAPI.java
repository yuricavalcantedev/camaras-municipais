package com.yuri.development.camaras.municipais.domain.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SessionFromAPI {

    private Long id;

    @JsonProperty("__str__")
    private String title;

    @JsonProperty("data_inicio")
    private String startDateString;

    @JsonProperty("data_fim")
    private String endDateString;

    @JsonProperty("hora_inicio")
    private String startTimeString;

    @JsonProperty("hora_fim")
    private String endTimeString;

    @JsonProperty("sessao_legislativa")
    private Integer legislativeSession;

    @JsonProperty("legislatura")
    private Integer legislature;
}
