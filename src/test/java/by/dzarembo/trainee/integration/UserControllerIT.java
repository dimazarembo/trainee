package by.dzarembo.trainee.integration;

import by.dzarembo.trainee.dto.UserCreateRequest;
import by.dzarembo.trainee.dto.UserUpdateRequest;
import by.dzarembo.trainee.entity.PaymentCardEntity;
import by.dzarembo.trainee.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIT extends AbstractIntegrationTest{
    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
                .name("Ivan")
                .surname("Ivanov")
                .birthday(LocalDate.of(1995, 5, 10))
                .email("ivanov@test.com")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.surname").value("Ivanov"))
                .andExpect(jsonPath("$.birthday").value("1995-05-10"))
                .andExpect(jsonPath("$.email").value("ivanov@test.com"))
                .andExpect(jsonPath("$.active").value(true));

        assertThat(userRepository.findAll()).hasSize(1);
        assertThat(userRepository.findAll().getFirst().isActive()).isTrue();
    }


    @Test
    void getUserById_shouldReturnUser() throws Exception {
        UserEntity user = new UserEntity();
        user.setName("Ivan");
        user.setSurname("Ivanov");
        user.setBirthday(LocalDate.of(1995, 5, 10));
        user.setEmail("ivanov@test.com");
        user.setActive(true);

        UserEntity savedUser = userRepository.save(user);

        mockMvc.perform(get("/users/{id}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.surname").value("Ivanov"))
                .andExpect(jsonPath("$.birthday").value("1995-05-10"))
                .andExpect(jsonPath("$.email").value("ivanov@test.com"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getUserById_shouldReturn404_whenUserNotFound() throws Exception {

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        UserEntity user = new UserEntity();
        user.setName("Ivan");
        user.setSurname("Ivanov");
        user.setBirthday(LocalDate.of(1995, 5, 10));
        user.setEmail("ivanov@test.com");
        user.setActive(true);

        UserEntity savedUser = userRepository.save(user);

        UserUpdateRequest request = UserUpdateRequest.builder()
                .name("Petr")
                .surname("Petrov")
                .birthday(LocalDate.of(1990, 1, 1))
                .email("petrov@test.com")
                .build();

        mockMvc.perform(put("/users/{id}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.name").value("Petr"))
                .andExpect(jsonPath("$.surname").value("Petrov"))
                .andExpect(jsonPath("$.birthday").value("1990-01-01"))
                .andExpect(jsonPath("$.email").value("petrov@test.com"))
                .andExpect(jsonPath("$.active").value(true));

        UserEntity updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();

        assertThat(updatedUser.getName()).isEqualTo("Petr");
        assertThat(updatedUser.getSurname()).isEqualTo("Petrov");
        assertThat(updatedUser.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(updatedUser.getEmail()).isEqualTo("petrov@test.com");
    }

    @Test
    void activateUser_shouldReturnActivatedUser() throws Exception {
        UserEntity user = new UserEntity();
        user.setName("Ivan");
        user.setSurname("Ivanov");
        user.setBirthday(LocalDate.of(1995, 5, 10));
        user.setEmail("ivanov@test.com");
        user.setActive(false);

        UserEntity savedUser = userRepository.save(user);

        mockMvc.perform(patch("/users/{id}/activate", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.active").value(true));

        UserEntity activatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(activatedUser.isActive()).isTrue();
    }

    @Test
    void deactivateUser_shouldReturnDeactivatedUser() throws Exception {
        UserEntity user = new UserEntity();
        user.setName("Ivan");
        user.setSurname("Ivanov");
        user.setBirthday(LocalDate.of(1995, 5, 10));
        user.setEmail("ivanov@test.com");
        user.setActive(true);

        UserEntity savedUser = userRepository.save(user);
        PaymentCardEntity card = new PaymentCardEntity();
        card.setUser(savedUser);
        card.setCardNumber("1111222233334444");
        card.setHolderName("Ivan Ivanov");
        card.setExpirationDate(LocalDate.of(2030, 1, 1));
        card.setActive(true);

        PaymentCardEntity savedCard = paymentCardRepository.save(card);

        mockMvc.perform(patch("/users/{id}/deactivate", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.active").value(false));

        UserEntity deactivatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        PaymentCardEntity deactivatedCard = paymentCardRepository.findById(savedCard.getId()).orElseThrow();
        assertThat(deactivatedUser.isActive()).isFalse();
        assertThat(deactivatedCard.isActive()).isFalse();
    }

    @Test
    void deleteUser_shouldReturnNoContent() throws Exception {
        UserEntity user = new UserEntity();
        user.setName("Ivan");
        user.setSurname("Ivanov");
        user.setBirthday(LocalDate.of(1995, 5, 10));
        user.setEmail("ivanov@test.com");
        user.setActive(true);

        UserEntity savedUser = userRepository.save(user);
        PaymentCardEntity card = new PaymentCardEntity();
        card.setUser(savedUser);
        card.setCardNumber("1111222233334444");
        card.setHolderName("Ivan Ivanov");
        card.setExpirationDate(LocalDate.of(2030, 1, 1));
        card.setActive(true);

        PaymentCardEntity savedCard = paymentCardRepository.save(card);

        mockMvc.perform(delete("/users/{id}", savedUser.getId()))
                .andExpect(status().isNoContent());

        UserEntity deletedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        PaymentCardEntity deletedCard = paymentCardRepository.findById(savedCard.getId()).orElseThrow();
        assertThat(deletedUser.isActive()).isFalse();
        assertThat(deletedCard.isActive()).isFalse();
    }

    @Test
    void getUsers_shouldReturnPage() throws Exception {
        UserEntity user1 = new UserEntity();
        user1.setName("Ivan");
        user1.setSurname("Ivanov");
        user1.setBirthday(LocalDate.of(1995, 5, 10));
        user1.setEmail("ivanov@test.com");
        user1.setActive(true);

        UserEntity user2 = new UserEntity();
        user2.setName("Petr");
        user2.setSurname("Petrov");
        user2.setBirthday(LocalDate.of(1990, 1, 1));
        user2.setEmail("petrov@test.com");
        user2.setActive(true);

        userRepository.save(user1);
        userRepository.save(user2);

        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").isNumber());
    }

    @Test
    void getUsers_shouldFilterByExactNameAndSurnameIgnoringCase() throws Exception {
        UserEntity user1 = new UserEntity();
        user1.setName("Ivan");
        user1.setSurname("Ivanov");
        user1.setBirthday(LocalDate.of(1995, 5, 10));
        user1.setEmail("ivanov@test.com");
        user1.setActive(true);

        UserEntity user2 = new UserEntity();
        user2.setName("Ivanii");
        user2.setSurname("Ivanovich");
        user2.setBirthday(LocalDate.of(1990, 1, 1));
        user2.setEmail("ivanovich@test.com");
        user2.setActive(true);

        userRepository.save(user1);
        userRepository.save(user2);

        mockMvc.perform(get("/users")
                        .param("name", "ivan")
                        .param("surname", "ivanov")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Ivan"))
                .andExpect(jsonPath("$.content[0].surname").value("Ivanov"));
    }

    @Test
    void getUserWithCards_shouldReturnUserWithCards() throws Exception {
        UserEntity user = new UserEntity();
        user.setName("Ivan");
        user.setSurname("Ivanov");
        user.setBirthday(LocalDate.of(1995, 5, 10));
        user.setEmail("ivanov@test.com");
        user.setActive(true);

        UserEntity savedUser = userRepository.save(user);

        PaymentCardEntity card1 = new PaymentCardEntity();
        card1.setUser(savedUser);
        card1.setCardNumber("1111222233334444");
        card1.setHolderName("Ivan Ivanov");
        card1.setExpirationDate(LocalDate.of(2030, 1, 1));
        card1.setActive(true);

        PaymentCardEntity card2 = new PaymentCardEntity();
        card2.setUser(savedUser);
        card2.setCardNumber("5555666677778888");
        card2.setHolderName("Ivan Ivanov");
        card2.setExpirationDate(LocalDate.of(2031, 2, 2));
        card2.setActive(true);

        paymentCardRepository.save(card1);
        paymentCardRepository.save(card2);

        mockMvc.perform(get("/users/{id}/with-cards", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.surname").value("Ivanov"))
                .andExpect(jsonPath("$.cards.length()").value(2))
                .andExpect(jsonPath("$.cards[0].id").isNumber())
                .andExpect(jsonPath("$.cards[0].cardNumber").exists())
                .andExpect(jsonPath("$.cards[1].holderName").value("Ivan Ivanov"));
    }

    @Test
    void getCardsByUser_shouldReturnCards() throws Exception {
        UserEntity user = new UserEntity();
        user.setName("Ivan");
        user.setSurname("Ivanov");
        user.setBirthday(LocalDate.of(1995, 5, 10));
        user.setEmail("ivanov@test.com");
        user.setActive(true);

        UserEntity savedUser = userRepository.save(user);

        PaymentCardEntity card1 = new PaymentCardEntity();
        card1.setUser(savedUser);
        card1.setCardNumber("1111222233334444");
        card1.setHolderName("Ivan Ivanov");
        card1.setExpirationDate(LocalDate.of(2030, 1, 1));
        card1.setActive(true);

        PaymentCardEntity card2 = new PaymentCardEntity();
        card2.setUser(savedUser);
        card2.setCardNumber("5555666677778888");
        card2.setHolderName("Ivan Ivanov");
        card2.setExpirationDate(LocalDate.of(2031, 2, 2));
        card2.setActive(true);

        paymentCardRepository.save(card1);
        paymentCardRepository.save(card2);

        mockMvc.perform(get("/users/{userId}/cards", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].userId").value(savedUser.getId()))
                .andExpect(jsonPath("$[0].holderName").value("Ivan Ivanov"));
    }
}
