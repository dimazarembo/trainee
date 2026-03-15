package by.dzarembo.trainee;

import by.dzarembo.trainee.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TraineeApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

}
