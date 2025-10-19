import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemRequestTest {
    @Test
    void equalsShouldReturnTrue_ForSameId() {
        ItemRequest request1 = new ItemRequest();
        request1.setId(1L);

        ItemRequest request2 = new ItemRequest();
        request2.setId(1L);

        assertThat(request1).isEqualTo(request2);
    }

    @Test
    void hashCodeShouldBeEqualForSameId() {
        ItemRequest request1 = new ItemRequest();
        request1.setId(1L);

        ItemRequest request2 = new ItemRequest();
        request2.setId(1L);

        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    void shouldSetAndGetAllFields() {
        User requestor = new User();
        requestor.setId(1L);

        LocalDateTime created = LocalDateTime.now();

        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Need a drill");
        request.setRequestor(requestor);
        request.setCreated(created);

        assertThat(request.getId()).isEqualTo(1L);
        assertThat(request.getDescription()).isEqualTo("Need a drill");
        assertThat(request.getRequestor()).isEqualTo(requestor);
        assertThat(request.getCreated()).isEqualTo(created);
    }

    @Test
    void shouldHandleNullValues() {
        ItemRequest request = new ItemRequest();

        request.setId(null);
        request.setDescription(null);
        request.setRequestor(null);
        request.setCreated(null);

        assertThat(request.getId()).isNull();
        assertThat(request.getDescription()).isNull();
        assertThat(request.getRequestor()).isNull();
        assertThat(request.getCreated()).isNull();
    }

    @Test
    void shouldSetDefaultCreatedTime() {
        ItemRequest request = new ItemRequest();

        assertThat(request.getCreated()).isNotNull();
    }

    @Test
    void equalsShouldReturnTrueForSameId() {
        ItemRequest request1 = new ItemRequest();
        request1.setId(1L);

        ItemRequest request2 = new ItemRequest();
        request2.setId(1L);

        assertThat(request1).isEqualTo(request2);
    }

    @Test
    void toStringShouldExcludeLazyFields() {
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Test request");

        String result = request.toString();

        assertThat(result).contains("id=1");
        assertThat(result).contains("description=Test request");
        assertThat(result).doesNotContain("requestor");
    }
}
