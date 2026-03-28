package by.dzarembo.trainee.security;

import by.dzarembo.trainee.entity.PaymentCardEntity;
import by.dzarembo.trainee.exception.PaymentCardNotFoundException;
import by.dzarembo.trainee.repository.PaymentCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("authorizationChecker")
@RequiredArgsConstructor
public class AuthorizationChecker {
    private final PaymentCardRepository paymentCardRepository;

    public boolean isCurrentUser(Authentication authentication, Long targetUserId) {
        AuthenticatedUser currentUser = extractUser(authentication);
        if (currentUser == null) {
            return false;
        }
        if (SecurityRoles.ADMIN.equals(currentUser.role())) {
            return true;
        }
        return currentUser.userId().equals(targetUserId);
    }

    public boolean canAccessCard(Authentication authentication, Long cardId) {
        AuthenticatedUser currentUser = extractUser(authentication);
        if (currentUser == null) {
            return false;
        }
        if (SecurityRoles.ADMIN.equals(currentUser.role())) {
            return true;
        }

        PaymentCardEntity card = paymentCardRepository.findById(cardId)
                .orElseThrow(() -> new PaymentCardNotFoundException(
                        String.format("Payment card with id %d not found", cardId)));

        return currentUser.userId().equals(card.getUser().getId());
    }

    private AuthenticatedUser extractUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            return null;
        }
        return authenticatedUser;
    }


}
