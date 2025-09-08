package co.com.pragma.r2dbc.mapper;

import co.com.pragma.model.user.User;
import co.com.pragma.r2dbc.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserDataMapper {
    User toDomain(UserEntity userEntity);

    UserEntity toEntity(User user);
}