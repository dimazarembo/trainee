package by.dzarembo.trainee.service;

import by.dzarembo.trainee.cache.UserWithCardsCacheEvictor;
import by.dzarembo.trainee.dto.UserCardInfoResponse;
import by.dzarembo.trainee.dto.UserWithCardsResponse;
import by.dzarembo.trainee.entity.PaymentCardEntity;
import by.dzarembo.trainee.entity.UserEntity;
import by.dzarembo.trainee.exception.UserNotFoundException;
import by.dzarembo.trainee.mapper.UserWithCardsMapper;
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
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PaymentCardRepository paymentCardRepository;
    @Mock
    UserWithCardsMapper userWithCardsMapper;
    @Mock
    UserWithCardsCacheEvictor userWithCardsCacheEvictor;
    @InjectMocks
    UserService userService;


    @Test
    void create_shouldReturnUser_whenUserCorrect() {
        Long userId = 1L;
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        when(userRepository.save(userEntity)).thenReturn(userEntity);

        UserEntity result = userService.create(userEntity);

        assertThat(result).isEqualTo(userEntity);
        verify(userRepository).save(userEntity);
    }

    @Test
    void getById_shouldReturnUser_whenUserExists() {
        Long userId = 1L;
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        UserEntity result = userService.getById(userId);

        assertThat(result).isEqualTo(userEntity);
        verify(userRepository).findById(userId);
    }

    @Test
    void getById_shouldThrowException_whenUserDoesNotExist() {
        Long userId = 1L;
        assertThatThrownBy(() -> userService.getById(userId)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getAll_shouldReturnAllUsers_whenUserExists() {
        UserEntity userEntity1 = new UserEntity();
        UserEntity userEntity2 = new UserEntity();
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> expectedPage = new PageImpl<>(List.of(userEntity1, userEntity2));

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        Page<UserEntity> result = userService.getAll("name", "surname", pageable);

        assertThat(result).isEqualTo(expectedPage);
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void update_shouldUpdateFieldsAndSaveUser(){
        Long userId = 1L;

        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);
        existingUser.setName("name_old");
        existingUser.setSurname("surname_old");
        existingUser.setEmail("email_old@test.com");
        existingUser.setBirthday(LocalDate.of(1980, 1, 1));

        UserEntity updatedUser = new UserEntity();
        updatedUser.setName("name_new");
        updatedUser.setSurname("surname_new");
        updatedUser.setEmail("email_new@test.com");
        updatedUser.setBirthday(LocalDate.of(1990, 2, 2));

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        UserEntity result = userService.update(userId, updatedUser);

        assertThat(result).isEqualTo(existingUser);
        assertThat(result.getName()).isEqualTo("name_new");
        assertThat(result.getSurname()).isEqualTo("surname_new");
        assertThat(result.getEmail()).isEqualTo("email_new@test.com");
        assertThat(result.getBirthday()).isEqualTo(LocalDate.of(1990, 2, 2));

        verify(userRepository).findById(userId);
        verify(userRepository).save(existingUser);
    }

    @Test
    void activate_shouldActivateUser_whenUserExists() {
        Long userId = 1L;
        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);
        existingUser.setActive(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        UserEntity result = userService.activate(userId);

        assertThat(result).isEqualTo(existingUser);
        assertThat(result.isActive()).isTrue();

        verify(userRepository).findById(userId);
        verify(userRepository).save(existingUser);
    }

    @Test
    void deactivate_shouldDeactivateUser_whenUserExists() {
        Long userId = 1L;
        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);
        existingUser.setActive(true);
        PaymentCardEntity firstCard = new PaymentCardEntity();
        firstCard.setActive(true);
        PaymentCardEntity secondCard = new PaymentCardEntity();
        secondCard.setActive(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(paymentCardRepository.findAllByUserId(userId)).thenReturn(List.of(firstCard, secondCard));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        UserEntity result = userService.deactivate(userId);

        assertThat(result).isEqualTo(existingUser);
        assertThat(result.isActive()).isFalse();
        assertThat(firstCard.isActive()).isFalse();
        assertThat(secondCard.isActive()).isFalse();

        verify(userRepository).findById(userId);
        verify(paymentCardRepository).findAllByUserId(userId);
        verify(userRepository).save(existingUser);
    }

    @Test
    void delete_shouldSoftDeleteUserAndCards_whenUserExists() {
        Long userId = 1L;
        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);
        existingUser.setActive(true);
        PaymentCardEntity firstCard = new PaymentCardEntity();
        firstCard.setActive(true);
        PaymentCardEntity secondCard = new PaymentCardEntity();
        secondCard.setActive(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(paymentCardRepository.findAllByUserId(userId)).thenReturn(List.of(firstCard, secondCard));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        userService.delete(userId);

        verify(userRepository).findById(userId);
        verify(paymentCardRepository).findAllByUserId(userId);
        verify(userRepository).save(existingUser);
        assertThat(existingUser.isActive()).isFalse();
        assertThat(firstCard.isActive()).isFalse();
        assertThat(secondCard.isActive()).isFalse();
    }

    @Test
    void getUserWithCards_shouldBuildResponse_whenUserExist() {
        Long userId = 1L;
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        PaymentCardEntity card1 = new PaymentCardEntity();
        PaymentCardEntity card2 = new PaymentCardEntity();

        UserWithCardsResponse userWithCardsResponse = new UserWithCardsResponse();

        UserCardInfoResponse cardResponse1 = new UserCardInfoResponse();
        UserCardInfoResponse cardResponse2 = new UserCardInfoResponse();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(paymentCardRepository.findAllByUserId(userId)).thenReturn(List.of(card1, card2));
        when(userWithCardsMapper.toResponse(userEntity)).thenReturn(userWithCardsResponse);
        when(userWithCardsMapper.toCardInfoResponse(card1)).thenReturn(cardResponse1);
        when(userWithCardsMapper.toCardInfoResponse(card2)).thenReturn(cardResponse2);

        UserWithCardsResponse result = userService.getUserWithCards(userId);

        assertThat(result).isEqualTo(userWithCardsResponse);
        assertThat(result.getCards().size()).isEqualTo(2);

        verify(userRepository).findById(userId);
        verify(paymentCardRepository).findAllByUserId(userId);
        verify(userWithCardsMapper).toResponse(userEntity);
        verify(userWithCardsMapper).toCardInfoResponse(card1);
        verify(userWithCardsMapper).toCardInfoResponse(card2);
    }
}
