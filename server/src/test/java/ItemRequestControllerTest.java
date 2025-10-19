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
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
@ContextConfiguration(classes = ShareItServer.class)
class ItemRequestControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mvc;

    private final UserDto userDto = new UserDto(1L, "Requestor", "requestor@example.com");
    private final UserDto ownerDto = new UserDto(2L, "Owner", "owner@example.com");

    private final ItemRequestDto itemRequestDto = new ItemRequestDto(
            null, "Need a drill", userDto, LocalDateTime.now()
    );

    private final ItemRequestDto savedItemRequestDto = new ItemRequestDto(
            1L, "Need a drill", userDto, LocalDateTime.now()
    );

    private final ItemResponseDto itemResponseDto = new ItemResponseDto(
            1L, "Drill", ownerDto
    );

    private final ItemRequestResponseDto itemRequestResponseDto = new ItemRequestResponseDto(
            1L, "Need a drill", LocalDateTime.now(), List.of(itemResponseDto)
    );

    private final ItemRequestResponseDto emptyItemRequestResponseDto = new ItemRequestResponseDto(
            1L, "Need a drill", LocalDateTime.now(), List.of()
    );

    @Test
    void addItemRequest() throws Exception {
        Mockito.when(itemRequestService.addItemRequest(anyLong(), any(ItemRequestDto.class)))
                .thenReturn(savedItemRequestDto);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(savedItemRequestDto.getId().intValue())))
                .andExpect(jsonPath("$.description", is(savedItemRequestDto.getDescription())))
                .andExpect(jsonPath("$.requestor.id", is(savedItemRequestDto.getRequestor().getId().intValue())));
    }

    @Test
    void getItemRequests() throws Exception {
        List<ItemRequestResponseDto> requests = List.of(itemRequestResponseDto);

        Mockito.when(itemRequestService.getItemRequests(anyLong()))
                .thenReturn(requests);

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestResponseDto.getId().intValue())))
                .andExpect(jsonPath("$[0].description", is(itemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].id", is(itemResponseDto.getId().intValue())))
                .andExpect(jsonPath("$[0].items[0].name", is(itemResponseDto.getName())));
    }

    @Test
    void getItemRequestsWhenNoRequests() throws Exception {
        Mockito.when(itemRequestService.getItemRequests(anyLong()))
                .thenReturn(List.of());

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllItemRequests() throws Exception {
        List<ItemRequestDto> requests = List.of(savedItemRequestDto);

        Mockito.when(itemRequestService.getAllItemRequests(anyLong()))
                .thenReturn(requests);

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(savedItemRequestDto.getId().intValue())))
                .andExpect(jsonPath("$[0].description", is(savedItemRequestDto.getDescription())))
                .andExpect(jsonPath("$[0].requestor.id", is(savedItemRequestDto.getRequestor().getId().intValue())));
    }

    @Test
    void getAllItemRequestsWhenNoOtherRequests() throws Exception {
        Mockito.when(itemRequestService.getAllItemRequests(anyLong()))
                .thenReturn(List.of());

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getItemRequestById() throws Exception {
        Mockito.when(itemRequestService.getItemRequestById(anyLong(), anyLong()))
                .thenReturn(itemRequestResponseDto);

        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestResponseDto.getId().intValue())))
                .andExpect(jsonPath("$.description", is(itemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id", is(itemResponseDto.getId().intValue())))
                .andExpect(jsonPath("$.items[0].name", is(itemResponseDto.getName())));
    }

    @Test
    void getItemRequestByIdWhenNoItems() throws Exception {
        Mockito.when(itemRequestService.getItemRequestById(anyLong(), anyLong()))
                .thenReturn(emptyItemRequestResponseDto);

        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(emptyItemRequestResponseDto.getId().intValue())))
                .andExpect(jsonPath("$.description", is(emptyItemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void addItemRequestWhenServiceThrowsNotFoundException() throws Exception {
        Mockito.when(itemRequestService.addItemRequest(anyLong(), any(ItemRequestDto.class)))
                .thenThrow(new NotFoundException("User not found"));

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 999L)
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getItemRequestsWhenServiceThrowsNotFoundException() throws Exception {
        Mockito.when(itemRequestService.getItemRequests(anyLong()))
                .thenThrow(new NotFoundException("User not found"));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 999L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllItemRequestsWhenServiceThrowsNotFoundException() throws Exception {
        Mockito.when(itemRequestService.getAllItemRequests(anyLong()))
                .thenThrow(new NotFoundException("User not found"));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 999L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getItemRequestByIdWhenServiceThrowsNotFoundException() throws Exception {
        Mockito.when(itemRequestService.getItemRequestById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Request not found"));

        mvc.perform(get("/requests/999")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void addItemRequestWithoutUserIdHeader() throws Exception {
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getItemRequestsWithoutUserIdHeader() throws Exception {
        mvc.perform(get("/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAllItemRequestsWithoutUserIdHeader() throws Exception {
        mvc.perform(get("/requests/all")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getItemRequestByIdWithoutUserIdHeader() throws Exception {
        mvc.perform(get("/requests/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getItemRequestByIdWhenUserIsNotRequestor() throws Exception {
        Mockito.when(itemRequestService.getItemRequestById(anyLong(), anyLong()))
                .thenReturn(itemRequestResponseDto);

        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 999L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestResponseDto.getId().intValue())));
    }

    @Test
    void addItemRequestWithMinimalData() throws Exception {
        ItemRequestDto minimalRequest = new ItemRequestDto(null, "Minimal request", null, null);
        ItemRequestDto savedMinimalRequest = new ItemRequestDto(2L, "Minimal request", userDto, LocalDateTime.now());

        Mockito.when(itemRequestService.addItemRequest(anyLong(), any(ItemRequestDto.class)))
                .thenReturn(savedMinimalRequest);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(minimalRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(savedMinimalRequest.getId().intValue())))
                .andExpect(jsonPath("$.description", is(savedMinimalRequest.getDescription())));
    }
}
