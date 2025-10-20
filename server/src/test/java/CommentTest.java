import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class CommentTest {

    @Test
    void equalsShouldReturnTrueForSameObject() {
        Comment comment = new Comment();
        comment.setId(1L);

        assertThat(comment).isEqualTo(comment);
    }

    @Test
    void equalsShouldReturnTrueForSameId() {
        Comment comment1 = new Comment();
        comment1.setId(1L);

        Comment comment2 = new Comment();
        comment2.setId(1L);

        assertThat(comment1).isEqualTo(comment2);
    }

    @Test
    void equalsShouldReturnFalseForDifferentClass() {
        Comment comment = new Comment();
        comment.setId(1L);

        assertThat(comment).isNotEqualTo(new Object());
    }

    @Test
    void equalsShouldReturnFalseForDifferentObjects() {
        Comment comment1 = new Comment();
        comment1.setId(1L);

        Comment comment2 = new Comment();
        comment2.setId(2L);

        assertThat(comment1).isNotEqualTo(comment2);
    }

    @Test
    void hashCodeShouldBeDifferentForDifferentObjects() {
        Comment comment1 = new Comment();
        comment1.setId(1L);

        Comment comment2 = new Comment();
        comment2.setId(2L);

        assertThat(comment1.hashCode()).isNotEqualTo(comment2.hashCode());
    }

    @Test
    void hashCodeShouldBeConsistent() {
        Comment comment = new Comment();
        comment.setId(1L);

        int firstHash = comment.hashCode();
        int secondHash = comment.hashCode();

        assertThat(firstHash).isEqualTo(secondHash);
    }

    @Test
    void shouldHandleNullValuesInSetters() {
        Comment comment = new Comment();

        comment.setId(null);
        comment.setText(null);
        comment.setItem(null);
        comment.setAuthor(null);
        comment.setCreated(null);

        assertThat(comment.getId()).isNull();
        assertThat(comment.getText()).isNull();
        assertThat(comment.getItem()).isNull();
        assertThat(comment.getAuthor()).isNull();
        assertThat(comment.getCreated()).isNull();
    }

    @Test
    void shouldHandleEmptyText() {
        Comment comment = new Comment();

        comment.setText("");
        assertThat(comment.getText()).isEmpty();

        comment.setText("   ");
        assertThat(comment.getText()).isEqualTo("   ");
    }

    @Test
    void shouldMaintainObjectConsistency() {
        Comment comment = new Comment();
        User author = new User();
        Item item = new Item();
        LocalDateTime created = LocalDateTime.now();

        comment.setId(1L);
        comment.setText("Comment text");
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(created);

        assertThat(comment.getId()).isEqualTo(1L);
        assertThat(comment.getText()).isEqualTo("Comment text");
        assertThat(comment.getAuthor()).isEqualTo(author);
        assertThat(comment.getItem()).isEqualTo(item);
        assertThat(comment.getCreated()).isEqualTo(created);
    }

    @Test
    void shouldHaveReasonableDefaultForCreated() {
        Comment comment = new Comment();

        assertThat(comment.getCreated()).isNotNull();
    }

    @Test
    void shouldSetAndGetAllFields() {
        User author = new User();
        author.setId(1L);

        Item item = new Item();
        item.setId(100L);

        LocalDateTime created = LocalDateTime.now();

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Great item!");
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(created);

        assertThat(comment.getId()).isEqualTo(1L);
        assertThat(comment.getText()).isEqualTo("Great item!");
        assertThat(comment.getAuthor()).isEqualTo(author);
        assertThat(comment.getItem()).isEqualTo(item);
        assertThat(comment.getCreated()).isEqualTo(created);
    }

    @Test
    void shouldSetDefaultCreatedTime() {
        Comment comment = new Comment();

        assertThat(comment.getCreated()).isNotNull();
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
