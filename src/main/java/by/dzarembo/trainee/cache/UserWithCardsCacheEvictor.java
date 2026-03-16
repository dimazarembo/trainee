package by.dzarembo.trainee.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class UserWithCardsCacheEvictor {
    private final CacheManager cacheManager;

    public void evictAfterCommit(Long userId) {
        if (userId == null) {
            return;
        }

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    evictNow(userId);
                }
            });
            return;
        }

        evictNow(userId);
    }

    private void evictNow(Long userId) {
        Cache cache = cacheManager.getCache(CacheNames.USERS_WITH_CARDS);
        if (cache != null) {
            cache.evict(userId);
        }
    }
}
