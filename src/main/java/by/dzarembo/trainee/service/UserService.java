package by.dzarembo.trainee.service;

import by.dzarembo.trainee.cache.CacheNames;
import by.dzarembo.trainee.cache.UserWithCardsCacheEvictor;
import by.dzarembo.trainee.dto.UserWithCardsResponse;
import by.dzarembo.trainee.entity.PaymentCardEntity;
import by.dzarembo.trainee.entity.UserEntity;
import by.dzarembo.trainee.exception.UserNotFoundException;
import by.dzarembo.trainee.mapper.UserWithCardsMapper;
import by.dzarembo.trainee.repository.PaymentCardRepository;
import by.dzarembo.trainee.repository.UserRepository;
import by.dzarembo.trainee.specification.UserSpecification;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PaymentCardRepository paymentCardRepository;
    private final UserWithCardsMapper userWithCardsMapper;
    private final UserWithCardsCacheEvictor userWithCardsCacheEvictor;

    @Transactional
    public UserEntity create(UserEntity userEntity) {
        userRepository.save(userEntity);
        return userEntity;
    }

    @Transactional(readOnly = true)
    public UserEntity getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
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
        var savedUser = userRepository.save(existingUser);

        userWithCardsCacheEvictor.evictAfterCommit(id);

        return savedUser;
    }

    @Transactional
    public UserEntity activate(Long id) {
        UserEntity existingUser = getById(id);
        existingUser.setActive(true);
        var savedUser = userRepository.save(existingUser);

        userWithCardsCacheEvictor.evictAfterCommit(id);

        return savedUser;
    }

    @Transactional
    public UserEntity deactivate(Long id) {
        return deactivateUserWithCards(id);
    }

    @Transactional
    public void delete(Long id) {
        deactivateUserWithCards(id);
    }

    @Cacheable(value = CacheNames.USERS_WITH_CARDS, key = "#userId")
    @Transactional(readOnly = true)
    public UserWithCardsResponse getUserWithCards(Long userId) {
        log.info("Loading user with cards from DB for userId={}", userId);
        UserEntity userEntity = getById(userId);
        List<PaymentCardEntity> cards = paymentCardRepository.findAllByUserId(userId);

        UserWithCardsResponse response = userWithCardsMapper.toResponse(userEntity);
        response.setCards(cards.stream().map(userWithCardsMapper::toCardInfoResponse).toList());
        return response;
    }

    private UserEntity deactivateUserWithCards(Long userId) {
        UserEntity user = getById(userId);

        List<PaymentCardEntity> cards = paymentCardRepository.findAllByUserId(userId);
        for (PaymentCardEntity paymentCardEntity : cards) {
            paymentCardEntity.setActive(false);
        }
        user.setActive(false);
        var savedUser = userRepository.save(user);

        userWithCardsCacheEvictor.evictAfterCommit(userId);

        return savedUser;
    }
}
