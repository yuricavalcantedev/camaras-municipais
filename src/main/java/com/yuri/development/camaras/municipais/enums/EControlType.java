package com.yuri.development.camaras.municipais.enums;

import lombok.Getter;

@Getter
public enum EControlType {
    TIME("timeControl");

    private final String value;

    EControlType(String value) {
        this.value = value;
    }
}