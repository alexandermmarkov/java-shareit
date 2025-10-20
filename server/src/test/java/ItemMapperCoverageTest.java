import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ShareItServer.class)
public class ItemMapperCoverageTest {
    @Autowired
    private ItemMapper itemMapper;

    @Test
    void toItemDtoShouldHandleNullRequest() {
        Item item = new Item();
        item.setId(1L);
        item.setRequest(null);

        ItemDto result = itemMapper.toItemDto(item);
        assertThat(result.getRequestId()).isNull();
    }

    @Test
    void toItemDtoShouldHandleRequestWithId() {
        Item item = new Item();
        item.setId(1L);

        ItemRequest request = new ItemRequest();
        request.setId(100L);
        item.setRequest(request);

        ItemDto result = itemMapper.toItemDto(item);
        assertThat(result.getRequestId()).isEqualTo(100L);
    }

    @Test
    void toItemWithDateDtoShouldHandleAllNullParameters() {
        Item item = new Item();
        item.setId(1L);

        ItemWithDateDto result = itemMapper.toItemWithDateDto(item, null, null, null);

        assertThat(result.getComments()).isNull();
        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
    }

    @Test
    void toItemWithDateDtoShouldHandleAllNonNullParameters() {
        Item item = new Item();
        item.setId(1L);

        List<CommentDto> comments = List.of(new CommentDto(null, null, null, null));
        LocalDateTime lastBooking = LocalDateTime.now().minusDays(1);
        LocalDateTime nextBooking = LocalDateTime.now().plusDays(1);

        ItemWithDateDto result = itemMapper.toItemWithDateDto(item, comments, lastBooking, nextBooking);

        assertThat(result.getComments()).isEqualTo(comments);
        assertThat(result.getLastBooking()).isEqualTo(lastBooking);
        assertThat(result.getNextBooking()).isEqualTo(nextBooking);
    }

    @Test
    void toItemShouldHandleNullOwnerAndRequest() {
        ItemDto itemDto = new ItemDto(null, null, null, null, null, null);
        itemDto.setId(1L);
        itemDto.setName("Test");
        itemDto.setAvailable(true);

        Item result = itemMapper.toItem(itemDto, null, null);

        assertThat(result.getOwner()).isNull();
        assertThat(result.getRequest()).isNull();
        assertThat(result.getName()).isEqualTo("Test");
    }

    @Test
    void toItemShouldHandleNonNullOwnerAndRequest() {
        ItemDto itemDto = new ItemDto(null, null, null, null, null, null);
        itemDto.setId(1L);
        itemDto.setName("Test");

        User owner = new User();
        owner.setId(1L);

        ItemRequest request = new ItemRequest();
        request.setId(100L);

        Item result = itemMapper.toItem(itemDto, owner, request);

        assertThat(result.getOwner()).isEqualTo(owner);
        assertThat(result.getRequest()).isEqualTo(request);
    }
}
