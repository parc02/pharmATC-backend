package park.pharmatc.v1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import park.pharmatc.v1.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}