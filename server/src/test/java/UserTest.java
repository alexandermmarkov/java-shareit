import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.User;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTest {
    @Test
    void equalsShouldReturnTrue_ForSameId() {
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(1L);

        assertThat(user1).isEqualTo(user2);
    }

    @Test
    void hashCodeShouldBeEqual_ForSameId() {
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(1L);

        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void shouldSetAndGetAllFields() {
        User user = new User();

        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@example.com");

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void shouldHandleNullValues() {
        User user = new User();

        user.setId(null);
        user.setName(null);
        user.setEmail(null);

        assertThat(user.getId()).isNull();
        assertThat(user.getName()).isNull();
        assertThat(user.getEmail()).isNull();
    }

    @Test
    void equalsShouldReturnTrueForSameId() {
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(1L);

        assertThat(user1).isEqualTo(user2);
    }

    @Test
    void equalsShouldReturnFalseForDifferentId() {
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(2L);

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    void hashCodeShouldBeEqualForSameId() {
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(1L);

        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void toStringShouldIncludeAllFields() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        String result = user.toString();

        assertThat(result).contains("id=1");
        assertThat(result).contains("name=Test User");
        assertThat(result).contains("email=test@example.com");
    }

}