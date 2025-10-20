import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.user.User;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemRequestMapperTest {
    private final ItemRequestMapper mapper = Mappers.getMapper(ItemRequestMapper.class);

    @Test
    void toItemRequest_ShouldIgnoreIdAndCreated() {
        ItemRequestDto dto = new ItemRequestDto(null, null, null, null);
        dto.setDescription("Test request");
        User requestor = new User();

        ItemRequest result = mapper.toItemRequest(dto, requestor);

        assertThat(result.getId()).isNull();
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getRequestor()).isEqualTo(requestor);
        assertThat(result.getDescription()).isEqualTo("Test request");
    }
}
