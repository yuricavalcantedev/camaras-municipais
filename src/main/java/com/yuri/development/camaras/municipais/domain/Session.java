package com.yuri.development.camaras.municipais.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.yuri.development.camaras.municipais.domain.api.SessionFromAPI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
@Getter
@Setter
@Table(name = "sessions")
@AllArgsConstructor
@NoArgsConstructor
public class Session implements Serializable {

    @Id
    private String uuid;

    @NotNull
    private Long saplId;
    @NotNull
    private String name;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ParlamentarPresence> parlamentarPresenceList = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Voting> votingList = new ArrayList<>();

    @OneToMany(mappedBy="session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "session-subject")
    @OrderBy("subjectOrderSapl")
    private List<Subject> subjectList = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<SpeakerSession> speakerList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "town_hall_id", nullable = false)
    @JsonBackReference
    private TownHall townHall;

    @Temporal(TemporalType.DATE)
    private Date date;

    private String startTime;
    private String endTime;
    private String startDate;
    private String endDate;

    public Session(Long saplId, String uuid, SessionFromAPI sessionFromAPI, TownHall townHall, Date date){

        this.saplId = saplId;
        this.uuid = uuid;
        this.name = sessionFromAPI.getTitle();
        this.townHall = townHall;
        this.date = date;
        this.startDate = sessionFromAPI.getStartDateString();
        this.endDate = sessionFromAPI.getEndDateString();
        this.startTime = sessionFromAPI.getStartTimeString();
        this.endTime = sessionFromAPI.getEndTimeString();
    }
}
