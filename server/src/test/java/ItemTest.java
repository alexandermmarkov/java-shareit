import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemTest {

    @Test
    void equalsShouldReturnTrueForSameObject() {
        Item item = new Item();
        item.setId(1L);

        assertThat(item).isEqualTo(item);
    }

    @Test
    void equalsShouldReturnFalseForDifferentClass() {
        Item item = new Item();
        item.setId(1L);

        assertThat(item).isNotEqualTo(new Object());
    }

    @Test
    void equalsShouldReturnFalseForDifferentId() {
        Item item1 = new Item();
        item1.setId(1L);

        Item item2 = new Item();
        item2.setId(2L);

        assertThat(item1).isNotEqualTo(item2);
    }

    @Test
    void equalsAndHashCodeShouldWorkWithManualImplementation() {
        Item item1 = new Item();
        item1.setId(1L);

        Item item2 = new Item();
        item2.setId(1L);

        Item item3 = new Item();
        item3.setId(2L);

        assertThat(item1).isEqualTo(item2);
        assertThat(item1).isNotEqualTo(item3);

        assertThat(item1.hashCode()).isEqualTo(item2.hashCode());

        assertThat(item1.hashCode()).isEqualTo(item1.hashCode());
        assertThat(item1).isEqualTo(item1);
    }

    @Test
    void toStringShouldNotContainLazyFields() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);

        String toString = item.toString();

        assertThat(toString).contains("Test Item");
        assertThat(toString).contains("Test Description");
        assertThat(toString).doesNotContain("owner");
        assertThat(toString).doesNotContain("request");
    }

    @Test
    void shouldCorrectlyInitializeItem() {
        Item item = new Item();

        assertThat(item.getId()).isNull();
        assertThat(item.getName()).isNull();
        assertThat(item.getDescription()).isNull();
        assertThat(item.getAvailable()).isNull();
        assertThat(item.getOwner()).isNull();
        assertThat(item.getRequest()).isNull();
    }

    @Test
    void shouldSetAndGetAllProperties() {
        Item item = new Item();
        User owner = new User();
        ItemRequest request = new ItemRequest();

        item.setId(1L);
        item.setName("Drill");
        item.setDescription("Powerful electric drill");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);

        assertThat(item.getId()).isEqualTo(1L);
        assertThat(item.getName()).isEqualTo("Drill");
        assertThat(item.getDescription()).isEqualTo("Powerful electric drill");
        assertThat(item.getAvailable()).isTrue();
        assertThat(item.getOwner()).isEqualTo(owner);
        assertThat(item.getRequest()).isEqualTo(request);
    }

    @Test
    void toStringShouldExcludeLazyFields() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setAvailable(true);

        String result = item.toString();

        assertThat(result).contains("id=1");
        assertThat(result).contains("name=Test Item");
        assertThat(result).contains("available=true");

        assertThat(result).doesNotContain("owner");
        assertThat(result).doesNotContain("request");
    }

    @Test
    void shouldHandleNullValuesInSetters() {
        Item item = new Item();

        item.setId(null);
        item.setName(null);
        item.setDescription(null);
        item.setAvailable(null);
        item.setOwner(null);
        item.setRequest(null);

        assertThat(item.getId()).isNull();
        assertThat(item.getName()).isNull();
        assertThat(item.getDescription()).isNull();
        assertThat(item.getAvailable()).isNull();
        assertThat(item.getOwner()).isNull();
        assertThat(item.getRequest()).isNull();
    }

    @Test
    void shouldHandleEdgeCasesForBoolean() {
        Item item = new Item();

        item.setAvailable(true);
        assertThat(item.getAvailable()).isTrue();

        item.setAvailable(false);
        assertThat(item.getAvailable()).isFalse();
    }

    @Test
    void shouldMaintainObjectConsistency() {
        Item item = new Item();
        User owner = new User();
        ItemRequest request = new ItemRequest();

        item.setId(1L);
        item.setName("Test");
        item.setDescription("Desc");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);

        assertThat(item.getId()).isEqualTo(1L);
        assertThat(item.getName()).isEqualTo("Test");
        assertThat(item.getDescription()).isEqualTo("Desc");
        assertThat(item.getAvailable()).isTrue();
        assertThat(item.getOwner()).isEqualTo(owner);
        assertThat(item.getRequest()).isEqualTo(request);
    }

    @Test
    void shouldSetAndGetAllFields() {
        Item item = new Item();
        User owner = new User();
        ItemRequest request = new ItemRequest();

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
    void toStringShouldNotContainExcludedFields() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test");

        String result = item.toString();

        assertThat(result).doesNotContain("owner");
        assertThat(result).doesNotContain("request");
    }
}
