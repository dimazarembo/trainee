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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationCheckerTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @InjectMocks
    private AuthorizationChecker authorizationChecker;

    @Test
    void isCurrentUser_shouldReturnTrue_whenCurrentUserIsAdmin() {
        Authentication authentication = authentication(new AuthenticatedUser(99L, SecurityRoles.ADMIN));

        boolean result = authorizationChecker.isCurrentUser(authentication, 1L);

        assertThat(result).isTrue();
        verifyNoInteractions(paymentCardRepository);
    }

    @Test
    void isCurrentUser_shouldReturnTrue_whenCurrentUserMatchesTargetUser() {
        Authentication authentication = authentication(new AuthenticatedUser(15L, SecurityRoles.USER));

        boolean result = authorizationChecker.isCurrentUser(authentication, 15L);

        assertThat(result).isTrue();
        verifyNoInteractions(paymentCardRepository);
    }

    @Test
    void isCurrentUser_shouldReturnFalse_whenCurrentUserDoesNotMatchTargetUser() {
        Authentication authentication = authentication(new AuthenticatedUser(15L, SecurityRoles.USER));

        boolean result = authorizationChecker.isCurrentUser(authentication, 22L);

        assertThat(result).isFalse();
        verifyNoInteractions(paymentCardRepository);
    }

    @Test
    void isCurrentUser_shouldReturnFalse_whenAuthenticationMissing() {
        boolean result = authorizationChecker.isCurrentUser(null, 1L);

        assertThat(result).isFalse();
        verifyNoInteractions(paymentCardRepository);
    }

    @Test
    void isCurrentUser_shouldReturnFalse_whenPrincipalIsNotAuthenticatedUser() {
        Authentication authentication = authentication("anonymous");

        boolean result = authorizationChecker.isCurrentUser(authentication, 1L);

        assertThat(result).isFalse();
        verifyNoInteractions(paymentCardRepository);
    }

    @Test
    void canAccessCard_shouldReturnTrue_whenCurrentUserIsAdmin() {
        Authentication authentication = authentication(new AuthenticatedUser(99L, SecurityRoles.ADMIN));

        boolean result = authorizationChecker.canAccessCard(authentication, 10L);

        assertThat(result).isTrue();
        verifyNoInteractions(paymentCardRepository);
    }

    @Test
    void canAccessCard_shouldReturnFalse_whenAuthenticationMissing() {
        boolean result = authorizationChecker.canAccessCard(null, 10L);

        assertThat(result).isFalse();
        verifyNoInteractions(paymentCardRepository);
    }

    @Test
    void canAccessCard_shouldReturnFalse_whenPrincipalIsNotAuthenticatedUser() {
        Authentication authentication = authentication("anonymous");

        boolean result = authorizationChecker.canAccessCard(authentication, 10L);

        assertThat(result).isFalse();
        verifyNoInteractions(paymentCardRepository);
    }

    @Test
    void canAccessCard_shouldReturnTrue_whenCurrentUserOwnsCard() {
        PaymentCardEntity card = buildCard(10L, 15L);
        Authentication authentication = authentication(new AuthenticatedUser(15L, SecurityRoles.USER));
        when(paymentCardRepository.findById(10L)).thenReturn(Optional.of(card));

        boolean result = authorizationChecker.canAccessCard(authentication, 10L);

        assertThat(result).isTrue();
        verify(paymentCardRepository).findById(10L);
    }

    @Test
    void canAccessCard_shouldReturnFalse_whenCurrentUserDoesNotOwnCard() {
        PaymentCardEntity card = buildCard(10L, 22L);
        Authentication authentication = authentication(new AuthenticatedUser(15L, SecurityRoles.USER));
        when(paymentCardRepository.findById(10L)).thenReturn(Optional.of(card));

        boolean result = authorizationChecker.canAccessCard(authentication, 10L);

        assertThat(result).isFalse();
        verify(paymentCardRepository).findById(10L);
    }

    @Test
    void canAccessCard_shouldThrowPaymentCardNotFoundException_whenCardDoesNotExist() {
        Authentication authentication = authentication(new AuthenticatedUser(15L, SecurityRoles.USER));
        when(paymentCardRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorizationChecker.canAccessCard(authentication, 10L))
                .isInstanceOf(PaymentCardNotFoundException.class)
                .hasMessage("Payment card with id 10 not found");
    }

    private Authentication authentication(Object principal) {
        return new UsernamePasswordAuthenticationToken(principal, null, List.of());
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
