package com.yuri.development.camaras.municipais.service;
import com.yuri.development.camaras.municipais.domain.Parlamentar;
import com.yuri.development.camaras.municipais.domain.ParlamentarFromAPI;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParlamenterService {
    public List<Parlamentar> getAllParlamentar() {

        String uri = "https://sapl.maracanau.ce.leg.br/api/parlamentares/parlamentar/search_parlamentares/";
        RestTemplate restTemplate = new RestTemplate();
        ParlamentarFromAPI [] returnFromAPI = restTemplate.getForObject(uri, ParlamentarFromAPI[].class);
        List<Parlamentar> list = Arrays.stream(returnFromAPI)
                .map(parlamentarFromAPI -> new Parlamentar(parlamentarFromAPI))
                .filter(parlamentar -> parlamentar.getActive())
                .collect(Collectors.toList());
        return list;
    }
}
