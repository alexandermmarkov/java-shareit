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
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@ContextConfiguration(classes = ShareItServer.class)
class ItemControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemService itemService;

    @Autowired
    private MockMvc mvc;

    private final UserDto userDto = new UserDto(1L, "Owner", "owner@example.com");
    private final ItemDto itemDto = new ItemDto(1L, "Drill", "Powerful drill", userDto, true, null);
    private final ItemWithDateDto itemWithDateDto = new ItemWithDateDto(
            1L, "Drill", "Powerful drill", true, null,
            LocalDateTime.of(2024, 1, 1, 10, 0, 0),
            LocalDateTime.of(2024, 1, 2, 10, 0, 0),
            List.of()
    );
    private final CommentDto commentDto = new CommentDto(
            1L, "Great item!", "Booker",
            LocalDateTime.of(2024, 1, 3, 10, 0, 0)
    );

    @Test
    void addItem() throws Exception {
        Mockito.when(itemService.addItem(anyLong(), any(ItemDto.class)))
                .thenReturn(itemDto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
    }

    @Test
    void updateItem() throws Exception {
        ItemDto updatedItemDto = new ItemDto(1L, "Updated Drill", "Very powerful drill", userDto, true, null);

        Mockito.when(itemService.updateItem(anyLong(), anyLong(), any(ItemDto.class)))
                .thenReturn(updatedItemDto);

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(updatedItemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedItemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updatedItemDto.getName())))
                .andExpect(jsonPath("$.description", is(updatedItemDto.getDescription())));
    }

    @Test
    void getItemWithDateById() throws Exception {
        Mockito.when(itemService.getItemWithDateById(anyLong(), anyLong()))
                .thenReturn(itemWithDateDto);

        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemWithDateDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemWithDateDto.getName())))
                .andExpect(jsonPath("$.lastBooking", notNullValue()))
                .andExpect(jsonPath("$.nextBooking", notNullValue()));
    }

    @Test
    void getItems() throws Exception {
        List<ItemWithDateDto> items = List.of(itemWithDateDto);

        Mockito.when(itemService.getItems(anyLong()))
                .thenReturn(items);

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemWithDateDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemWithDateDto.getName())))
                .andExpect(jsonPath("$[0].available", is(itemWithDateDto.getAvailable())));
    }

    @Test
    void searchItems() throws Exception {
        List<ItemDto> items = List.of(itemDto);

        Mockito.when(itemService.searchItem(anyString()))
                .thenReturn(items);

        mvc.perform(get("/items/search")
                        .param("text", "drill")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())));
    }

    @Test
    void searchItems_WithEmptyText() throws Exception {
        Mockito.when(itemService.searchItem(anyString()))
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/items/search")
                        .param("text", "")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void addComment() throws Exception {
        Mockito.when(itemService.addComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenReturn(commentDto);

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())));
    }

    @Test
    void addItemWhenServiceThrowsNotFoundException() throws Exception {
        Mockito.when(itemService.addItem(anyLong(), any(ItemDto.class)))
                .thenThrow(new NotFoundException("User not found"));

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateItemWhenServiceThrowsNotFoundException() throws Exception {
        Mockito.when(itemService.updateItem(anyLong(), anyLong(), any(ItemDto.class)))
                .thenThrow(new NotFoundException("Item not found"));

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getItemWithDateByIdWhenServiceThrowsNotFoundException() throws Exception {
        Mockito.when(itemService.getItemWithDateById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Item not found"));

        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void addCommentWhenServiceThrowsValidationException() throws Exception {
        Mockito.when(itemService.addComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenThrow(new ValidationException("User hasn't booked this item"));

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCommentWhenServiceThrowsNotFoundException() throws Exception {
        Mockito.when(itemService.addComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenThrow(new NotFoundException("Item not found"));

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void addItemWithoutUserIdHeader() throws Exception {
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateItemWithoutUserIdHeader() throws Exception {
        mvc.perform(patch("/items/1")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getItemWithDateByIdWithoutUserIdHeader() throws Exception {
        mvc.perform(get("/items/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getItemsWithoutUserIdHeader() throws Exception {
        mvc.perform(get("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void addCommentWithoutUserIdHeader() throws Exception {
        mvc.perform(post("/items/1/comment")
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void searchItemsWithoutTextParam() throws Exception {
        Mockito.when(itemService.searchItem(isNull()))
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/items/search")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error")
                        .value(containsString("Required request parameter 'text' for method parameter type String is not present")));
    }

    @Test
    void getItemsWhenUserHasNoItems() throws Exception {
        Mockito.when(itemService.getItems(anyLong()))
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 999L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getItemWithDateDtoWithComments() throws Exception {
        ItemWithDateDto itemWithComments = new ItemWithDateDto(
                1L, "Drill", "Powerful drill", true, null,
                LocalDateTime.of(2024, 1, 1, 10, 0, 0),
                LocalDateTime.of(2024, 1, 2, 10, 0, 0),
                List.of(commentDto)
        );

        Mockito.when(itemService.getItemWithDateById(anyLong(), anyLong()))
                .thenReturn(itemWithComments);

        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].text", is(commentDto.getText())))
                .andExpect(jsonPath("$.comments[0].authorName", is(commentDto.getAuthorName())));
    }
}
