import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ShareItServer.class)
public class ItemMapperTest {

    @Autowired
    private ItemMapper itemMapper;

    @Test
    void toItemDtoShouldHandleNullRequest() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setRequest(null);

        ItemDto result = itemMapper.toItemDto(item);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Item");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getRequestId()).isNull();
    }

    @Test
    void toItemDtoShouldMapRequestIdWhenRequestExists() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setAvailable(true);


        ItemDto result = itemMapper.toItemDto(item);

        assertThat(result.getRequestId()).isNull();
    }

    @Test
    void toItemWithDateDtoShouldHandleNullValues() {
        Item item = new Item();
        item.setRequest(null);

        ItemWithDateDto result = itemMapper.toItemWithDateDto(item, null, null, null);

        assertThat(result.getRequestId()).isNull();
        assertThat(result.getComments()).isNull();
        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
    }

    @Test
    void toItemShouldHandleNullRequest() {
        ItemDto itemDto = new ItemDto(1L, "Name", "Description", null, true, null);
        User owner = new User();

        Item result = itemMapper.toItem(itemDto, owner, null);

        assertThat(result.getRequest()).isNull();
    }
}
