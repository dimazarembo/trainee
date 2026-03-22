package by.dzarembo.trainee.security;

import by.dzarembo.trainee.entity.PaymentCardEntity;
import by.dzarembo.trainee.entity.UserEntity;
import by.dzarembo.trainee.exception.PaymentCardNotFoundException;
import by.dzarembo.trainee.repository.PaymentCardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessCheckerTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @InjectMocks
    private AccessChecker accessChecker;

    @Test
    void checkUserAccess_shouldAllowAccess_whenCurrentUserIsAdmin() {
        when(currentUserService.isAdmin()).thenReturn(true);

        accessChecker.checkUserAccess(1L);

        verifyNoInteractions(paymentCardRepository);
    }

    @Test
    void checkCardAccess_shouldAllowAccess_whenCurrentUserIsAdmin() {
        when(currentUserService.isAdmin()).thenReturn(true);

        accessChecker.checkCardAccess(10L);

        verifyNoInteractions(paymentCardRepository);
    }

    @Test
    void checkUserAccess_shouldAllowAccess_whenCurrentUserMatchesTargetUser() {
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(15L);

        accessChecker.checkUserAccess(15L);

        verifyNoInteractions(paymentCardRepository);
    }

    @Test
    void checkUserAccess_shouldThrowAccessDeniedException_whenCurrentUserDoesNotMatchTargetUser() {
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(15L);

        assertThatThrownBy(() -> accessChecker.checkUserAccess(22L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Access denied");
    }

    @Test
    void checkUserAccess_shouldThrowAccessDeniedException_whenCurrentUserMissing() {
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(null);

        assertThatThrownBy(() -> accessChecker.checkUserAccess(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Access denied");

        verifyNoInteractions(paymentCardRepository);
    }

    @Test
    void checkCardAccess_shouldThrowAccessDeniedException_whenCurrentUserMissing() {
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(null);

        assertThatThrownBy(() -> accessChecker.checkCardAccess(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Access denied");

        verifyNoInteractions(paymentCardRepository);
    }

    @Test
    void checkCardAccess_shouldAllowAccess_whenCurrentUserOwnsCard() {
        PaymentCardEntity card = buildCard(10L, 15L);
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(15L);
        when(paymentCardRepository.findById(10L)).thenReturn(Optional.of(card));

        accessChecker.checkCardAccess(10L);

        verify(paymentCardRepository).findById(10L);
    }

    @Test
    void checkCardAccess_shouldThrowAccessDeniedException_whenCurrentUserDoesNotOwnCard() {
        PaymentCardEntity card = buildCard(10L, 22L);
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(15L);
        when(paymentCardRepository.findById(10L)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> accessChecker.checkCardAccess(10L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Access denied");

        verify(paymentCardRepository).findById(10L);
    }

    @Test
    void checkCardAccess_shouldThrowPaymentCardNotFoundException_whenCardDoesNotExist() {
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(15L);
        when(paymentCardRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accessChecker.checkCardAccess(10L))
                .isInstanceOf(PaymentCardNotFoundException.class)
                .hasMessage("Payment card with id 10 not found");
    }

    private PaymentCardEntity buildCard(Long cardId, Long userId) {
        UserEntity user = new UserEntity();
        user.setId(userId);

        PaymentCardEntity card = new PaymentCardEntity();
        card.setId(cardId);
        card.setUser(user);
        return card;
    }
}
