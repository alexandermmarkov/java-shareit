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
import shareit.item.ItemClient;
import shareit.item.ItemController;
import shareit.item.dto.CommentDto;
import shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@ContextConfiguration(classes = ShareItGateway.class)
class ItemControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemClient itemClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createItemWhenNameIsBlank() throws Exception {
        ItemDto invalidDto = new ItemDto(null, "   ", "Description", null, true, null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Название вещи не может быть пустым")));
    }

    @Test
    void createItemWhenDescriptionIsNull() throws Exception {
        ItemDto invalidDto = new ItemDto(null, "Name", null, null, true, null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Описание вещи не может быть пустым")));
    }

    @Test
    void createItemWhenAvailableIsNull() throws Exception {
        ItemDto invalidDto = new ItemDto(null, "Valid Name", "Valid description", null, null, null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Статус доступности вещи не может быть пустым")));
    }

    @Test
    void createCommentWhenTextIsBlank() throws Exception {
        CommentDto invalidDto = new CommentDto(null, "   ", "Author", LocalDateTime.now());

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Текст комментария не может быть пустым")));
    }

    @Test
    void createCommentWhenTextIsTooLong() throws Exception {
        String longText = "a".repeat(2001);
        CommentDto invalidDto = new CommentDto(null, longText, "Author", LocalDateTime.now());

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Комментарий не должен превышать 2000 символов")));
    }

    @Test
    void addItemWhenUserIdIsNegative() throws Exception {
        ItemDto validDto = new ItemDto(null, "Name", "Description", null, true, null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItemWhenUserIdIsZero() throws Exception {
        ItemDto validDto = new ItemDto(null, "Name", "Description", null, true, null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItemWhenUserIdIsNegative() throws Exception {
        ItemDto validDto = new ItemDto(null, "Updated Name", "Updated Description", null, false, null);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItemWhenItemIdIsNegative() throws Exception {
        ItemDto validDto = new ItemDto(null, "Updated Name", "Updated Description", null, false, null);

        mockMvc.perform(patch("/items/-1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItemWithDateByIdWhenUserIdIsNegative() throws Exception {
        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", -1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItemWithDateByIdWhenItemIdIsNegative() throws Exception {
        mockMvc.perform(get("/items/-1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItemsWhenUserIdIsNegative() throws Exception {
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", -1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchItemsWhenUserIdIsNegative() throws Exception {
        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", -1L)
                        .param("text", "drill"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCommentWhenUserIdIsNegative() throws Exception {
        CommentDto validDto = new CommentDto(null, "Comment", "Author", LocalDateTime.now());

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCommentWhenItemIdIsNegative() throws Exception {
        CommentDto validDto = new CommentDto(null, "Comment", "Author", LocalDateTime.now());

        mockMvc.perform(post("/items/-1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void addItemWithValidData() throws Exception {
        ItemDto validDto = new ItemDto(null, "Drill", "Powerful drill", null, true, null);
        ItemDto responseDto = new ItemDto(1L, "Drill", "Powerful drill", null, true, null);

        Mockito.when(itemClient.addItem(anyLong(), any(ItemDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(responseDto));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Drill"));

        Mockito.verify(itemClient, times(1)).addItem(1L, validDto);
    }

    @Test
    void updateItemWithValidData() throws Exception {
        ItemDto updateDto = new ItemDto(null, "Updated Drill", "Very powerful", null, false, null);
        ItemDto responseDto = new ItemDto(1L, "Updated Drill", "Very powerful", null, false, null);

        Mockito.when(itemClient.updateItem(anyLong(), anyLong(), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Drill"))
                .andExpect(jsonPath("$.available").value(false));

        Mockito.verify(itemClient, times(1)).updateItem(1L, 1L, updateDto);
    }

    @Test
    void getItemWithDateByIdWithValidIds() throws Exception {
        ItemDto responseDto = new ItemDto(1L, "Drill", "Powerful drill", null, true, null);

        Mockito.when(itemClient.getItemWithDateById(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Drill"));

        Mockito.verify(itemClient, times(1)).getItemWithDateById(1L, 1L);
    }

    @Test
    void getItemsWithValidUserId() throws Exception {
        List<ItemDto> responseList = List.of(
                new ItemDto(1L, "Drill", "Powerful drill", null, true, null),
                new ItemDto(2L, "Hammer", "Good hammer", null, true, null)
        );

        Mockito.when(itemClient.getItems(anyLong()))
                .thenReturn(ResponseEntity.ok(responseList));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        Mockito.verify(itemClient, times(1)).getItems(1L);
    }

    @Test
    void searchItemsWithValidText() throws Exception {
        List<ItemDto> responseList = List.of(
                new ItemDto(1L, "Power Drill", "Very powerful", null, true, null)
        );

        Mockito.when(itemClient.searchItem(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok(responseList));

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Power Drill"));

        Mockito.verify(itemClient, times(1)).searchItem(1L, "drill");
    }

    @Test
    void searchItemsWithBlankText() throws Exception {
        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        Mockito.verify(itemClient, never()).searchItem(anyLong(), anyString());
    }

    @Test
    void searchItemsWithEmptyText() throws Exception {
        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        Mockito.verify(itemClient, never()).searchItem(anyLong(), anyString());
    }

    @Test
    void addCommentWithValidData() throws Exception {
        CommentDto validDto = new CommentDto(null, "Great item!", "John", LocalDateTime.now());
        CommentDto responseDto = new CommentDto(1L, "Great item!", "John", LocalDateTime.now());

        Mockito.when(itemClient.addComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(responseDto));

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Great item!"));

        Mockito.verify(itemClient, times(1)).addComment(1L, 1L, validDto);
    }

    @Test
    void addItemWithoutUserIdHeader() throws Exception {
        ItemDto validDto = new ItemDto(null, "Name", "Description", null, true, null);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItemWithoutUserIdHeader() throws Exception {
        ItemDto validDto = new ItemDto(null, "Updated Name", "Updated Description", null, false, null);

        mockMvc.perform(patch("/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItemWithDateByIdWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/items/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItemsWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchItemsWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCommentWithoutUserIdHeader() throws Exception {
        CommentDto validDto = new CommentDto(null, "Great item!", "John", LocalDateTime.now());

        mockMvc.perform(post("/items/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchItemsWithoutTextParam() throws Exception {
        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCommentWithMaxLengthText() throws Exception {
        String maxLengthText = "a".repeat(2000);
        CommentDto validDto = new CommentDto(null, maxLengthText, "Author", LocalDateTime.now());
        CommentDto responseDto = new CommentDto(1L, maxLengthText, "Author", LocalDateTime.now());

        Mockito.when(itemClient.addComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(responseDto));

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated());

        Mockito.verify(itemClient, times(1)).addComment(1L, 1L, validDto);
    }

    @Test
    void addItemShouldCallClient() throws Exception {
        ItemDto requestDto = new ItemDto(null, "Drill", "Powerful drill", null, true, null);
        ItemDto responseDto = new ItemDto(1L, "Drill", "Powerful drill", null, true, null);

        Mockito.when(itemClient.addItem(anyLong(), any(ItemDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(responseDto));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.description").value("Powerful drill"))
                .andExpect(jsonPath("$.available").value(true));

        Mockito.verify(itemClient, times(1)).addItem(eq(1L), any(ItemDto.class));
    }

    @Test
    void updateItemShouldCallClient() throws Exception {
        ItemDto requestDto = new ItemDto(null, "Updated Drill", "Very powerful drill", null, false, null);
        ItemDto responseDto = new ItemDto(1L, "Updated Drill", "Very powerful drill", null, false, null);

        Mockito.when(itemClient.updateItem(anyLong(), anyLong(), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Drill"))
                .andExpect(jsonPath("$.available").value(false));

        Mockito.verify(itemClient, times(1)).updateItem(1L, 1L, requestDto);
    }

    @Test
    void getItemWithDateByIdShouldCallClient() throws Exception {
        ItemDto responseDto = new ItemDto(1L, "Drill", "Powerful drill", null, true, null);

        Mockito.when(itemClient.getItemWithDateById(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.available").value(true));

        Mockito.verify(itemClient, times(1)).getItemWithDateById(1L, 1L);
    }

    @Test
    void getItemsShouldCallClient() throws Exception {
        ItemDto item1 = new ItemDto(1L, "Drill", "Powerful drill", null, true, null);
        ItemDto item2 = new ItemDto(2L, "Hammer", "Good hammer", null, true, null);
        List<ItemDto> responseList = List.of(item1, item2);

        Mockito.when(itemClient.getItems(anyLong()))
                .thenReturn(ResponseEntity.ok(responseList));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Drill"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Hammer"));

        Mockito.verify(itemClient, times(1)).getItems(1L);
    }

    @Test
    void getItemsEmptyResultShouldReturnEmptyList() throws Exception {
        List<ItemDto> responseList = List.of();

        Mockito.when(itemClient.getItems(anyLong()))
                .thenReturn(ResponseEntity.ok(responseList));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        Mockito.verify(itemClient, times(1)).getItems(1L);
    }

    @Test
    void searchItemsShouldCallClient() throws Exception {
        ItemDto item1 = new ItemDto(1L, "Power Drill", "Very powerful", null, true, null);
        List<ItemDto> responseList = List.of(item1);

        Mockito.when(itemClient.searchItem(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok(responseList));

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Power Drill"));

        Mockito.verify(itemClient, times(1)).searchItem(1L, "drill");
    }

    @Test
    void searchItemsEmptyResultShouldReturnEmptyList() throws Exception {
        List<ItemDto> responseList = List.of();

        Mockito.when(itemClient.searchItem(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok(responseList));

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        Mockito.verify(itemClient, times(1)).searchItem(1L, "nonexistent");
    }

    @Test
    void addComment_ShouldCallClient() throws Exception {
        CommentDto requestDto = new CommentDto(null, "Great item!", null, null);
        CommentDto responseDto = new CommentDto(1L, "Great item!", "John Doe", LocalDateTime.now());

        Mockito.when(itemClient.addComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(responseDto));

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Great item!"))
                .andExpect(jsonPath("$.authorName").value("John Doe"));

        Mockito.verify(itemClient, times(1)).addComment(1L, 1L, requestDto);
    }

    @Test
    void updateItem_PartialUpdate_ShouldCallClient() throws Exception {
        ItemDto requestDto = new ItemDto(null, "New Name", null, null, null, null);
        ItemDto responseDto = new ItemDto(1L, "New Name", "Original Description", null, true, null);

        Mockito.when(itemClient.updateItem(anyLong(), anyLong(), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.description").value("Original Description"));

        Mockito.verify(itemClient, times(1)).updateItem(1L, 1L, requestDto);
    }

    @Test
    void getItems_WithPagination_ShouldCallClient() throws Exception {
        ItemDto item1 = new ItemDto(1L, "Drill", "Powerful drill", null, true, null);
        ItemDto item2 = new ItemDto(2L, "Hammer", "Good hammer", null, true, null);
        List<ItemDto> responseList = List.of(item1, item2);

        Mockito.when(itemClient.getItems(anyLong()))
                .thenReturn(ResponseEntity.ok(responseList));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        Mockito.verify(itemClient, times(1)).getItems(1L);
    }

    @Test
    void searchItemsWithPaginationShouldCallClient() throws Exception {
        ItemDto item1 = new ItemDto(1L, "Power Drill", "Very powerful", null, true, null);
        List<ItemDto> responseList = List.of(item1);

        Mockito.when(itemClient.searchItem(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok(responseList));

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "drill")
                        .param("from", "5")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        Mockito.verify(itemClient, times(1)).searchItem(1L, "drill");
    }

    @Test
    void addItem_WithRequestShouldCallClient() throws Exception {
        ItemDto requestDto = new ItemDto(null, "Drill", "Powerful drill", null, true, 100L);
        ItemDto responseDto = new ItemDto(1L, "Drill", "Powerful drill", null, true, 100L);

        Mockito.when(itemClient.addItem(anyLong(), any(ItemDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(responseDto));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestId").value(100L));

        Mockito.verify(itemClient, times(1)).addItem(eq(1L), any(ItemDto.class));
    }

    @Test
    void getItemWithDateByIdForDifferentUserShouldCallClient() throws Exception {
        ItemDto responseDto = new ItemDto(1L, "Drill", "Powerful drill", null, true, null);

        Mockito.when(itemClient.getItemWithDateById(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        Mockito.verify(itemClient, times(1)).getItemWithDateById(999L, 1L);
    }

    @Test
    void addCommentWithMaxLengthTextShouldCallClient() throws Exception {
        String maxLengthText = "a".repeat(2000);
        CommentDto requestDto = new CommentDto(null, maxLengthText, null, null);
        CommentDto responseDto = new CommentDto(1L, maxLengthText, "John Doe", LocalDateTime.now());

        Mockito.when(itemClient.addComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(responseDto));

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());

        Mockito.verify(itemClient, times(1)).addComment(1L, 1L, requestDto);
    }
}
