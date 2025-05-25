package com.yuri.development.camaras.municipais.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class UpdateParlamentarInfoDTO {

    @NotBlank(message = "ID do parlamentar não pode ser nulo")
    private Long id;

    @NotBlank(message = "Nome do parlamentar não pode ser nulo")
    private String name;
}
