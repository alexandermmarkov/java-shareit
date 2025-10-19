import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import shareit.ShareItGateway;
import shareit.user.UserClient;
import shareit.user.UserController;
import shareit.user.dto.UserDto;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@ContextConfiguration(classes = ShareItGateway.class)
class UserControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserClient userClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUserWhenEmailIsInvalid() throws Exception {
        UserDto invalidDto = new UserDto(null, "John Doe", "invalid-email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Некорректный формат электронной почты")));
    }

    @Test
    void createUserWhenEmailIsNull() throws Exception {
        UserDto invalidDto = new UserDto(null, "John Doe", null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("У нового пользователя обязательно должен быть указан email")));
    }

    @Test
    void createUserWhenEmailIsBlank() throws Exception {
        UserDto invalidDto = new UserDto(null, "John Doe", "   ");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Некорректный формат электронной почты")));
    }

    @Test
    void createUserWhenEmailIsEmpty() throws Exception {
        UserDto invalidDto = new UserDto(null, "John Doe", "");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("У нового пользователя обязательно должен быть указан email")));
    }

    @Test
    void updateUserWhenEmailIsInvalid() throws Exception {
        UserDto invalidDto = new UserDto(null, "John Doe", "invalid-email");

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Некорректный формат электронной почты")));
    }

    @Test
    void updateUserWhenUserIdIsNegative() throws Exception {
        UserDto validDto = new UserDto(null, "John Doe", "john@example.com");

        mockMvc.perform(patch("/users/-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserWhenUserIdIsZero() throws Exception {
        UserDto validDto = new UserDto(null, "John Doe", "john@example.com");

        mockMvc.perform(patch("/users/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserByIdWhenUserIdIsNegative() throws Exception {
        mockMvc.perform(get("/users/-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserByIdWhenUserIdIsZero() throws Exception {
        mockMvc.perform(get("/users/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUserByIdWhenUserIdIsNegative() throws Exception {
        mockMvc.perform(delete("/users/-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUserByIdWhenUserIdIsZero() throws Exception {
        mockMvc.perform(delete("/users/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUserWithValidData() throws Exception {
        UserDto validDto = new UserDto(null, "John Doe", "john.doe@example.com");
        UserDto responseDto = new UserDto(1L, "John Doe", "john.doe@example.com");

        Mockito.when(userClient.createUser(any(UserDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(responseDto));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        Mockito.verify(userClient, times(1)).createUser(validDto);
    }

    @Test
    void createUserWithNullName() throws Exception {
        UserDto validDto = new UserDto(null, null, "john.doe@example.com");
        UserDto responseDto = new UserDto(1L, null, "john.doe@example.com");

        Mockito.when(userClient.createUser(any(UserDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(responseDto));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated());

        Mockito.verify(userClient, times(1)).createUser(validDto);
    }

    @Test
    void updateUserWithValidData() throws Exception {
        UserDto updateDto = new UserDto(null, "John Updated", "john.updated@example.com");
        UserDto responseDto = new UserDto(1L, "John Updated", "john.updated@example.com");

        Mockito.when(userClient.updateUser(anyLong(), any(UserDto.class)))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"));

        Mockito.verify(userClient, times(1)).updateUser(1L, updateDto);
    }

    @Test
    void updateUserWithPartialData() throws Exception {
        UserDto updateDto = new UserDto(null, "John Updated", null);
        UserDto responseDto = new UserDto(1L, "John Updated", "original@example.com");

        Mockito.when(userClient.updateUser(anyLong(), any(UserDto.class)))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        Mockito.verify(userClient, times(1)).updateUser(1L, updateDto);
    }

    @Test
    void getUsers() throws Exception {
        List<UserDto> responseList = List.of(
                new UserDto(1L, "John Doe", "john@example.com"),
                new UserDto(2L, "Jane Smith", "jane@example.com"),
                new UserDto(3L, "Bob Johnson", "bob@example.com")
        );

        Mockito.when(userClient.getUsers())
                .thenReturn(ResponseEntity.ok(responseList));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[2].id").value(3L));

        Mockito.verify(userClient, times(1)).getUsers();
    }

    @Test
    void getUsersWhenNoUsers() throws Exception {
        Mockito.when(userClient.getUsers())
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        Mockito.verify(userClient, times(1)).getUsers();
    }

    @Test
    void getUserByIdWithValidId() throws Exception {
        UserDto responseDto = new UserDto(1L, "John Doe", "john@example.com");

        Mockito.when(userClient.getUserById(anyLong()))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        Mockito.verify(userClient, times(1)).getUserById(1L);
    }

    @Test
    void deleteUserByIdWithValidId() throws Exception {
        Mockito.when(userClient.deleteUserById(anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        Mockito.verify(userClient, times(1)).deleteUserById(1L);
    }

    @Test
    void createUserWithValidEmailFormats() throws Exception {
        UserDto validDto = new UserDto(null, "Test User", "test@yandex.ru");
        UserDto responseDto = new UserDto(1L, "Test User", "test@yandex.ru");

        Mockito.when(userClient.createUser(any(UserDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(responseDto));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void createUserWithInvalidEmailFormats() throws Exception {
        UserDto invalidDto = new UserDto(null, "Test User", "email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUserWithoutContentType() throws Exception {
        UserDto validDto = new UserDto(null, "John Doe", "john@example.com");

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateUserWithoutContentType() throws Exception {
        UserDto validDto = new UserDto(null, "John Doe", "john@example.com");

        mockMvc.perform(patch("/users/1")
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void createUserWithInvalidJson() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateUserWithInvalidJson() throws Exception {
        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isInternalServerError());
    }
}
