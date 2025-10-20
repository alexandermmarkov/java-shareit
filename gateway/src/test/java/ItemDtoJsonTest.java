import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.ContextConfiguration;
import shareit.ShareItGateway;
import shareit.item.dto.ItemDto;
import shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = ShareItGateway.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemDtoJsonTest {
    private final JacksonTester<ItemDto> json;

    @Test
    void testItemDtoSerialization() throws Exception {
        UserDto owner = new UserDto(1L, "John Doe", "john@example.com");

        ItemDto itemDto = new ItemDto(
                100L,
                "Power Drill",
                "Powerful cordless drill with hammer function",
                owner,
                true,
                50L);

        JsonContent<ItemDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(100);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Power Drill");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Powerful cordless drill with hammer function");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(50);

        assertThat(result).extractingJsonPathNumberValue("$.owner.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.owner.name").isEqualTo("John Doe");
        assertThat(result).extractingJsonPathStringValue("$.owner.email").isEqualTo("john@example.com");
    }

    @Test
    void testItemDtoDeserialization() throws Exception {
        String content = "{" +
                "\"id\": 200," +
                "\"name\": \"Saw\"," +
                "\"description\": \"Circular saw\"," +
                "\"owner\": {" +
                "    \"id\": 2," +
                "    \"name\": \"Jane Smith\"," +
                "    \"email\": \"jane@example.com\"" +
                "}," +
                "\"available\": false," +
                "\"requestId\": 1" +
                "}";

        ItemDto itemDto = json.parseObject(content);

        assertThat(itemDto.getId()).isEqualTo(200L);
        assertThat(itemDto.getName()).isEqualTo("Saw");
        assertThat(itemDto.getDescription()).isEqualTo("Circular saw");
        assertThat(itemDto.getAvailable()).isEqualTo(false);
        assertThat(itemDto.getRequestId()).isEqualTo(1L);

        assertThat(itemDto.getOwner()).isNotNull();
        assertThat(itemDto.getOwner().getId()).isEqualTo(2L);
        assertThat(itemDto.getOwner().getName()).isEqualTo("Jane Smith");
        assertThat(itemDto.getOwner().getEmail()).isEqualTo("jane@example.com");
    }
}
