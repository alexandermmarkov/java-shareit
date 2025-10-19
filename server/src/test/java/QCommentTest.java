import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.QComment;

import static org.assertj.core.api.Assertions.assertThat;

public class QCommentTest {
    @Test
    void shouldCreateQCommentInstance() {
        QComment qComment = QComment.comment;

        assertThat(qComment).isNotNull();
        assertThat(qComment.getType()).isEqualTo(Comment.class);
    }

    @Test
    void shouldAccessQCommentFields() {
        QComment qComment = QComment.comment;

        assertThat(qComment.id).isNotNull();
        assertThat(qComment.text).isNotNull();
        assertThat(qComment.item).isNotNull();
        assertThat(qComment.author).isNotNull();
        assertThat(qComment.created).isNotNull();
    }

    @Test
    void shouldUseQCommentInQueries() {
        QComment qComment = QComment.comment;

        BooleanExpression expression = qComment.id.eq(1L);
        assertThat(expression).isNotNull();
    }
}
