package ma.fellahia.mapper;

import ma.fellahia.domain.User;
import ma.fellahia.dto.response.UserProfileResponse;
import org.mapstruct.*;

/**
 * MapStruct mapper for User entity.
 * Simple fields only — profile-specific fields are handled in UserService.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(target = "balance",        ignore = true)
    @Mapping(target = "rib",            ignore = true)
    @Mapping(target = "barNumber",      ignore = true)
    @Mapping(target = "specialization", ignore = true)
    @Mapping(target = "region",         ignore = true)
    @Mapping(target = "rating",         ignore = true)
    @Mapping(target = "totalCases",     ignore = true)
    UserProfileResponse toProfileResponse(User user);
}
