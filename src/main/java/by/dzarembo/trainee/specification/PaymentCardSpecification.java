package by.dzarembo.trainee.specification;

import by.dzarembo.trainee.entity.PaymentCardEntity;
import by.dzarembo.trainee.entity.UserEntity;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecification {
    public static Specification<PaymentCardEntity> hasUserName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isBlank()) {
                return criteriaBuilder.conjunction();
            } else {
                Join<PaymentCardEntity, UserEntity> userJoin = root.join("user");
                return criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("name")), "%" + name.toLowerCase() + "%");
            }
        };
    }

    public static Specification<PaymentCardEntity> hasUserSurname(String surname) {
        return (root, query, criteriaBuilder) -> {
            if (surname == null || surname.isBlank()) {
                return criteriaBuilder.conjunction();
            } else {
                Join<PaymentCardEntity, UserEntity> userJoin = root.join("user");
                return criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("surname")), "%" + surname.toLowerCase() + "%");
            }
        };
    }
}
