package com.yuri.development.camaras.municipais.enums;

public enum EVotingTypeResult {

    MAIORIA_QUALIFICADA ("Maioria qualificada"), MAIORIA_SIMPLES ("Maioria simples"), MAIORIA_ABSOLUTA ("Maioria absoluta");
    String description;

    EVotingTypeResult(String description){
        this.description = description;
    }
    public static EVotingTypeResult searchType(String type){
        for(EVotingTypeResult evotingType : EVotingTypeResult.values()){
            if(evotingType.name().equals(type)){
                return evotingType;
            }
        }
        return null;
    }

    public String getDescription(){
        return this.description;
    }
}
