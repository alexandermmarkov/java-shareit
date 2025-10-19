import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.ContextConfiguration;
import shareit.ShareItGateway;
import shareit.item.dto.CommentDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = ShareItGateway.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CommentDtoJsonTest {
    private final JacksonTester<CommentDto> json;

    @Test
    void testCommentDtoSerialization() throws Exception {
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 14, 30, 0);

        CommentDto commentDto = new CommentDto(
                50L,
                "This is a very useful item! Highly recommend.",
                "John Doe",
                created);

        JsonContent<CommentDto> result = json.write(commentDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(50);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("This is a very useful item! Highly recommend.");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("John Doe");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2024-01-01T14:30:00");
    }

    @Test
    void testCommentDtoDeserialization() throws Exception {
        String content = """
                {
                    "id": 50,
                    "text": "Great quality, works perfectly!",
                    "authorName": "Jane Smith",
                    "created": "2024-01-02T10:15:30"
                }
                """;

        CommentDto commentDto = json.parseObject(content);

        assertThat(commentDto.getId()).isEqualTo(50L);
        assertThat(commentDto.getText()).isEqualTo("Great quality, works perfectly!");
        assertThat(commentDto.getAuthorName()).isEqualTo("Jane Smith");
        assertThat(commentDto.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 2, 10, 15, 30));
    }
}
