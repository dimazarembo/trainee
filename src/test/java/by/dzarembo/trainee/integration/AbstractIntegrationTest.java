package by.dzarembo.trainee.integration;

import by.dzarembo.trainee.cache.CacheNames;
import by.dzarembo.trainee.repository.PaymentCardRepository;
import by.dzarembo.trainee.repository.UserRepository;
import by.dzarembo.trainee.security.SecurityRoles;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import by.dzarembo.trainee.security.AuthenticatedUser;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractIntegrationTest {
    @Container
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Container
    static final GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PaymentCardRepository paymentCardRepository;

    @Autowired
    protected CacheManager cacheManager;


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @BeforeEach
    void setUpTestState() {
        authenticateAsAdmin();
        clearCache();
        clearRepositories();
    }

    private void authenticateAsAdmin() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new AuthenticatedUser(1L, SecurityRoles.ADMIN),
                        null,
                        List.of(new SimpleGrantedAuthority(SecurityRoles.ROLE_ADMIN))
                )
        );
    }

    private void clearCache() {
        Cache cache = cacheManager.getCache(CacheNames.USERS_WITH_CARDS);
        if (cache != null) {
            cache.clear();
        }
    }

    private void clearRepositories() {
        paymentCardRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
