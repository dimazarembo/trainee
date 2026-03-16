package by.dzarembo.trainee.service;

import by.dzarembo.trainee.entity.PaymentCardEntity;
import by.dzarembo.trainee.entity.UserEntity;
import by.dzarembo.trainee.exception.CardLimitExceedException;
import by.dzarembo.trainee.exception.PaymentCardNotFoundException;
import by.dzarembo.trainee.exception.UserNotFoundException;
import by.dzarembo.trainee.repository.PaymentCardRepository;
import by.dzarembo.trainee.repository.UserRepository;
import by.dzarembo.trainee.specification.PaymentCardSpecification;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PaymentCardService {

    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;
    private final CacheManager cacheManager;

    @Transactional
    public PaymentCardEntity create(Long userId, PaymentCardEntity card) {
        if (countByUserId(userId) >= 5) {
            throw new CardLimitExceedException(String.format("User with id %d already has 5 cards", userId));
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id %d not found", userId)));

        user.addPaymentCard(card);

        PaymentCardEntity savedCard = paymentCardRepository.saveAndFlush(card);

        evictUserWithCardsCache(userId);
        return savedCard;
    }

    public PaymentCardEntity getById(Long id) {
        return paymentCardRepository.findById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(String.format("Payment card with id %d not found", id)));
    }

    public Page<PaymentCardEntity> getAll(String name, String surname, Pageable pageable) {
        Specification<PaymentCardEntity> specification =
                PaymentCardSpecification.hasUserName(name).and(PaymentCardSpecification.hasUserSurname(surname));
        return paymentCardRepository.findAll(specification, pageable);
    }

    public List<PaymentCardEntity> getAllByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        return paymentCardRepository.findAllByUserId(userId);
    }

    @Transactional
    public PaymentCardEntity update(Long cardId, PaymentCardEntity card) {
        PaymentCardEntity existingPaymentCard = getById(cardId);
        existingPaymentCard.setCardNumber(card.getCardNumber());
        existingPaymentCard.setHolderName(card.getHolderName());
        existingPaymentCard.setExpirationDate(card.getExpirationDate());
        var savedCard = paymentCardRepository.save(existingPaymentCard);

        evictUserWithCardsCache(existingPaymentCard.getUser().getId());

        return savedCard;
    }

    @Transactional
    public PaymentCardEntity activate(Long cardId) {
        PaymentCardEntity existingPaymentCard = getById(cardId);
        existingPaymentCard.setActive(true);
        var savedCard = paymentCardRepository.save(existingPaymentCard);

        evictUserWithCardsCache(existingPaymentCard.getUser().getId());

        return savedCard;
    }

    @Transactional
    public PaymentCardEntity deactivate(Long cardId) {
        PaymentCardEntity existingPaymentCard = getById(cardId);
        existingPaymentCard.setActive(false);
        var savedCard = paymentCardRepository.save(existingPaymentCard);

        evictUserWithCardsCache(existingPaymentCard.getUser().getId());

        return savedCard;
    }

    private long countByUserId(Long userId) {

        return paymentCardRepository.countByUserIdJpql(userId);
    }

    @Transactional
    public void delete(Long cardId) {
        deactivate(cardId);
    }

    private void evictUserWithCardsCache(Long userId) {
        Cache cache = cacheManager.getCache("usersWithCards");
        if (cache != null) {
            cache.evict(userId);
        }
    }
}
