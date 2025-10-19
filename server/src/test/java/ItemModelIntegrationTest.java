import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(classes = ShareItServer.class)
public class ItemModelIntegrationTest {

    @Autowired
    private EntityManager em;

    @Test
    void shouldPersistItemWithMinimumFields() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@test.com");
        em.persist(owner);

        Item item = new Item();
        item.setName("Minimal Item");
        item.setAvailable(true);
        item.setOwner(owner);

        em.persist(item);
        em.flush();
        em.clear();

        Item saved = em.find(Item.class, item.getId());
        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("Minimal Item");
        assertThat(saved.getAvailable()).isTrue();
        assertThat(saved.getDescription()).isNull();
        assertThat(saved.getRequest()).isNull();
    }

    @Test
    void shouldPersistCommentWithMinimumFields() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@test.com");
        em.persist(owner);

        Item item = new Item();
        item.setName("Item");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        User author = new User();
        author.setName("Author");
        author.setEmail("author@test.com");
        em.persist(author);

        Comment comment = new Comment();
        comment.setText("Short comment");
        comment.setItem(item);
        comment.setAuthor(author);

        em.persist(comment);
        em.flush();
        em.clear();

        Comment saved = em.find(Comment.class, comment.getId());
        assertThat(saved).isNotNull();
        assertThat(saved.getText()).isEqualTo("Short comment");
        assertThat(saved.getCreated()).isNotNull();
    }
}
