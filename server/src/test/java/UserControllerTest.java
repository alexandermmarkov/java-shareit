import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.exception.IncorrectDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@ContextConfiguration(classes = ShareItServer.class)
class UserControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    UserService userService;

    @Autowired
    private MockMvc mvc;

    private final UserDto userDto = new UserDto(1L, "John Doe", "john@example.com");
    private final UserDto anotherUserDto = new UserDto(2L, "Jane Smith", "jane@example.com");
    private final UserDto updatedUserDto = new UserDto(1L, "John Updated", "john.updated@example.com");

    @Test
    void createUser() throws Exception {
        Mockito.when(userService.createUser(any(UserDto.class)))
                .thenReturn(userDto);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(userDto.getId().intValue())))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    void updateUser() throws Exception {
        Mockito.when(userService.updateUser(anyLong(), any(UserDto.class)))
                .thenReturn(updatedUserDto);

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(updatedUserDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedUserDto.getId().intValue())))
                .andExpect(jsonPath("$.name", is(updatedUserDto.getName())))
                .andExpect(jsonPath("$.email", is(updatedUserDto.getEmail())));
    }

    @Test
    void getUsers() throws Exception {
        List<UserDto> users = List.of(userDto, anotherUserDto);

        Mockito.when(userService.getUsers())
                .thenReturn(users);

        mvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(userDto.getId().intValue())))
                .andExpect(jsonPath("$[0].name", is(userDto.getName())))
                .andExpect(jsonPath("$[1].id", is(anotherUserDto.getId().intValue())))
                .andExpect(jsonPath("$[1].name", is(anotherUserDto.getName())));
    }

    @Test
    void getUsersWhenEmpty() throws Exception {
        Mockito.when(userService.getUsers())
                .thenReturn(List.of());

        mvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getUserById() throws Exception {
        Mockito.when(userService.getUserById(anyLong()))
                .thenReturn(userDto);

        mvc.perform(get("/users/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId().intValue())))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    void deleteUserById() throws Exception {
        Mockito.doNothing().when(userService).deleteUserById(anyLong());

        mvc.perform(delete("/users/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(userService, times(1)).deleteUserById(1L);
    }

    @Test
    void createUserWhenServiceThrowsValidationException() throws Exception {
        Mockito.when(userService.createUser(any(UserDto.class)))
                .thenThrow(new IncorrectDataException("Email already exists"));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateUserWhenServiceThrowsNotFoundException() throws Exception {
        Mockito.when(userService.updateUser(anyLong(), any(UserDto.class)))
                .thenThrow(new NotFoundException("User not found"));

        mvc.perform(patch("/users/999")
                        .content(mapper.writeValueAsString(updatedUserDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUserWhenServiceThrowsValidationException() throws Exception {
        Mockito.when(userService.updateUser(anyLong(), any(UserDto.class)))
                .thenThrow(new IncorrectDataException("Invalid email format"));

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(updatedUserDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getUserByIdWhenServiceThrowsNotFoundException() throws Exception {
        Mockito.when(userService.getUserById(anyLong()))
                .thenThrow(new NotFoundException("User not found"));

        mvc.perform(get("/users/999")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUserByIdWhenServiceThrowsNotFoundException() throws Exception {
        Mockito.doThrow(new NotFoundException("User not found"))
                .when(userService).deleteUserById(anyLong());

        mvc.perform(delete("/users/999")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
