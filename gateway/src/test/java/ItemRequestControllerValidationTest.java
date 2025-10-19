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
import shareit.request.ItemRequestClient;
import shareit.request.ItemRequestController;
import shareit.request.dto.ItemRequestDto;
import shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
@ContextConfiguration(classes = ShareItGateway.class)
class ItemRequestControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addItemRequestWhenDescriptionIsNull() throws Exception {
        ItemRequestDto invalidDto = new ItemRequestDto(null, null, null, null);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Описание вещи не может быть пустым")));
    }

    @Test
    void addItemRequestWhenDescriptionIsBlank() throws Exception {
        ItemRequestDto invalidDto = new ItemRequestDto(null, "   ", null, null);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Описание вещи не может быть пустым")));
    }

    @Test
    void addItemRequestWhenDescriptionIsEmpty() throws Exception {
        ItemRequestDto invalidDto = new ItemRequestDto(null, "", null, null);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Описание вещи не может быть пустым")));
    }

    @Test
    void addItemRequestWhenDescriptionIsTooLong() throws Exception {
        String longDescription = "a".repeat(2001);
        ItemRequestDto invalidDto = new ItemRequestDto(null, longDescription, null, null);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Описание вещи не должно превышать 2000 символов")));
    }

    @Test
    void addItemRequestWhenUserIdIsNegative() throws Exception {
        ItemRequestDto validDto = new ItemRequestDto(null, "Need a power drill", null, null);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItemRequestWhenUserIdIsZero() throws Exception {
        ItemRequestDto validDto = new ItemRequestDto(null, "Need a power drill", null, null);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItemRequestsWhenUserIdIsNegative() throws Exception {
        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", -1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllItemRequestsWhenUserIdIsNegative() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", -1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItemRequestByIdWhenUserIdIsNegative() throws Exception {
        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", -1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItemRequestByIdWhenRequestIdIsNegative() throws Exception {
        mockMvc.perform(get("/requests/-1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItemRequestByIdWhenRequestIdIsZero() throws Exception {
        mockMvc.perform(get("/requests/0")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItemRequestWithValidData() throws Exception {
        UserDto requestor = new UserDto(1L, "John Doe", "john@example.com");
        ItemRequestDto validDto = new ItemRequestDto(null, "Need a power drill for home repairs", null, null);
        ItemRequestDto responseDto = new ItemRequestDto(1L, "Need a power drill for home repairs", requestor, LocalDateTime.now());

        Mockito.when(itemRequestClient.addItemRequest(anyLong(), any(ItemRequestDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(responseDto));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a power drill for home repairs"));

        Mockito.verify(itemRequestClient, times(1)).addItemRequest(1L, validDto);
    }

    @Test
    void getItemRequestsWithValidUserId() throws Exception {
        UserDto requestor = new UserDto(1L, "John Doe", "john@example.com");
        List<ItemRequestDto> responseList = List.of(
                new ItemRequestDto(1L, "Need a drill", requestor, LocalDateTime.now().minusDays(1)),
                new ItemRequestDto(2L, "Need a hammer", requestor, LocalDateTime.now().minusHours(5))
        );

        Mockito.when(itemRequestClient.getItemRequests(anyLong()))
                .thenReturn(ResponseEntity.ok(responseList));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        Mockito.verify(itemRequestClient, times(1)).getItemRequests(1L);
    }

    @Test
    void getAllItemRequestsWithValidUserId() throws Exception {
        UserDto requestor1 = new UserDto(1L, "John Doe", "john@example.com");
        UserDto requestor2 = new UserDto(2L, "Jane Smith", "jane@example.com");
        List<ItemRequestDto> responseList = List.of(
                new ItemRequestDto(1L, "Need a drill", requestor1, LocalDateTime.now().minusDays(2)),
                new ItemRequestDto(2L, "Need a saw", requestor2, LocalDateTime.now().minusDays(1)),
                new ItemRequestDto(3L, "Need a ladder", requestor1, LocalDateTime.now().minusHours(3))
        );

        Mockito.when(itemRequestClient.getAllItemRequests(anyLong()))
                .thenReturn(ResponseEntity.ok(responseList));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[2].id").value(3L));

        Mockito.verify(itemRequestClient, times(1)).getAllItemRequests(1L);
    }

    @Test
    void getItemRequestByIdWithValidIds() throws Exception {
        UserDto requestor = new UserDto(1L, "John Doe", "john@example.com");
        ItemRequestDto responseDto = new ItemRequestDto(1L, "Need a power drill", requestor, LocalDateTime.now().minusDays(1));

        Mockito.when(itemRequestClient.getItemRequestById(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a power drill"))
                .andExpect(jsonPath("$.requestor.id").value(1L))
                .andExpect(jsonPath("$.requestor.name").value("John Doe"));

        Mockito.verify(itemRequestClient, times(1)).getItemRequestById(1L, 1L);
    }

    @Test
    void getItemRequestsWhenNoRequests() throws Exception {
        Mockito.when(itemRequestClient.getItemRequests(anyLong()))
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        Mockito.verify(itemRequestClient, times(1)).getItemRequests(1L);
    }

    @Test
    void getAllItemRequestsWhenNoRequests() throws Exception {
        Mockito.when(itemRequestClient.getAllItemRequests(anyLong()))
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        Mockito.verify(itemRequestClient, times(1)).getAllItemRequests(1L);
    }

    @Test
    void addItemRequestWithoutUserIdHeader() throws Exception {
        ItemRequestDto validDto = new ItemRequestDto(null, "Need a power drill", null, null);

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getItemRequestsWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAllItemRequestsWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/requests/all"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getItemRequestByIdWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/requests/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void addItemRequestWithMinimalDescription() throws Exception {
        UserDto requestor = new UserDto(1L, "John Doe", "john@example.com");
        ItemRequestDto validDto = new ItemRequestDto(null, "A", null, null);
        ItemRequestDto responseDto = new ItemRequestDto(1L, "A", requestor, LocalDateTime.now());

        Mockito.when(itemRequestClient.addItemRequest(anyLong(), any(ItemRequestDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(responseDto));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated());

        Mockito.verify(itemRequestClient, times(1)).addItemRequest(1L, validDto);
    }

    @Test
    void addItemRequestWithMaxLengthDescription() throws Exception {
        UserDto requestor = new UserDto(1L, "John Doe", "john@example.com");
        String maxLengthDescription = "a".repeat(2000);
        ItemRequestDto validDto = new ItemRequestDto(null, maxLengthDescription, null, null);
        ItemRequestDto responseDto = new ItemRequestDto(1L, maxLengthDescription, requestor, LocalDateTime.now());

        Mockito.when(itemRequestClient.addItemRequest(anyLong(), any(ItemRequestDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(responseDto));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated());

        Mockito.verify(itemRequestClient, times(1)).addItemRequest(1L, validDto);
    }

    @Test
    void addItemRequestWithRequestorInResponse() throws Exception {
        UserDto requestor = new UserDto(1L, "John Doe", "john@example.com");
        ItemRequestDto validDto = new ItemRequestDto(null, "Need a power drill", null, null);
        ItemRequestDto responseDto = new ItemRequestDto(1L, "Need a power drill", requestor, LocalDateTime.now());

        Mockito.when(itemRequestClient.addItemRequest(anyLong(), any(ItemRequestDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(responseDto));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a power drill"))
                .andExpect(jsonPath("$.requestor.id").value(1L))
                .andExpect(jsonPath("$.requestor.name").value("John Doe"))
                .andExpect(jsonPath("$.requestor.email").value("john@example.com"));

        Mockito.verify(itemRequestClient, times(1)).addItemRequest(1L, validDto);
    }
}
