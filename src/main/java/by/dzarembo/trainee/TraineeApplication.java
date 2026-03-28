package by.dzarembo.trainee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableJpaAuditing
@EnableCaching
public class TraineeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TraineeApplication.class, args);
    }

}
