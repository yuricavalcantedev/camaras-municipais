package com.yuri.development.camaras.municipais.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.yuri.development.camaras.municipais.domain.api.IntegranteMesaAPI;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "roles_in_session")
public class RoleInSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_uuid", nullable = false)
    @JsonBackReference
    @NotNull
    private Session session;

    @NotNull
    private String role;

    @NotNull
    private String parlamentarName;

    private Integer priority;

    private Integer saplTableNumber;

     public RoleInSession (Session session, String role, String parlamentarName, Integer priority, Integer saplTableNumber){
        this(session, role, parlamentarName, priority);
        this.saplTableNumber = saplTableNumber;
    }

    public RoleInSession (Session session, String role, String parlamentarName, Integer priority){
        this.session = session;
        this.role = role;
        this.parlamentarName = parlamentarName;
        this.priority = priority;
    }
}
