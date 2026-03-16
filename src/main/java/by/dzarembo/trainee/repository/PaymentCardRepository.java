package by.dzarembo.trainee.repository;

import by.dzarembo.trainee.entity.PaymentCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentCardRepository extends JpaRepository<PaymentCardEntity, Long>,
        JpaSpecificationExecutor<PaymentCardEntity> {
    List<PaymentCardEntity> findAllByUserId(Long userId);

    @Query(value = "select count(pce.id) from PaymentCardEntity pce where pce.user.id =:userId")
    long countByUserId(@Param("userId") Long userId);
}
