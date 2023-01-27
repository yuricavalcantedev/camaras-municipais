package com.yuri.development.camaras.municipais.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuri.development.camaras.municipais.GlobalConstants;
import com.yuri.development.camaras.municipais.domain.LegislativeSubjectType;
import com.yuri.development.camaras.municipais.domain.TownHall;
import com.yuri.development.camaras.municipais.domain.api.TipoMateriaWrapperAPI;
import com.yuri.development.camaras.municipais.enums.EVotingTypeResult;
import com.yuri.development.camaras.municipais.service.LegislativeSubjectTypeService;
import com.yuri.development.camaras.municipais.service.TownHallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BootComponent {

    @Autowired
    private TownHallService townHallService;

    @Autowired
    private LegislativeSubjectTypeService legislativeSubjectTypeService;

    private static RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    @Transactional
    public void loadLegislativeDataForAllTownhalls() throws JsonProcessingException {

        List<TownHall> townHallList = this.townHallService.findAll();
        ObjectMapper objectMapper = new ObjectMapper();

        for(int i = 0; i < townHallList.size(); i++){

            TownHall townHall = townHallList.get(i);
            if(townHall.getLegislativeSubjectTypeList().size() != 0 || townHall.getName().equals("Admin")){
                continue;
            }

            String url = townHall.getApiURL() + GlobalConstants.GET_ALL_TIPO_MATERIA;
            boolean exit = false;
            int pageNumber = 0;

            while(!exit){

                pageNumber = pageNumber + 1;
                TipoMateriaWrapperAPI wrapper = objectMapper.readValue(restTemplate.getForObject(url + "?page=" + pageNumber, String.class), TipoMateriaWrapperAPI.class);
                this.legislativeSubjectTypeService.saveAll(wrapper.getResults().stream().map(item -> new LegislativeSubjectType(townHall, item, EVotingTypeResult.MAIORIA_SIMPLES)).collect(Collectors.toList()));

                exit = wrapper.getPagination().getNextPage() == null;
            }


        }
    }
}
