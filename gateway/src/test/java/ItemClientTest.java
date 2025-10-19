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
import shareit.item.ItemClient;
import shareit.item.dto.CommentDto;
import shareit.item.dto.ItemDto;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemClientTest {

    @Mock
    private RestTemplate restTemplate;

    private ItemClient itemClient;

    @BeforeEach
    void setUp() {
        itemClient = new ItemClient("http://localhost:8080", new RestTemplateBuilder());
        setRestTemplateField(itemClient, restTemplate);
    }

    @Test
    void addItemShouldCallPostWithCorrectParameters() {
        long userId = 1L;
        ItemDto itemDto = createItemDto();

        try {
            itemClient.addItem(userId, itemDto);
        } catch (Exception e) {

        }

        verify(restTemplate).exchange(
                eq(""),
                eq(HttpMethod.POST),
                argThat(httpEntity ->
                        httpEntity != null &&
                                httpEntity.getHeaders().containsKey("X-Sharer-User-Id") &&
                                httpEntity.getBody() == itemDto
                ),
                eq(Object.class)
        );
    }

    @Test
    void updateItemShouldCallPatchWithCorrectParameters() {
        long userId = 1L;
        long itemId = 1L;
        ItemDto itemDto = createItemDto();

        try {
            itemClient.updateItem(userId, itemId, itemDto);
        } catch (Exception e) {

        }

        verify(restTemplate).exchange(
                eq("/1"),
                eq(HttpMethod.PATCH),
                argThat(httpEntity ->
                        httpEntity != null &&
                                httpEntity.getHeaders().containsKey("X-Sharer-User-Id") &&
                                httpEntity.getBody() == itemDto
                ),
                eq(Object.class)
        );
    }

    @Test
    void getItemWithDateByIdShouldCallGetWithCorrectParameters() {
        long userId = 1L;
        long itemId = 1L;

        try {
            itemClient.getItemWithDateById(userId, itemId);
        } catch (Exception e) {

        }

        verify(restTemplate).exchange(
                eq("/1"),
                eq(HttpMethod.GET),
                argThat(httpEntity ->
                        httpEntity != null &&
                                httpEntity.getHeaders().containsKey("X-Sharer-User-Id")
                ),
                eq(Object.class)
        );
    }

    @Test
    void getItemsShouldCallGetWithCorrectParameters() {
        long userId = 1L;

        try {
            itemClient.getItems(userId);
        } catch (Exception e) {

        }

        verify(restTemplate).exchange(
                eq(""),
                eq(HttpMethod.GET),
                argThat(httpEntity ->
                        httpEntity != null &&
                                httpEntity.getHeaders().containsKey("X-Sharer-User-Id")
                ),
                eq(Object.class)
        );
    }

    @Test
    void searchItemShouldCallGetWithCorrectParameters() {
        long userId = 1L;
        String text = "test";

        try {
            itemClient.searchItem(userId, text);
        } catch (Exception e) {

        }

        verify(restTemplate).exchange(
                eq("/search?text={text}"),
                eq(HttpMethod.GET),
                argThat(httpEntity ->
                        httpEntity != null &&
                                httpEntity.getHeaders().containsKey("X-Sharer-User-Id")
                ),
                eq(Object.class),
                eq(Map.of("text", "test"))
        );
    }

    @Test
    void searchItemWithEmptyTextShouldCallGetWithEmptyText() {
        long userId = 1L;
        String text = "";

        try {
            itemClient.searchItem(userId, text);
        } catch (Exception e) {

        }

        verify(restTemplate).exchange(
                eq("/search?text={text}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                eq(Map.of("text", ""))
        );
    }

    @Test
    void addCommentShouldCallPostWithCorrectParameters() {
        long userId = 1L;
        long itemId = 1L;
        CommentDto commentDto = createCommentDto();

        try {
            itemClient.addComment(userId, itemId, commentDto);
        } catch (Exception e) {

        }

        verify(restTemplate).exchange(
                eq("/1/comment"),
                eq(HttpMethod.POST),
                argThat(httpEntity ->
                        httpEntity != null &&
                                httpEntity.getHeaders().containsKey("X-Sharer-User-Id") &&
                                httpEntity.getBody() == commentDto
                ),
                eq(Object.class)
        );
    }

    @Test
    void constructorShouldInitializeClient() {
        String serverUrl = "http://localhost:8080";
        RestTemplateBuilder builder = new RestTemplateBuilder();

        ItemClient client = new ItemClient(serverUrl, builder);

        assertThat(client).isNotNull();
    }

    @Test
    void addItemWithHttpErrorShouldReturnErrorResponse() {
        long userId = 1L;
        ItemDto itemDto = createItemDto();

        HttpStatusCodeException exception = Mockito.mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getResponseBodyAsByteArray()).thenReturn("Error message".getBytes());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> result = itemClient.addItem(userId, itemDto);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(restTemplate).exchange(eq(""), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void updateItemWithHttpErrorShouldReturnErrorResponse() {
        long userId = 1L;
        long itemId = 1L;
        ItemDto itemDto = createItemDto();

        HttpStatusCodeException exception = Mockito.mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(exception.getResponseBodyAsByteArray()).thenReturn("Not found".getBytes());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> result = itemClient.updateItem(userId, itemId, itemDto);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(restTemplate).exchange(eq("/1"), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void getItemWithDateByIdWithHttpErrorShouldReturnErrorResponse() {
        long userId = 1L;
        long itemId = 1L;

        HttpStatusCodeException exception = Mockito.mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(exception.getResponseBodyAsByteArray()).thenReturn("Not found".getBytes());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> result = itemClient.getItemWithDateById(userId, itemId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(restTemplate).exchange(eq("/1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void getItemsWithHttpErrorShouldReturnErrorResponse() {
        long userId = 1L;

        HttpStatusCodeException exception = Mockito.mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        when(exception.getResponseBodyAsByteArray()).thenReturn("Server error".getBytes());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> result = itemClient.getItems(userId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(restTemplate).exchange(eq(""), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void searchItemWithHttpErrorShouldReturnErrorResponse() {
        long userId = 1L;
        String text = "test";

        HttpStatusCodeException exception = Mockito.mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getResponseBodyAsByteArray()).thenReturn("Bad request".getBytes());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), anyMap()))
                .thenThrow(exception);

        ResponseEntity<Object> result = itemClient.searchItem(userId, text);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(restTemplate).exchange(eq("/search?text={text}"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), eq(Map.of("text", "test")));
    }

    @Test
    void addCommentWithHttpErrorShouldReturnErrorResponse() {
        long userId = 1L;
        long itemId = 1L;
        CommentDto commentDto = createCommentDto();

        HttpStatusCodeException exception = Mockito.mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getResponseBodyAsByteArray()).thenReturn("Invalid comment".getBytes());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> result = itemClient.addComment(userId, itemId, commentDto);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(restTemplate).exchange(eq("/1/comment"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void searchItemWithSpecialCharactersShouldCallGetWithEncodedText() {
        long userId = 1L;
        String text = "test&search";

        try {
            itemClient.searchItem(userId, text);
        } catch (Exception e) {

        }

        verify(restTemplate).exchange(
                eq("/search?text={text}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                eq(Map.of("text", "test&search"))
        );
    }

    @Test
    void addCommentWithDifferentUserAndItemShouldCallPostWithCorrectPath() {
        long userId = 999L;
        long itemId = 888L;
        CommentDto commentDto = createCommentDto();

        try {
            itemClient.addComment(userId, itemId, commentDto);
        } catch (Exception e) {

        }

        verify(restTemplate).exchange(
                eq("/888/comment"),
                eq(HttpMethod.POST),
                argThat(httpEntity ->
                        httpEntity != null &&
                                httpEntity.getHeaders().containsKey("X-Sharer-User-Id")
                ),
                eq(Object.class)
        );
    }

    @Test
    void updateItemWithDifferentIdsShouldCallPatchWithCorrectPath() {
        long userId = 999L;
        long itemId = 888L;
        ItemDto itemDto = createItemDto();

        try {
            itemClient.updateItem(userId, itemId, itemDto);
        } catch (Exception e) {

        }

        verify(restTemplate).exchange(
                eq("/888"),
                eq(HttpMethod.PATCH),
                argThat(httpEntity ->
                        httpEntity != null &&
                                httpEntity.getHeaders().containsKey("X-Sharer-User-Id")
                ),
                eq(Object.class)
        );
    }

    private ItemDto createItemDto() {
        return new ItemDto(
                null,
                "Test Item",
                "Test Description",
                null,
                true,
                null
        );
    }

    private CommentDto createCommentDto() {
        return new CommentDto(
                null,
                "Test comment",
                null,
                null
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

