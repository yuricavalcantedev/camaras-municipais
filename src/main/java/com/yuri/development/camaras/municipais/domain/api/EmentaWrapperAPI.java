package com.yuri.development.camaras.municipais.domain.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties (ignoreUnknown = true)
@Data
public class EmentaWrapperAPI {

    private PaginationFromAPI pagination;

    private List<EmentaAPI> results;
}
