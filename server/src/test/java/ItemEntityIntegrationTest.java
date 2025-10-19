import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ShareItServer.class)
@Transactional
public class ItemEntityIntegrationTest {

    @Autowired
    private EntityManager em;

    @Test
    void shouldPersistAndRetrieveItemWithAllFields() {
        User owner = new User();
        owner.setName("Test Owner");
        owner.setEmail("owner@test.com");
        em.persist(owner);

        User requestor = new User();
        requestor.setName("Test Requestor");
        requestor.setEmail("requestor@test.com");
        em.persist(requestor);

        ItemRequest request = new ItemRequest();
        request.setDescription("Need item");
        request.setRequestor(requestor);
        em.persist(request);

        Item item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);

        em.persist(item);
        em.flush();
        em.clear();

        Item retrieved = em.find(Item.class, item.getId());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getName()).isEqualTo("Test Item");
        assertThat(retrieved.getDescription()).isEqualTo("Test Description");
        assertThat(retrieved.getAvailable()).isTrue();
        assertThat(retrieved.getOwner().getId()).isEqualTo(owner.getId());
        assertThat(retrieved.getRequest().getId()).isEqualTo(request.getId());
    }

    @Test
    void shouldHandleItemWithNullFields() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@test.com");
        em.persist(owner);

        Item item = new Item();
        item.setName("Item with null fields");
        item.setAvailable(true);
        item.setOwner(owner);

        em.persist(item);
        em.flush();
        em.clear();

        Item retrieved = em.find(Item.class, item.getId());
        assertThat(retrieved.getDescription()).isNull();
        assertThat(retrieved.getRequest()).isNull();
    }

    @Test
    void shouldUpdateItemFields() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@test.com");
        em.persist(owner);

        Item item = new Item();
        item.setName("Original Name");
        item.setDescription("Original Description");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);
        em.flush();

        item.setName("Updated Name");
        item.setDescription("Updated Description");
        item.setAvailable(false);
        em.merge(item);
        em.flush();
        em.clear();

        Item updated = em.find(Item.class, item.getId());
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
        assertThat(updated.getAvailable()).isFalse();
    }

    @Test
    void shouldMaintainItemRelationships() {
        User owner1 = new User();
        owner1.setName("Owner 1");
        owner1.setEmail("owner1@test.com");
        em.persist(owner1);

        User owner2 = new User();
        owner2.setName("Owner 2");
        owner2.setEmail("owner2@test.com");
        em.persist(owner2);

        Item item1 = new Item();
        item1.setName("Item 1");
        item1.setAvailable(true);
        item1.setOwner(owner1);

        Item item2 = new Item();
        item2.setName("Item 2");
        item2.setAvailable(true);
        item2.setOwner(owner2);

        em.persist(item1);
        em.persist(item2);
        em.flush();
        em.clear();

        Item retrieved1 = em.find(Item.class, item1.getId());
        Item retrieved2 = em.find(Item.class, item2.getId());

        assertThat(retrieved1.getOwner().getId()).isEqualTo(owner1.getId());
        assertThat(retrieved2.getOwner().getId()).isEqualTo(owner2.getId());
        assertThat(retrieved1.getOwner().getId()).isNotEqualTo(retrieved2.getOwner().getId());
    }
}
