package com.yuri.development.camaras.municipais.service;
import com.yuri.development.camaras.municipais.domain.Parlamentar;
import com.yuri.development.camaras.municipais.domain.ParlamentarFromAPI;
import com.yuri.development.camaras.municipais.domain.TownHall;
import com.yuri.development.camaras.municipais.repository.TownHallRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ParlamenterService {

    @Autowired
    private TownHallRepository townHallRepository;

    private List<Parlamentar> findAll(String apiURL) {

        String uri = apiURL;
        RestTemplate restTemplate = new RestTemplate();
        ParlamentarFromAPI [] returnFromAPI = restTemplate.getForObject(uri, ParlamentarFromAPI[].class);
        List<Parlamentar> list = Arrays.stream(returnFromAPI)
                .map(parlamentarFromAPI -> new Parlamentar(parlamentarFromAPI))
                .filter(parlamentar -> parlamentar.getActive())
                .collect(Collectors.toList());
        return list;
    }

    public List<Parlamentar> findAllByTownHall(Long id){

        List<Parlamentar> parlamentarList = new ArrayList<>();
        Optional< TownHall> townHall = this.townHallRepository.findById(id);
        if(townHall.isPresent()){
           parlamentarList.addAll(this.findAll(townHall.get().getApiURL()));
        }

        return parlamentarList;
    }
}
