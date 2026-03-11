package by.dzarembo.trainee.mapper;

import by.dzarembo.trainee.dto.PaymentCardCreateRequest;
import by.dzarembo.trainee.dto.PaymentCardResponse;
import by.dzarembo.trainee.dto.PaymentCardUpdateRequest;
import by.dzarembo.trainee.entity.PaymentCardEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "userId", target = "user.id")
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PaymentCardEntity toEntity(PaymentCardCreateRequest paymentCardCreateRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PaymentCardEntity toEntity(PaymentCardUpdateRequest paymentCardUpdateRequest);

    @Mapping(source = "user.id", target = "userId")
    PaymentCardResponse toResponse(PaymentCardEntity paymentCardEntity);
}
