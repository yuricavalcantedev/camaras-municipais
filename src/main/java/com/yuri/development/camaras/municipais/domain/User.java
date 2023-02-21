package com.yuri.development.camaras.municipais.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name= "users", uniqueConstraints = { @UniqueConstraint(columnNames = "username") })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="user_type", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("U")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 25)
    private String username;

    @NotBlank
    private String password;

    private Boolean isRecoveringPassword;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "town_hall_id", nullable = false)
    @JsonBackReference
    private TownHall townHall;

    @Transient
    private String townhallName;
    public User (String name, String username, String password){

        this(name, username);
        this.password = password;
    }

    public User(String name, String username){
        this.name = name;
        this.username = username;
    }

    public User(Long id, String name, String username, List<Role> roles, boolean isRecoveringPassword){
        this(name, username);
        this.id = id;
        this.roles = roles;
        this.isRecoveringPassword = isRecoveringPassword;
    }

    public User(User user, TownHall townHall){
        this(user.getName(), user.getUsername());
        this.id = user.getId();
        this.isRecoveringPassword = user.getIsRecoveringPassword();
        this.roles = user.getRoles();
        this.townHall = townHall;
        this.townhallName = townHall.getName();
    }
}
