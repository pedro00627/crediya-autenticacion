package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.request.UserRequestRecord;
import co.com.pragma.api.dto.response.UserResponseRecord;
import co.com.pragma.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserDTOMapper {
    UserResponseRecord toResponse(User user);

    // El 'id' es generado por el sistema, no se provee en la petición, por lo que se ignora.
    @Mapping(target = "id", ignore = true)
    User toModel(UserRequestRecord request);
}
