package by.dzarembo.trainee.integration;

import by.dzarembo.trainee.dto.PaymentCardCreateRequest;
import by.dzarembo.trainee.dto.PaymentCardUpdateRequest;
import by.dzarembo.trainee.entity.PaymentCardEntity;
import by.dzarembo.trainee.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentCardControllerIT extends AbstractIntegrationTest {
    @Test
    void createPaymentCard_shouldReturnCreatedCard() throws Exception {
        UserEntity user = new UserEntity();
        user.setName("Ivan");
        user.setSurname("Ivanov");
        user.setBirthday(LocalDate.of(1995, 5, 10));
        user.setEmail("ivanov@test.com");
        user.setActive(true);

        UserEntity savedUser = userRepository.save(user);

        PaymentCardCreateRequest request = PaymentCardCreateRequest.builder()
                .userId(savedUser.getId())
                .cardNumber("1111222233334444")
                .holderName("Ivan Ivanov")
                .expirationDate(LocalDate.of(2030, 1, 1))
                .build();

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.userId").value(savedUser.getId()))
                .andExpect(jsonPath("$.cardNumber").value("1111222233334444"))
                .andExpect(jsonPath("$.holderName").value("Ivan Ivanov"))
                .andExpect(jsonPath("$.expirationDate").value("2030-01-01"));

        assertThat(paymentCardRepository.findAll()).hasSize(1);
    }

    @Test
    void createPaymentCard_shouldReturnBadRequest_whenUserAlreadyHasFiveCards() throws Exception {
        UserEntity user = new UserEntity();
        user.setName("Ivan");
        user.setSurname("Ivanov");
        user.setBirthday(LocalDate.of(1995, 5, 10));
        user.setEmail("ivanov@test.com");
        user.setActive(true);

        UserEntity savedUser = userRepository.save(user);

        for (int i = 0; i < 5; i++) {
            PaymentCardEntity card = new PaymentCardEntity();
            card.setUser(savedUser);
            card.setCardNumber("111122223333444" + i);
            card.setHolderName("Ivan Ivanov");
            card.setExpirationDate(LocalDate.of(2030 + i, 1, 1));
            card.setActive(true);
            paymentCardRepository.save(card);
        }

        PaymentCardCreateRequest request = PaymentCardCreateRequest.builder()
                .userId(savedUser.getId())
                .cardNumber("9999000011112222")
                .holderName("Ivan Ivanov")
                .expirationDate(LocalDate.of(2035, 1, 1))
                .build();

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("User with id " + savedUser.getId() + " already has 5 cards"));

        assertThat(paymentCardRepository.findAllByUserId(savedUser.getId())).hasSize(5);
    }

    @Test
    void createPaymentCard_shouldAllowNewCard_whenUserHasFourActiveAndOneInactiveCards() throws Exception {
        UserEntity user = new UserEntity();
        user.setName("Ivan");
        user.setSurname("Ivanov");
        user.setBirthday(LocalDate.of(1995, 5, 10));
        user.setEmail("ivanov@test.com");
        user.setActive(true);

        UserEntity savedUser = userRepository.save(user);

        for (int i = 0; i < 5; i++) {
            PaymentCardEntity card = new PaymentCardEntity();
            card.setUser(savedUser);
            card.setCardNumber("111122223333444" + i);
            card.setHolderName("Ivan Ivanov");
            card.setExpirationDate(LocalDate.of(2030 + i, 1, 1));
            card.setActive(i != 4);
            paymentCardRepository.save(card);
        }

        PaymentCardCreateRequest request = PaymentCardCreateRequest.builder()
                .userId(savedUser.getId())
                .cardNumber("9999000011112222")
                .holderName("Ivan Ivanov")
                .expirationDate(LocalDate.of(2035, 1, 1))
                .build();

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(savedUser.getId()))
                .andExpect(jsonPath("$.cardNumber").value("9999000011112222"));

        assertThat(paymentCardRepository.findAllByUserId(savedUser.getId())).hasSize(6);
    }

    @Test
    void getPaymentCard_shouldReturnCard() throws Exception {
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

        mockMvc.perform(get("/cards/{id}", savedCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCard.getId()))
                .andExpect(jsonPath("$.userId").value(savedUser.getId()))
                .andExpect(jsonPath("$.cardNumber").value("1111222233334444"))
                .andExpect(jsonPath("$.holderName").value("Ivan Ivanov"))
                .andExpect(jsonPath("$.expirationDate").value("2030-01-01"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getPaymentCard_shouldReturn404_whenCardNotFound() throws Exception {
        mockMvc.perform(get("/cards/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCards_shouldReturnPage() throws Exception {
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

        mockMvc.perform(get("/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").isNumber());
    }

    @Test
    void getCards_shouldFilterByExactUserNameAndSurnameIgnoringCase() throws Exception {
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

        UserEntity savedUser1 = userRepository.save(user1);
        UserEntity savedUser2 = userRepository.save(user2);

        PaymentCardEntity card1 = new PaymentCardEntity();
        card1.setUser(savedUser1);
        card1.setCardNumber("1111222233334444");
        card1.setHolderName("Ivan Ivanov");
        card1.setExpirationDate(LocalDate.of(2030, 1, 1));
        card1.setActive(true);

        PaymentCardEntity card2 = new PaymentCardEntity();
        card2.setUser(savedUser2);
        card2.setCardNumber("5555666677778888");
        card2.setHolderName("Ivanii Ivanovich");
        card2.setExpirationDate(LocalDate.of(2031, 2, 2));
        card2.setActive(true);

        paymentCardRepository.save(card1);
        paymentCardRepository.save(card2);

        mockMvc.perform(get("/cards")
                        .param("name", "ivan")
                        .param("surname", "ivanov")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].userId").value(savedUser1.getId()))
                .andExpect(jsonPath("$.content[0].holderName").value("Ivan Ivanov"));
    }


    @Test
    void updatePaymentCard_shouldReturnUpdatedCard() throws Exception {
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

        PaymentCardUpdateRequest request = PaymentCardUpdateRequest.builder()
                .cardNumber("9999000011112222")
                .holderName("Petr Petrov")
                .expirationDate(LocalDate.of(2032, 12, 31))
                .build();

        mockMvc.perform(put("/cards/{id}", savedCard.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCard.getId()))
                .andExpect(jsonPath("$.userId").value(savedUser.getId()))
                .andExpect(jsonPath("$.cardNumber").value("9999000011112222"))
                .andExpect(jsonPath("$.holderName").value("Petr Petrov"))
                .andExpect(jsonPath("$.expirationDate").value("2032-12-31"))
                .andExpect(jsonPath("$.active").value(true));

        PaymentCardEntity updatedCard = paymentCardRepository.findById(savedCard.getId()).orElseThrow();

        assertThat(updatedCard.getCardNumber()).isEqualTo("9999000011112222");
        assertThat(updatedCard.getHolderName()).isEqualTo("Petr Petrov");
        assertThat(updatedCard.getExpirationDate()).isEqualTo(LocalDate.of(2032, 12, 31));
    }

    @Test
    void activatePaymentCard_shouldReturnActivatedCard() throws Exception {
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
        card.setActive(false);

        PaymentCardEntity savedCard = paymentCardRepository.save(card);

        mockMvc.perform(patch("/cards/{id}/activate", savedCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCard.getId()))
                .andExpect(jsonPath("$.active").value(true));

        PaymentCardEntity activatedCard = paymentCardRepository.findById(savedCard.getId()).orElseThrow();
        assertThat(activatedCard.isActive()).isTrue();
    }

    @Test
    void deactivatePaymentCard_shouldReturnDeactivatedCard() throws Exception {
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

        mockMvc.perform(patch("/cards/{id}/deactivate", savedCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCard.getId()))
                .andExpect(jsonPath("$.active").value(false));

        PaymentCardEntity deactivatedCard = paymentCardRepository.findById(savedCard.getId()).orElseThrow();
        assertThat(deactivatedCard.isActive()).isFalse();
    }

    @Test
    void deletePaymentCard_shouldReturnNoContent() throws Exception {
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

        mockMvc.perform(delete("/cards/{id}", savedCard.getId()))
                .andExpect(status().isNoContent());

        PaymentCardEntity deletedCard = paymentCardRepository.findById(savedCard.getId()).orElseThrow();
        assertThat(deletedCard.isActive()).isFalse();
    }
}
