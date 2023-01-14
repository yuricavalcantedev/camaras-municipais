package com.yuri.development.camaras.municipais.repository;

import com.yuri.development.camaras.municipais.domain.Parlamentar;
import com.yuri.development.camaras.municipais.domain.TownHall;
import com.yuri.development.camaras.municipais.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);

    @Query(value = "UPDATE users SET is_recovering_password = ? WHERE id = ?", nativeQuery = true)
    void updateRecoveryPassword(Boolean flag, Long id);

    @Query(value = "SELECT * FROM users where is_recovering_password = true", nativeQuery = true)
    List<User> findAllWhoWantsToRecoverPassword();

    @Query(value = "SELECT * FROM users where user_type = ?", nativeQuery = true)
    List<User> findAllUsersByType(String type);

    @Query(value = "SELECT * FROM users WHERE username = ? AND password = ?", nativeQuery = true)
    Optional<User> findByUsernameAndPassword(String username, String password);

    List<Parlamentar> findByTownHall(TownHall townHall);


}
