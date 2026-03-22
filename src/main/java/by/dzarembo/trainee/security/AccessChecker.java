package by.dzarembo.trainee.security;

import by.dzarembo.trainee.entity.PaymentCardEntity;
import by.dzarembo.trainee.exception.PaymentCardNotFoundException;
import by.dzarembo.trainee.repository.PaymentCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessChecker {
    private final CurrentUserService currentUserService;
    private final PaymentCardRepository paymentCardRepository;

    public void checkUserAccess(Long targetUserId) {
        if (currentUserService.isAdmin()) {
            return;
        }

        Long currentUserId = currentUserService.getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(targetUserId)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    public void checkCardAccess(Long cardId) {
        if (currentUserService.isAdmin()) {
            return;
        }

        Long currentUserId = currentUserService.getCurrentUserId();
        if (currentUserId == null) {
            throw new AccessDeniedException("Access denied");
        }

        PaymentCardEntity card = paymentCardRepository.findById(cardId)
                .orElseThrow(() -> new PaymentCardNotFoundException(
                        String.format("Payment card with id %d not found", cardId)));

        if (!currentUserId.equals(card.getUser().getId())) {
            throw new AccessDeniedException("Access denied");
        }
    }
}
