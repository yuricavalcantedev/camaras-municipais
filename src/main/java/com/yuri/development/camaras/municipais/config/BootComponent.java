package com.yuri.development.camaras.municipais.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuri.development.camaras.municipais.GlobalConstants;
import com.yuri.development.camaras.municipais.domain.LegislativeSubjectType;
import com.yuri.development.camaras.municipais.domain.Role;
import com.yuri.development.camaras.municipais.domain.TownHall;
import com.yuri.development.camaras.municipais.domain.User;
import com.yuri.development.camaras.municipais.domain.api.TipoMateriaWrapperAPI;
import com.yuri.development.camaras.municipais.service.LegislativeSubjectTypeService;
import com.yuri.development.camaras.municipais.service.RoleService;
import com.yuri.development.camaras.municipais.service.TownHallService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class BootComponent {

    @Autowired
    private TownHallService townHallService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LegislativeSubjectTypeService legislativeSubjectTypeService;

    Logger logger = Logger.getLogger(BootComponent.class.getName());

    @PostConstruct
    @Transactional
    private void loadDefaultData() throws JsonProcessingException {

        this.logger.info("loadDefaultData() started!");
        this.logger.info("loadTownhalls() started!");
        List<TownHall> auxTownHallList = new ArrayList<>();
        auxTownHallList.add(new TownHall("Admin", "Admin", "empty"));
        auxTownHallList.add(new TownHall("Aquiraz", "Câmara Municipal de Aquiraz ", "https://sapl.aquiraz.ce.leg.br/api/"));
        auxTownHallList.add(new TownHall("Canindé", "Câmara Municipal de Canindé", "https://sapl.caninde.ce.leg.br/api/"));
        auxTownHallList.add(new TownHall("Cascavel", "Câmara Municipal de Cascavel", "https://sapl.cascavel.ce.leg.br/api/"));
        auxTownHallList.add(new TownHall("Beberibe", "Câmara Municipal de Beberibe", "https://sapl.beberibe.ce.leg.br/api/"));
        auxTownHallList.add(new TownHall("Eusébio", "Câmara Municipal de Eusébio", "https://sapl.eusebio.ce.leg.br/api/"));
        auxTownHallList.add(new TownHall("Irauçuba", "Câmara Municipal de Irauçuba", "https://sapl.iraucuba.ce.leg.br/api/"));
        auxTownHallList.add(new TownHall("Maracanaú", "Câmara Municipal de Maracanaú", "https://sapl.maracanau.ce.leg.br/api/"));
        auxTownHallList.add(new TownHall("São Gonçalo do Amarante", "Câmara Municipal de São Gonçalo do Amarante", "https://sapl.saogoncalodoamarante.ce.leg.br/api/"));
        auxTownHallList.add(new TownHall("Horizonte", "Câmara Municipal de Horizonte", "https://sapl.horizonte.ce.leg.br/api/"));

        for(TownHall townHall : auxTownHallList){
            if(this.townHallService.findByApiURL(townHall.getApiURL()) == null){
                this.townHallService.create(townHall);
            }
        }

        this.logger.info("loadTownhalls() finished");

        this.logger.info("loadRoles() started");

        List<Role> roleList = new ArrayList<>();
        roleList.add(new Role("ROLE_USER"));
        roleList.add(new Role("ROLE_MODERATOR"));
        roleList.add(new Role("ROLE_MODERATOR_VIEW"));
        roleList.add(new Role("ROLE_ADMIN"));

        if(this.roleService.getAll().size() == 0){
            for(Role role: roleList){
                this.roleService.save(role);
            }
        }

        this.logger.info("loadRoles() finished");

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        List<TownHall> townHallList = this.townHallService.findAll();

        for (TownHall townHall : townHallList) {
            if (townHall.getLegislativeSubjectTypeList().size() != 0 || townHall.getName().equals("Admin")) {
                continue;
            }
            this.saveLegislativeDataForTownHall(townHall, restTemplate, objectMapper);
        }

        this.logger.info("loadDefaultData() finished");
    }

    private void saveLegislativeDataForTownHall(TownHall townHall, RestTemplate restTemplate, ObjectMapper objectMapper) throws JsonProcessingException {

        String url = townHall.getApiURL() + GlobalConstants.GET_ALL_TIPO_MATERIA;
        boolean exit = false;
        int pageNumber = 0;

        while(!exit){

            pageNumber = pageNumber + 1;
            TipoMateriaWrapperAPI wrapper = objectMapper.readValue(restTemplate.getForObject(url + "?page=" + pageNumber, String.class), TipoMateriaWrapperAPI.class);
            this.legislativeSubjectTypeService.saveAll(wrapper.getResults().stream().map(item -> new LegislativeSubjectType(townHall, item)).collect(Collectors.toList()));
            exit = wrapper.getPagination().getNextPage() == null;
        }
    }
}
