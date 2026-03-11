package by.dzarembo.trainee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TraineeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TraineeApplication.class, args);
    }

}
