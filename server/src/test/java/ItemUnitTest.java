import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemUnitTest {
    @Test
    void shouldCreateItemWithAllFields() {
        User owner = new User();
        owner.setId(1L);

        ItemRequest request = new ItemRequest();
        request.setId(100L);

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);

        assertThat(item.getId()).isEqualTo(1L);
        assertThat(item.getName()).isEqualTo("Test Item");
        assertThat(item.getDescription()).isEqualTo("Test Description");
        assertThat(item.getAvailable()).isTrue();
        assertThat(item.getOwner()).isEqualTo(owner);
        assertThat(item.getRequest()).isEqualTo(request);
    }

    @Test
    void shouldHandleNullValues() {
        Item item = new Item();

        assertThat(item.getId()).isNull();
        assertThat(item.getName()).isNull();
        assertThat(item.getDescription()).isNull();
        assertThat(item.getAvailable()).isNull();
        assertThat(item.getOwner()).isNull();
        assertThat(item.getRequest()).isNull();
    }

    @Test
    void shouldImplementEqualsCorrectly() {
        Item item1 = new Item();
        item1.setId(1L);

        Item item2 = new Item();
        item2.setId(1L);

        Item item3 = new Item();
        item3.setId(2L);

        assertThat(item1).isEqualTo(item1);
        assertThat(item1).isNotEqualTo(null);
        assertThat(item1).isNotEqualTo("string");
        assertThat(item1).isEqualTo(item2);
        assertThat(item1).isNotEqualTo(item3);
    }

    @Test
    void shouldImplementHashCodeCorrectly() {
        Item item1 = new Item();
        item1.setId(1L);

        Item item2 = new Item();
        item2.setId(1L);

        assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
        assertThat(item1.hashCode()).isEqualTo(item1.hashCode());
    }

    @Test
    void toStringShouldExcludeLazyFields() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);

        String result = item.toString();

        assertThat(result).contains("id=1");
        assertThat(result).contains("name=Test Item");
        assertThat(result).contains("description=Test Description");
        assertThat(result).contains("available=true");
        assertThat(result).doesNotContain("owner");
        assertThat(result).doesNotContain("request");
    }
}
