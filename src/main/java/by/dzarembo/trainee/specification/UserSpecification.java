package by.dzarembo.trainee.specification;

import by.dzarembo.trainee.entity.UserEntity;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
    public static Specification<UserEntity> hasName(String name) {
        return (root, query, criteriaBuilder) ->
        {
            if (name == null || name.isBlank()) {
                return criteriaBuilder.conjunction();
            } else {
                return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
            }
        };

    }

    public static Specification<UserEntity> hasSurname(String surname) {
        return (root, query, criteriaBuilder) ->
        {
            if (surname == null || surname.isBlank()) {
                return criteriaBuilder.conjunction();
            } else {
                return criteriaBuilder.like(criteriaBuilder.lower(root.get("surname")), "%" + surname.toLowerCase() + "%");
            }
        };
    }
}
