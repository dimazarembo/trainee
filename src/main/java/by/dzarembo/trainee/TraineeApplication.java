package by.dzarembo.trainee;

import by.dzarembo.trainee.security.AuthServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)@EnableJpaAuditing
@EnableCaching
@EnableConfigurationProperties(AuthServiceProperties.class)
public class TraineeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TraineeApplication.class, args);
    }

}
