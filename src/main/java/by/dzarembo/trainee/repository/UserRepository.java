package by.dzarembo.trainee.repository;

import by.dzarembo.trainee.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long>,
        JpaSpecificationExecutor<UserEntity> {

    @Query(value = "select * from users where id = :id for update", nativeQuery = true)
    Optional<UserEntity> findByIdForUpdate(@Param("id") Long id);

}
