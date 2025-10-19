import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = ShareItServer.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplTest {
    private final EntityManager em;
    private final UserService service;

    @Test
    void testCreateUser() {
        UserDto userDto = new UserDto(null, "John Doe", "john@example.com");

        UserDto result = service.createUser(userDto);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), equalTo(userDto.getName()));
        assertThat(result.getEmail(), equalTo(userDto.getEmail()));

        TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.id = :id", User.class);
        User savedUser = query.setParameter("id", result.getId()).getSingleResult();

        assertThat(savedUser.getName(), equalTo(userDto.getName()));
        assertThat(savedUser.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void testUpdateUser() {
        User existingUser = makeUser("old@example.com", "Old Name");
        em.persist(existingUser);
        em.flush();

        UserDto updateDto = new UserDto(null, "New Name", "new@example.com");

        UserDto result = service.updateUser(existingUser.getId(), updateDto);

        assertThat(result.getId(), equalTo(existingUser.getId()));
        assertThat(result.getName(), equalTo(updateDto.getName()));
        assertThat(result.getEmail(), equalTo(updateDto.getEmail()));

        TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.id = :id", User.class);
        User updatedUser = query.setParameter("id", existingUser.getId()).getSingleResult();

        assertThat(updatedUser.getName(), equalTo(updateDto.getName()));
        assertThat(updatedUser.getEmail(), equalTo(updateDto.getEmail()));
    }

    @Test
    void testUpdateUserOnlyName() {
        User existingUser = makeUser("user@example.com", "Old Name");
        em.persist(existingUser);
        em.flush();

        UserDto updateDto = new UserDto(null, "New Name", null);

        UserDto result = service.updateUser(existingUser.getId(), updateDto);

        assertThat(result.getId(), equalTo(existingUser.getId()));
        assertThat(result.getName(), equalTo(updateDto.getName()));
        assertThat(result.getEmail(), equalTo(existingUser.getEmail()));

        TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.id = :id", User.class);
        User updatedUser = query.setParameter("id", existingUser.getId()).getSingleResult();

        assertThat(updatedUser.getName(), equalTo(updateDto.getName()));
        assertThat(updatedUser.getEmail(), equalTo(existingUser.getEmail()));
    }

    @Test
    void testUpdateUserOnlyEmail() {
        User existingUser = makeUser("old@example.com", "John Doe");
        em.persist(existingUser);
        em.flush();

        UserDto updateDto = new UserDto(null, null, "new@example.com");

        UserDto result = service.updateUser(existingUser.getId(), updateDto);

        assertThat(result.getId(), equalTo(existingUser.getId()));
        assertThat(result.getName(), equalTo(existingUser.getName()));
        assertThat(result.getEmail(), equalTo(updateDto.getEmail()));

        TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.id = :id", User.class);
        User updatedUser = query.setParameter("id", existingUser.getId()).getSingleResult();

        assertThat(updatedUser.getName(), equalTo(existingUser.getName()));
        assertThat(updatedUser.getEmail(), equalTo(updateDto.getEmail()));
    }

    @Test
    void testUpdateUserWithBlankName() {
        User existingUser = makeUser("user@example.com", "Original Name");
        em.persist(existingUser);
        em.flush();

        UserDto updateDto = new UserDto(null, " ", "new@example.com");

        UserDto result = service.updateUser(existingUser.getId(), updateDto);

        assertThat(result.getId(), equalTo(existingUser.getId()));
        assertThat(result.getName(), equalTo(existingUser.getName()));
        assertThat(result.getEmail(), equalTo(updateDto.getEmail()));
    }

    @Test
    void testUpdateUserWithBlankEmail() {
        User existingUser = makeUser("original@example.com", "John Doe");
        em.persist(existingUser);
        em.flush();

        UserDto updateDto = new UserDto(null, "New Name", " ");

        UserDto result = service.updateUser(existingUser.getId(), updateDto);

        assertThat(result.getId(), equalTo(existingUser.getId()));
        assertThat(result.getName(), equalTo(updateDto.getName()));
        assertThat(result.getEmail(), equalTo(existingUser.getEmail()));
    }

    @Test
    void testUpdateUserWhenUserNotFound() {
        UserDto updateDto = new UserDto(null, "New Name", "new@example.com");

        assertThrows(NotFoundException.class, () -> service.updateUser(9999L, updateDto));
    }

    @Test
    void testGetUsers() {
        User user1 = makeUser("user1@example.com", "User One");
        em.persist(user1);

        User user2 = makeUser("user2@example.com", "User Two");
        em.persist(user2);

        em.flush();

        List<UserDto> result = service.getUsers();

        assertThat(result, hasSize(2));

        List<String> emails = result.stream()
                .map(UserDto::getEmail)
                .collect(Collectors.toList());
        assertThat(emails, containsInAnyOrder("user1@example.com", "user2@example.com"));

        List<String> names = result.stream()
                .map(UserDto::getName)
                .collect(Collectors.toList());
        assertThat(names, containsInAnyOrder("User One", "User Two"));
    }

    @Test
    void testGetUsersWhenEmpty() {
        List<UserDto> result = service.getUsers();

        assertThat(result, hasSize(0));
    }

    @Test
    void testGetUserById() {
        User user = makeUser("test@example.com", "Test User");
        em.persist(user);
        em.flush();

        UserDto result = service.getUserById(user.getId());

        assertThat(result.getId(), equalTo(user.getId()));
        assertThat(result.getName(), equalTo(user.getName()));
        assertThat(result.getEmail(), equalTo(user.getEmail()));
    }

    @Test
    void testGetUserByIdWhenNotFound() {
        assertThrows(NotFoundException.class, () -> service.getUserById(9999L));
    }

    @Test
    void testDeleteUserById() {
        User user = makeUser("delete@example.com", "To Delete");
        em.persist(user);
        em.flush();

        TypedQuery<User> queryBefore = em.createQuery("SELECT u FROM User u WHERE u.id = :id", User.class);
        User userBefore = queryBefore.setParameter("id", user.getId()).getSingleResult();
        assertThat(userBefore, notNullValue());

        service.deleteUserById(user.getId());

        TypedQuery<User> queryAfter = em.createQuery("SELECT u FROM User u WHERE u.id = :id", User.class);
        List<User> usersAfter = queryAfter.setParameter("id", user.getId()).getResultList();

        assertThat(usersAfter, hasSize(0));
    }

    @Test
    void testCreateUserWithExistingEmail() {
        User existingUser = makeUser("duplicate@example.com", "Existing User");
        em.persist(existingUser);
        em.flush();

        UserDto duplicateUserDto = new UserDto(null, "New User", "duplicate@example.com");

        assertThrows(Exception.class, () -> {
            service.createUser(duplicateUserDto);
            em.flush();
        });
    }

    @Test
    void testUpdateUserWithExistingEmail() {
        User user1 = makeUser("user1@example.com", "User One");
        em.persist(user1);

        User user2 = makeUser("user2@example.com", "User Two");
        em.persist(user2);

        em.flush();

        UserDto updateDto = new UserDto(null, "Updated User", "user2@example.com");

        assertThrows(Exception.class, () -> {
            service.updateUser(user1.getId(), updateDto);
            em.flush();
        });
    }

    private User makeUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }
}
