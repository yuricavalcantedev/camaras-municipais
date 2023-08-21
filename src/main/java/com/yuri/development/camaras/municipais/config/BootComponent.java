package com.yuri.development.camaras.municipais.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuri.development.camaras.municipais.GlobalConstants;
import com.yuri.development.camaras.municipais.domain.LegislativeSubjectType;
import com.yuri.development.camaras.municipais.domain.Role;
import com.yuri.development.camaras.municipais.domain.TableRole;
import com.yuri.development.camaras.municipais.domain.TownHall;
import com.yuri.development.camaras.municipais.domain.api.TipoMateriaWrapperAPI;
import com.yuri.development.camaras.municipais.service.LegislativeSubjectTypeService;
import com.yuri.development.camaras.municipais.service.RoleService;
import com.yuri.development.camaras.municipais.service.TableRoleService;
import com.yuri.development.camaras.municipais.service.TownHallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BootComponent {

    @Autowired
    private TownHallService townHallService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private TableRoleService tableRoleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LegislativeSubjectTypeService legislativeSubjectTypeService;

    java.util.logging.Logger logger = java.util.logging.Logger.getLogger(BootComponent.class.getName());

    @PostConstruct
    @Transactional
    private void loadDefaultData() throws JsonProcessingException {

        this.logger.info("loadDefaultData() started!");

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
            TownHall townHallDB = this.townHallService.findByApiURL(townHall.getApiURL());
            if(townHallDB == null){
                this.townHallService.create(townHall);
            }

            List<TableRole> tableRoleList = this.tableRoleService.findAllByTownhall(townHallDB);
            if(tableRoleList.isEmpty()){
                townHallDB.setTableRoleList(this.tableRoleService.saveAll(this.createDefaultTableRoleForTownhallId(townHallDB)));
                townHallService.update(townHallDB);
            }

        }

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

    private List<TableRole> createDefaultTableRoleForTownhallId(TownHall townhall){

        List<TableRole> tableRoleList = new ArrayList<>();
        tableRoleList.add(new TableRole( townhall, "PRES", 1 ));
        tableRoleList.add(new TableRole( townhall, "1º VICE", 2 ));
        tableRoleList.add(new TableRole( townhall, "2º VICE", 3 ));
        tableRoleList.add(new TableRole( townhall, "1º SEC", 4 ));
        tableRoleList.add(new TableRole( townhall, "2º SEC", 5 ));
        tableRoleList.add(new TableRole( townhall, "3º SEC", 6 ));

        return tableRoleList;
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
