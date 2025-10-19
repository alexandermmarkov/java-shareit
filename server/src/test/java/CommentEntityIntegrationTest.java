import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ShareItServer.class)
@Transactional
public class CommentEntityIntegrationTest {

    @Autowired
    private EntityManager em;

    @Test
    void shouldPersistAndRetrieveCommentWithAllFields() {
        User owner = new User();
        owner.setName("Item Owner");
        owner.setEmail("owner@test.com");
        em.persist(owner);

        User author = new User();
        author.setName("Comment Author");
        author.setEmail("author@test.com");
        em.persist(author);

        Item item = new Item();
        item.setName("Test Item");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        Comment comment = new Comment();
        comment.setText("This is a test comment");
        comment.setItem(item);
        comment.setAuthor(author);
        LocalDateTime createdTime = LocalDateTime.now().minusHours(1);
        comment.setCreated(createdTime);

        em.persist(comment);
        em.flush();
        em.clear();

        Comment retrieved = em.find(Comment.class, comment.getId());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getText()).isEqualTo("This is a test comment");
        assertThat(retrieved.getItem().getId()).isEqualTo(item.getId());
        assertThat(retrieved.getAuthor().getId()).isEqualTo(author.getId());
    }

    @Test
    void shouldHandleCommentWithDefaultCreatedTime() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@test.com");
        em.persist(owner);

        User author = new User();
        author.setName("Author");
        author.setEmail("author@test.com");
        em.persist(author);

        Item item = new Item();
        item.setName("Item");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        Comment comment = new Comment();
        comment.setText("Comment with default time");
        comment.setItem(item);
        comment.setAuthor(author);

        em.persist(comment);
        em.flush();
        em.clear();

        Comment retrieved = em.find(Comment.class, comment.getId());
        assertThat(retrieved.getCreated()).isNotNull();
        assertThat(retrieved.getCreated()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void shouldUpdateCommentText() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@test.com");
        em.persist(owner);

        User author = new User();
        author.setName("Author");
        author.setEmail("author@test.com");
        em.persist(author);

        Item item = new Item();
        item.setName("Item");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        Comment comment = new Comment();
        comment.setText("Original text");
        comment.setItem(item);
        comment.setAuthor(author);
        em.persist(comment);
        em.flush();

        comment.setText("Updated text");
        em.merge(comment);
        em.flush();
        em.clear();

        Comment updated = em.find(Comment.class, comment.getId());
        assertThat(updated.getText()).isEqualTo("Updated text");
    }

    @Test
    void shouldMaintainCommentRelationships() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@test.com");
        em.persist(owner);

        User author1 = new User();
        author1.setName("Author 1");
        author1.setEmail("author1@test.com");
        em.persist(author1);

        User author2 = new User();
        author2.setName("Author 2");
        author2.setEmail("author2@test.com");
        em.persist(author2);

        Item item1 = new Item();
        item1.setName("Item 1");
        item1.setAvailable(true);
        item1.setOwner(owner);
        em.persist(item1);

        Item item2 = new Item();
        item2.setName("Item 2");
        item2.setAvailable(true);
        item2.setOwner(owner);
        em.persist(item2);

        Comment comment1 = new Comment();
        comment1.setText("Comment 1");
        comment1.setItem(item1);
        comment1.setAuthor(author1);

        Comment comment2 = new Comment();
        comment2.setText("Comment 2");
        comment2.setItem(item2);
        comment2.setAuthor(author2);

        em.persist(comment1);
        em.persist(comment2);
        em.flush();
        em.clear();

        Comment retrieved1 = em.find(Comment.class, comment1.getId());
        Comment retrieved2 = em.find(Comment.class, comment2.getId());

        assertThat(retrieved1.getItem().getId()).isEqualTo(item1.getId());
        assertThat(retrieved2.getItem().getId()).isEqualTo(item2.getId());
        assertThat(retrieved1.getAuthor().getId()).isEqualTo(author1.getId());
        assertThat(retrieved2.getAuthor().getId()).isEqualTo(author2.getId());
    }
}
