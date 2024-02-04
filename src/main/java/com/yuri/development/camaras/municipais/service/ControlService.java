package com.yuri.development.camaras.municipais.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yuri.development.camaras.municipais.annotation.HLogger;
import com.yuri.development.camaras.municipais.domain.Control;
import com.yuri.development.camaras.municipais.dto.ControlDTO;
import com.yuri.development.camaras.municipais.enums.EControlType;
import com.yuri.development.camaras.municipais.exception.ApiErrorException;
import com.yuri.development.camaras.municipais.repository.ControlRepository;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.Optional;

import static com.yuri.development.camaras.municipais.util.EventConstants.*;
import static com.yuri.development.camaras.municipais.util.EventConstants.DELETE_SESSION_DESCRIPTION;

@Service
public class ControlService {

    @Autowired
    private ControlRepository controlRepository;

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(SessionService.class.getName());

    @Transactional
    public ResponseEntity<?> create(ControlDTO controlDTO) throws JsonProcessingException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Control control = new Control(controlDTO.getType(), controlDTO.getCommand(), controlDTO.getTownHallId());

        try {
            controlRepository.save(control);
        } catch (DataIntegrityViolationException ex) {
            return new ResponseEntity<>(new ApiErrorException(DATABASE_STRUCUTRE_ERROR, DATABASE_STRUCUTRE_ERROR_DESCRIPTION), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {
            return new ResponseEntity<>(new ApiErrorException(1001, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }

        stopWatch.stop();

        logger.info("Event_id = " + CREATE_CONTROL + ", Event_description = "
                + CREATE_CONTROL_DESCRIPTION + ", Duration(ms) = " + stopWatch.getTotalTimeMillis(), control.getType(), control.getCommand(), control.getTownHallId());

        return new ResponseEntity<>(control, HttpStatus.OK);
    }

    public List<Control> findByTypeAndTownHallId(EControlType controlType, String townHallId) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<Control> controlList = this.controlRepository.findByTypeAndTownHallId(controlType, townHallId);

        stopWatch.stop();

        logger.info("Event_id = " + FIND_CONTROL_BY_TYPE_AND_TOWN_HALL_ID + ", Event_description = "
                + FIND_CONTROL_BY_TYPE_AND_TOWN_HALL_ID_DESCRIPTION + ", Duration(ms) = " + stopWatch.getTotalTimeMillis(), controlType, townHallId);

        return controlList;
    }

    private Optional<Control> findById(Long controlId) {
        return this.controlRepository.findById(controlId);
    }


    public void deleteById(Long controlId) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Optional<Control> control = this.findById(controlId);

        if (control.isPresent()) {
            this.controlRepository.deleteById(controlId);
            stopWatch.stop();
            logger.info("Event_id = " + DELETE_CONTROL + ", Event_description = " + DELETE_CONTROL_DESCRIPTION + ", Duration(ms) = " + stopWatch.getTotalTimeMillis(), controlId);
        }
    }

}
