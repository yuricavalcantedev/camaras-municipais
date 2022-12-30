package com.yuri.development.camaras.municipais.domain.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

public class DayOrderPayLoadFromAPI {

    private PaginationFromAPI pagination;
    private DayOrderFromAPI [] results;
}
