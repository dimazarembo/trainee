package by.dzarembo.trainee.service;

import by.dzarembo.trainee.entity.UserEntity;
import by.dzarembo.trainee.repository.UserRepository;
import by.dzarembo.trainee.specification.UserSpecification;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    public UserEntity create(UserEntity userEntity) {
        userRepository.save(userEntity);
        return userEntity;
    }

    public UserEntity getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    public Page<UserEntity> getAll(String name, String surname, Pageable pageable) {
        Specification<UserEntity> specification = UserSpecification.hasName(name).and(UserSpecification.hasSurname(surname));
        return userRepository.findAll(specification, pageable);
    }

    @Transactional
    public UserEntity update(Long id, UserEntity userEntity) {
        UserEntity existingUser = getById(id);
        existingUser.setName(userEntity.getName());
        existingUser.setSurname(userEntity.getSurname());
        existingUser.setEmail(userEntity.getEmail());
        existingUser.setBirthday(userEntity.getBirthday());
        return userRepository.save(existingUser);
    }

    @Transactional
    public UserEntity activate(Long id) {
        UserEntity existingUser = getById(id);
        existingUser.setActive(true);
        return userRepository.save(existingUser);
    }

    @Transactional
    public UserEntity deactivate(Long id) {
        UserEntity existingUser = getById(id);
        existingUser.setActive(false);
        return userRepository.save(existingUser);
    }
}
