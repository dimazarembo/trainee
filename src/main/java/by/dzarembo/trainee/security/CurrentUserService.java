package by.dzarembo.trainee.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            return null;
        }

        return authenticatedUser;
    }

    public boolean isAdmin() {
        AuthenticatedUser currentUser = getCurrentUser();
        return currentUser != null && "ADMIN".equals(currentUser.role());
    }

    public Long getCurrentUserId() {
        AuthenticatedUser currentUser = getCurrentUser();
        return currentUser != null ? currentUser.userId() : null;
    }
}
