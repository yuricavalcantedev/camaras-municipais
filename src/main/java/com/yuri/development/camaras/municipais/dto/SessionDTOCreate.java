package com.yuri.development.camaras.municipais.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionDTOCreate implements Serializable {

    @NotNull (message = "Id from sapl can not be null")
    private Long saplSessionId;

    @NotNull(message = "Townhall id can not be null")
    private Long townHallId;
}
