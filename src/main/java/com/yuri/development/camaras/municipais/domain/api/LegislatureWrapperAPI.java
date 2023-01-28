package com.yuri.development.camaras.municipais.domain.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegislatureWrapperAPI {

    private PaginationFromAPI pagination;
    private List<LegislatureAPI> results;
}
