import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import shareit.client.BaseClient;
import shareit.user.UserClient;
import shareit.user.dto.UserDto;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserClientTest {

    @Mock
    private RestTemplate restTemplate;

    private UserClient userClient;

    @BeforeEach
    void setUp() {
        userClient = new UserClient("http://localhost:8080", new RestTemplateBuilder());
        setRestTemplateField(userClient, restTemplate);
    }

    @Test
    void createUserShouldCallPostWithCorrectParameters() {
        UserDto userDto = createUserDto();

        try {
            userClient.createUser(userDto);
        } catch (Exception ignored) {

        }

        verify(restTemplate).exchange(
                eq(""),
                eq(HttpMethod.POST),
                argThat(httpEntity ->
                        httpEntity != null &&
                                httpEntity.getBody() == userDto
                ),
                eq(Object.class)
        );
    }

    @Test
    void updateUserShouldCallPatchWithCorrectParameters() {
        long userId = 1L;
        UserDto userDto = createUserDto();

        try {
            userClient.updateUser(userId, userDto);
        } catch (Exception ignored) {

        }

        verify(restTemplate).exchange(
                eq("/1"),
                eq(HttpMethod.PATCH),
                argThat(httpEntity ->
                        httpEntity != null &&
                                httpEntity.getBody() == userDto
                ),
                eq(Object.class)
        );
    }

    @Test
    void getUsersShouldCallGetWithCorrectParameters() {
        try {
            userClient.getUsers();
        } catch (Exception ignored) {

        }

        verify(restTemplate).exchange(
                eq(""),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        );
    }

    @Test
    void getUserByIdShouldCallGetWithCorrectParameters() {
        long userId = 1L;

        try {
            userClient.getUserById(userId);
        } catch (Exception ignored) {

        }

        verify(restTemplate).exchange(
                eq("/1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        );
    }

    @Test
    void deleteUserByIdShouldCallDeleteWithCorrectParameters() {
        long userId = 1L;

        try {
            userClient.deleteUserById(userId);
        } catch (Exception ignored) {

        }

        verify(restTemplate).exchange(
                eq("/1"),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Object.class)
        );
    }

    @Test
    void createUserWithHttpErrorShouldReturnErrorResponse() {
        UserDto userDto = createUserDto();

        HttpStatusCodeException exception = Mockito.mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getResponseBodyAsByteArray()).thenReturn("Invalid user".getBytes());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> result = userClient.createUser(userDto);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateUserWithHttpErrorShouldReturnErrorResponse() {
        long userId = 1L;
        UserDto userDto = createUserDto();

        HttpStatusCodeException exception = Mockito.mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(exception.getResponseBodyAsByteArray()).thenReturn("User not found".getBytes());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> result = userClient.updateUser(userId, userDto);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteUserByIdWithHttpErrorShouldReturnErrorResponse() {
        long userId = 1L;

        HttpStatusCodeException exception = Mockito.mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(exception.getResponseBodyAsByteArray()).thenReturn("User not found".getBytes());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> result = userClient.deleteUserById(userId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private UserDto createUserDto() {
        return new UserDto(
                null,
                "Test User",
                "test@example.com"
        );
    }

    private void setRestTemplateField(BaseClient client, RestTemplate restTemplate) {
        try {
            Field restTemplateField = BaseClient.class.getDeclaredField("rest");
            restTemplateField.setAccessible(true);
            restTemplateField.set(client, restTemplate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set restTemplate field", e);
        }
    }
}
