package com.yuri.development.camaras.municipais.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yuri.development.camaras.municipais.domain.TownHall;
import com.yuri.development.camaras.municipais.dto.SessionDTOCreate;
import com.yuri.development.camaras.municipais.exception.ApiErrorException;
import com.yuri.development.camaras.municipais.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.NoSuchElementException;

import static com.yuri.development.camaras.municipais.util.EventConstants.TOWNHALL_HAS_SESSION_ALREADY;
import static com.yuri.development.camaras.municipais.util.EventConstants.TOWNHALL_NOT_FOUND;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {

    @InjectMocks
    private SessionService sessionService;

    @Mock
    private TownHallService townHallService;

    @Mock
    private ParlamenterService parlamenterService;

    @Mock
    private UserService userService;

    @Mock
    private SubjectService subjectService;

    @Mock
    private ParlamentarPresenceService parlamentarPresenceService;

    @Mock
    private SpeakerService speakerService;

    @Mock
    private VotingService votingService;

    @Mock
    private SessionRepository sessionRepository;

    @BeforeEach
    void setup(){

    }

    @Test
    void when_createSession_withInvalidTownhall_then_return400(){

        SessionDTOCreate sessionDTOCreate = new SessionDTOCreate(10L, 20L);
        doThrow(NoSuchElementException.class).when(townHallService).findTownhallById(anyLong());

        ResponseEntity<?> result =  sessionService.create(sessionDTOCreate);
        ApiErrorException resultError = (ApiErrorException) result.getBody();
        assertEquals(result.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(resultError.getCode(), TOWNHALL_NOT_FOUND);
    }

    @Test
    void when_createSession_withValidValues_and_sessionAlreadyExists_then_return400(){

        SessionDTOCreate sessionDTOCreate = new SessionDTOCreate(10L, 20L);
        doReturn(new TownHall()).when(townHallService).findTownhallById(anyLong());

        ResponseEntity<?> result =  sessionService.create(sessionDTOCreate);
        ApiErrorException resultError = (ApiErrorException) result.getBody();
        assertEquals(result.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(resultError.getCode(), TOWNHALL_HAS_SESSION_ALREADY);
    }

    @Test
    void when_createVoting_withValidParamters_and_sessionNotFound_then_return400() throws JsonProcessingException {

        doReturn(null).when(sessionService).findByUuid(anyString());

        sessionService.createVoting(anyString(), anyList());
    }
}
