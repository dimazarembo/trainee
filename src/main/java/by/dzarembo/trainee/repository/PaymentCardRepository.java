package by.dzarembo.trainee.repository;

import by.dzarembo.trainee.entity.PaymentCardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentCardRepository extends JpaRepository<PaymentCardEntity, Long>,
        JpaSpecificationExecutor<PaymentCardEntity> {

    @EntityGraph(attributePaths = "user")
    List<PaymentCardEntity> findAllByUserId(Long userId);

    @EntityGraph(attributePaths = "user")
    Page<PaymentCardEntity> findAll(Specification<PaymentCardEntity> specification, Pageable pageable);

    @Query(value = "select count(pce.id) from PaymentCardEntity pce where pce.user.id = :userId and pce.active = true")
    long countByUserIdAndActiveTrue(@Param("userId") Long userId);
}
