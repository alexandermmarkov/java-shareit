import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.user.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ShareItServer.class)
public class ItemRequestMapperBranchCoverageTest {

    @Autowired
    private ItemRequestMapper itemRequestMapper;

    @Test
    void toItemRequestResponseDtoShouldHandleEmptyItemsList() {
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Test request");

        ItemRequestResponseDto result = itemRequestMapper.toItemRequestResponseDto(request, List.of());

        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void toItemRequestResponseDtoShouldHandleNullItemsList() {
        ItemRequest request = new ItemRequest();
        request.setId(1L);

        ItemRequestResponseDto result = itemRequestMapper.toItemRequestResponseDto(request, null);

        assertThat(result.getItems()).isNull();
    }

    @Test
    void toItemResponseShouldHandleNullItem() {
        ItemResponseDto result = itemRequestMapper.toItemResponse(null);

        assertThat(result).isNull();
    }

    @Test
    void toItemRequestShouldHandleNullDto() {
        User requestor = new User();

        ItemRequest result = itemRequestMapper.toItemRequest(null, requestor);

        assertThat(result).isNotNull();
    }

    @Test
    void toItemRequestDtoShouldHandleNullRequest() {
        ItemRequestDto result = itemRequestMapper.toItemRequestDto(null);

        assertThat(result).isNull();
    }
}
