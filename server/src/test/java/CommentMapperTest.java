import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import static org.assertj.core.api.Assertions.assertThat;

public class CommentMapperTest {
    private final CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);

    @Test
    void toCommentShouldIgnoreIdAndCreated() {
        CommentDto commentDto = new CommentDto(null, null, null, null);
        commentDto.setText("Test comment");
        User author = new User();
        Item item = new Item();

        Comment result = commentMapper.toComment(commentDto, author, item);

        assertThat(result.getId()).isNull();
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getText()).isEqualTo("Test comment");
        assertThat(result.getAuthor()).isEqualTo(author);
        assertThat(result.getItem()).isEqualTo(item);
    }
}
