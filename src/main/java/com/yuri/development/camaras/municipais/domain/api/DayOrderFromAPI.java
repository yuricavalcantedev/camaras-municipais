package com.yuri.development.camaras.municipais.domain.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DayOrderFromAPI {

    private Integer id;

    private String __str__;
    private Object[] metadata;
    private String data_ordem;
    private String observacao;
    private Integer numero_ordem;
    private String resultado;
    private Integer tipo_votacao;
    private Boolean votacao_aberta;
    private Boolean registro_aberto;
    private Integer sessao_plenaria;
    private Integer materia;
    private Object tramitacao;
}
