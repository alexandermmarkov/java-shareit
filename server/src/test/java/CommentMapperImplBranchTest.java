import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ShareItServer.class)
public class CommentMapperImplBranchTest {
    @Autowired
    private CommentMapper commentMapper;

    @Test
    void toCommentShouldCoverAllBranches() {
        CommentDto commentDto = new CommentDto(null, "Test comment", null, null);
        User author = new User();
        author.setId(1L);
        Item item = new Item();
        item.setId(100L);

        Comment result = commentMapper.toComment(commentDto, author, item);

        assertThat(result.getId()).isNull();
        assertThat(result.getText()).isEqualTo("Test comment");
        assertThat(result.getAuthor()).isEqualTo(author);
        assertThat(result.getItem()).isEqualTo(item);
        assertThat(result.getCreated()).isNotNull();
    }

    @Test
    void commentAuthorNameShouldCoverNullAuthorBranch() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Test");

        CommentDto result = commentMapper.toCommentDto(comment);

        assertThat(result.getAuthorName()).isNull();
    }

    @Test
    void commentAuthorNameShouldCoverAuthorWithNameBranch() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Test");

        User author = new User();
        author.setName("John Doe");
        comment.setAuthor(author);

        CommentDto result = commentMapper.toCommentDto(comment);

        assertThat(result.getAuthorName()).isEqualTo("John Doe");
    }

    @Test
    void toCommentDtoShouldCoverAllBranches() {
        Comment comment1 = new Comment();
        comment1.setId(1L);
        comment1.setText("Comment 1");

        CommentDto result1 = commentMapper.toCommentDto(comment1);
        assertThat(result1.getAuthorName()).isNull();

        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setText("Comment 2");
        User authorWithoutName = new User();

        comment2.setAuthor(authorWithoutName);
        CommentDto result2 = commentMapper.toCommentDto(comment2);
        assertThat(result2.getAuthorName()).isNull();

        Comment comment3 = new Comment();
        comment3.setId(3L);
        comment3.setText("Comment 3");
        User authorWithName = new User();
        authorWithName.setName("Test Author");
        comment3.setAuthor(authorWithName);
        CommentDto result3 = commentMapper.toCommentDto(comment3);
        assertThat(result3.getAuthorName()).isEqualTo("Test Author");
    }
}
