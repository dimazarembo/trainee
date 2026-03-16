package by.dzarembo.trainee.service;

import by.dzarembo.trainee.cache.UserWithCardsCacheEvictor;
import by.dzarembo.trainee.entity.PaymentCardEntity;
import by.dzarembo.trainee.entity.UserEntity;
import by.dzarembo.trainee.exception.PaymentCardNotFoundException;
import by.dzarembo.trainee.repository.PaymentCardRepository;
import by.dzarembo.trainee.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentCardServiceTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserWithCardsCacheEvictor userWithCardsCacheEvictor;

    @InjectMocks
    private PaymentCardService paymentCardService;

    @Test
    public void create_shouldReturnCard_whenCardCorrect() {
        Long userId = 1L;
        UserEntity user = buildUser(userId);
        PaymentCardEntity card = buildCard(10L, null);

        when(paymentCardRepository.countByUserId(userId)).thenReturn(0L);
        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(user));
        when(paymentCardRepository.saveAndFlush(card)).thenReturn(card);

        PaymentCardEntity result = paymentCardService.create(userId, card);

        assertThat(result).isEqualTo(card);
        assertThat(card.getUser()).isEqualTo(user);
        assertThat(user.getCards()).contains(card);

        verify(userRepository).findByIdForUpdate(userId);
        verify(paymentCardRepository).countByUserId(userId);
        verify(paymentCardRepository).saveAndFlush(card);
        verify(userWithCardsCacheEvictor).evictAfterCommit(userId);
    }

    @Test
    public void getById_shouldReturnCard_whenCardExists() {
        Long cardId = 1L;
        PaymentCardEntity card = buildCard(cardId, null);

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(card));

        PaymentCardEntity result = paymentCardService.getById(cardId);

        assertThat(result).isEqualTo(card);
        verify(paymentCardRepository).findById(cardId);
    }

    @Test
    public void getById_shouldThrowException_whenCardDoesNotExist() {
        Long cardId = 1L;

        assertThatThrownBy(() -> paymentCardService.getById(cardId))
                .isInstanceOf(PaymentCardNotFoundException.class);
    }

    @Test
    public void getAll_shouldReturnAllCards_whenCardsExist() {
        PaymentCardEntity card1 = buildCard(1L, null);
        PaymentCardEntity card2 = buildCard(2L, null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<PaymentCardEntity> expectedPage = new PageImpl<>(List.of(card1, card2));

        when(paymentCardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        Page<PaymentCardEntity> result = paymentCardService.getAll("name", "surname", pageable);

        assertThat(result).isEqualTo(expectedPage);
        verify(paymentCardRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    public void getAllByUserId_shouldReturnCards_whenUserExists() {
        Long userId = 1L;
        UserEntity user = buildUser(userId);
        PaymentCardEntity card1 = buildCard(1L, user);
        PaymentCardEntity card2 = buildCard(2L, user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(paymentCardRepository.findAllByUserId(userId)).thenReturn(List.of(card1, card2));

        List<PaymentCardEntity> result = paymentCardService.getAllByUserId(userId);

        assertThat(result).containsExactly(card1, card2);
        verify(userRepository).findById(userId);
        verify(paymentCardRepository).findAllByUserId(userId);
    }

    @Test
    public void update_shouldUpdateFieldsAndSaveCard() {
        Long userId = 1L;
        Long cardId = 1L;
        UserEntity user = buildUser(userId);
        PaymentCardEntity existingCard = buildCard(cardId, user);
        PaymentCardEntity updatedCard = new PaymentCardEntity();
        updatedCard.setCardNumber("5555 6666 7777 8888");
        updatedCard.setHolderName("New Holder");
        updatedCard.setExpirationDate(LocalDate.of(2032, 2, 2));

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(paymentCardRepository.save(existingCard)).thenReturn(existingCard);

        PaymentCardEntity result = paymentCardService.update(cardId, updatedCard);

        assertThat(result).isEqualTo(existingCard);
        assertThat(result.getCardNumber()).isEqualTo("5555 6666 7777 8888");
        assertThat(result.getHolderName()).isEqualTo("New Holder");
        assertThat(result.getExpirationDate()).isEqualTo(LocalDate.of(2032, 2, 2));

        verify(paymentCardRepository).findById(cardId);
        verify(paymentCardRepository).save(existingCard);
        verify(userWithCardsCacheEvictor).evictAfterCommit(userId);
    }

    @Test
    public void activate_shouldActivateCard_whenCardExists() {
        Long userId = 1L;
        Long cardId = 1L;
        UserEntity user = buildUser(userId);
        PaymentCardEntity existingCard = buildCard(cardId, user);
        existingCard.setActive(false);

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(paymentCardRepository.save(existingCard)).thenReturn(existingCard);

        PaymentCardEntity result = paymentCardService.activate(cardId);

        assertThat(result).isEqualTo(existingCard);
        assertThat(result.isActive()).isTrue();

        verify(paymentCardRepository).findById(cardId);
        verify(paymentCardRepository).save(existingCard);
        verify(userWithCardsCacheEvictor).evictAfterCommit(userId);
    }

    @Test
    public void deactivate_shouldDeactivateCard_whenCardExists() {
        Long userId = 1L;
        Long cardId = 1L;
        UserEntity user = buildUser(userId);
        PaymentCardEntity existingCard = buildCard(cardId, user);
        existingCard.setActive(true);

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(paymentCardRepository.save(existingCard)).thenReturn(existingCard);

        PaymentCardEntity result = paymentCardService.deactivate(cardId);

        assertThat(result).isEqualTo(existingCard);
        assertThat(result.isActive()).isFalse();

        verify(paymentCardRepository).findById(cardId);
        verify(paymentCardRepository).save(existingCard);
        verify(userWithCardsCacheEvictor).evictAfterCommit(userId);
    }

    @Test
    public void delete_shouldSoftDeleteCard_whenCardExists() {
        Long userId = 1L;
        Long cardId = 1L;
        UserEntity user = buildUser(userId);
        PaymentCardEntity card = buildCard(cardId, user);

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(paymentCardRepository.save(card)).thenReturn(card);

        paymentCardService.delete(cardId);

        assertThat(card.isActive()).isFalse();
        assertThat(card.getUser()).isEqualTo(user);

        verify(paymentCardRepository).findById(cardId);
        verify(paymentCardRepository).save(card);
        verify(userWithCardsCacheEvictor).evictAfterCommit(userId);
    }

    private UserEntity buildUser(Long userId) {
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setName("Ivan");
        user.setSurname("Ivanov");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user.setEmail("ivan@test.com");
        user.setActive(true);
        return user;
    }

    private PaymentCardEntity buildCard(Long cardId, UserEntity user) {
        PaymentCardEntity card = new PaymentCardEntity();
        card.setId(cardId);
        card.setUser(user);
        card.setCardNumber("1111 2222 3333 4444");
        card.setHolderName("Ivan Ivanov");
        card.setExpirationDate(LocalDate.of(2030, 1, 1));
        card.setActive(true);
        return card;
    }
}
