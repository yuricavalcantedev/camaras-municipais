package com.yuri.development.camaras.municipais.controller.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddSubjectRequest {

    private Integer saplMateriaId;
    private String description;
    private String originalTextUrl;
    private Integer subjectOrderSapl;
}
