package by.dzarembo.trainee.service;

import by.dzarembo.trainee.entity.PaymentCardEntity;
import by.dzarembo.trainee.entity.UserEntity;
import by.dzarembo.trainee.repository.PaymentCardRepository;
import by.dzarembo.trainee.repository.UserRepository;
import by.dzarembo.trainee.specification.PaymentCardSpecification;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
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

    @Transactional
    public PaymentCardEntity create(Long userId, PaymentCardEntity card) {
        if (countByUserId(userId) >= 5)
            throw new IllegalStateException(String.format("User with id %d already has 5 cards", userId));
        UserEntity user = userRepository.findById(userId).orElseThrow(EntityNotFoundException::new);
        user.addPaymentCard(card);
        userRepository.save(user);
        return card;
    }

    public PaymentCardEntity getById(Long id) {
        return paymentCardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment card not found with id: " + id));
    }

    public Page<PaymentCardEntity> getAll(String name, String surname, Pageable pageable) {
        Specification<PaymentCardEntity> specification =
                PaymentCardSpecification.hasUserName(name).and(PaymentCardSpecification.hasUserSurname(surname));
        return paymentCardRepository.findAll(specification, pageable);
    }

    public List<PaymentCardEntity> getAllByUserId(Long userId) {
        return paymentCardRepository.findAllByUserId(userId);
    }

    @Transactional
    public PaymentCardEntity update(Long cardId, PaymentCardEntity card) {
        PaymentCardEntity existingPaymentCard = getById(cardId);
        existingPaymentCard.setCardNumber(card.getCardNumber());
        existingPaymentCard.setHolderName(card.getHolderName());
        existingPaymentCard.setExpirationDate(card.getExpirationDate());
        return paymentCardRepository.save(existingPaymentCard);
    }

    @Transactional
    public PaymentCardEntity activate(Long cardId) {
        PaymentCardEntity existingPaymentCard = getById(cardId);
        existingPaymentCard.setActive(true);
        return paymentCardRepository.save(existingPaymentCard);
    }

    @Transactional
    public PaymentCardEntity deactivate(Long cardId) {
        PaymentCardEntity existingPaymentCard = getById(cardId);
        existingPaymentCard.setActive(false);
        return paymentCardRepository.save(existingPaymentCard);
    }

    private long countByUserId(Long userId) {

        return paymentCardRepository.countByUserIdJpql(userId);
    }

    @Transactional
    public void delete(Long cardId) {
        PaymentCardEntity card = getById(cardId);
        UserEntity user = card.getUser();
        user.removePaymentCard(card);
        userRepository.save(user);
    }
}
