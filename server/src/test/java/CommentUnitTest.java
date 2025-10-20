import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class CommentUnitTest {
    @Test
    void shouldCreateCommentWithAllFields() {
        User author = new User();
        author.setId(1L);

        Item item = new Item();
        item.setId(100L);

        LocalDateTime created = LocalDateTime.now();

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Test comment");
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(created);

        assertThat(comment.getId()).isEqualTo(1L);
        assertThat(comment.getText()).isEqualTo("Test comment");
        assertThat(comment.getAuthor()).isEqualTo(author);
        assertThat(comment.getItem()).isEqualTo(item);
        assertThat(comment.getCreated()).isEqualTo(created);
    }

    @Test
    void shouldHandleNullValues() {
        Comment comment = new Comment();

        assertThat(comment.getId()).isNull();
        assertThat(comment.getText()).isNull();
        assertThat(comment.getAuthor()).isNull();
        assertThat(comment.getItem()).isNull();
        assertThat(comment.getCreated()).isNotNull();
    }

    @Test
    void shouldSetDefaultCreatedTimeIfNull() {
        Comment comment = new Comment();

        assertThat(comment.getCreated()).isNotNull();
    }

    @Test
    void shouldImplementEqualsCorrectly() {
        Comment comment1 = new Comment();
        comment1.setId(1L);

        Comment comment2 = new Comment();
        comment2.setId(1L);

        Comment comment3 = new Comment();
        comment3.setId(2L);

        assertThat(comment1).isEqualTo(comment1);
        assertThat(comment1).isNotEqualTo(null);
        assertThat(comment1).isNotEqualTo("string");
        assertThat(comment1).isEqualTo(comment2);
        assertThat(comment1).isNotEqualTo(comment3);
    }

    @Test
    void toStringShouldExcludeLazyFields() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Test comment");

        String result = comment.toString();

        assertThat(result).contains("id=1");
        assertThat(result).contains("text=Test comment");
        assertThat(result).doesNotContain("author");
        assertThat(result).doesNotContain("item");
    }
}
