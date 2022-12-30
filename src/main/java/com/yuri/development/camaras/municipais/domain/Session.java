package com.yuri.development.camaras.municipais.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.hibernate.annotations.Cascade;

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

    @OneToMany(mappedBy = "session", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ParlamentarPresence> parlamentarPresenceList = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Voting> votingList = new ArrayList<>();

    @OneToMany(mappedBy="session", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Subject> subjectList = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.REMOVE)
    @JsonManagedReference
    private List<SpeakerSession> speakerList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "town_hall_id", nullable = false)
    @JsonIgnore
    private TownHall townHall;

    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "speaker_order")
    private Integer speakerOrder = 0;

    public Session(Long saplId, String uuid, String name, TownHall townHall, Date date){

        this.saplId = saplId;
        this.uuid = uuid;
        this.name = name;
        this.townHall = townHall;
        this.date = date;
    }
}
