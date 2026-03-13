package by.dzarembo.trainee.mapper;

import by.dzarembo.trainee.dto.UserCardInfoResponse;
import by.dzarembo.trainee.dto.UserWithCardsResponse;
import by.dzarembo.trainee.entity.PaymentCardEntity;
import by.dzarembo.trainee.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserWithCardsMapper {
    @Mapping(target = "cards", ignore = true)
    UserWithCardsResponse toResponse(UserEntity userEntity);


    UserCardInfoResponse toCardInfoResponse (PaymentCardEntity paymentCardEntity);
}
