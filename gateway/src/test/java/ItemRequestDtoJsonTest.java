import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.ContextConfiguration;
import shareit.ShareItGateway;
import shareit.request.dto.ItemRequestDto;
import shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = ShareItGateway.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestDtoJsonTest {
    private final JacksonTester<ItemRequestDto> json;

    @Test
    void testItemRequestDtoSerialization() throws Exception {
        UserDto requestor = new UserDto(1L, "John Doe", "john@example.com");
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 10, 0, 0);

        ItemRequestDto itemRequestDto = new ItemRequestDto(
                100L,
                "I need a power drill for home renovation projects",
                requestor,
                created);

        JsonContent<ItemRequestDto> result = json.write(itemRequestDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(100);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("I need a power drill for home renovation projects");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2024-01-01T10:00:00");

        assertThat(result).extractingJsonPathNumberValue("$.requestor.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.requestor.name").isEqualTo("John Doe");
        assertThat(result).extractingJsonPathStringValue("$.requestor.email").isEqualTo("john@example.com");
    }

    @Test
    void testItemRequestDtoDeserialization() throws Exception {
        String content = "{" +
                "\"id\": 200," +
                "\"description\": \"Need a ladder for painting work\"," +
                "\"requestor\": {" +
                "    \"id\": 2," +
                "    \"name\": \"Jane Smith\"," +
                "    \"email\": \"jane@example.com\"" +
                "}," +
                "\"created\": \"2024-01-03T09:15:00\"" +
                "}";

        ItemRequestDto itemRequestDto = json.parseObject(content);

        assertThat(itemRequestDto.getId()).isEqualTo(200L);
        assertThat(itemRequestDto.getDescription()).isEqualTo("Need a ladder for painting work");
        assertThat(itemRequestDto.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 3, 9, 15, 0));

        assertThat(itemRequestDto.getRequestor()).isNotNull();
        assertThat(itemRequestDto.getRequestor().getId()).isEqualTo(2L);
        assertThat(itemRequestDto.getRequestor().getName()).isEqualTo("Jane Smith");
        assertThat(itemRequestDto.getRequestor().getEmail()).isEqualTo("jane@example.com");
    }
}
