package by.dzarembo.trainee.mapper;

import by.dzarembo.trainee.dto.UserCreateRequest;
import by.dzarembo.trainee.dto.UserResponse;
import by.dzarembo.trainee.dto.UserUpdateRequest;
import by.dzarembo.trainee.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "cards", ignore = true)
    UserEntity toEntity(UserCreateRequest userCreateRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "cards", ignore = true)
    UserEntity toEntity(UserUpdateRequest userUpdateRequest);

    UserResponse toResponse(UserEntity userEntity);
}
