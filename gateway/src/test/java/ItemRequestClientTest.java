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
import shareit.request.ItemRequestClient;
import shareit.request.dto.ItemRequestDto;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestClientTest {

    @Mock
    private RestTemplate restTemplate;

    private ItemRequestClient itemRequestClient;

    @BeforeEach
    void setUp() {
        itemRequestClient = new ItemRequestClient("http://localhost:8080", new RestTemplateBuilder());
        setRestTemplateField(itemRequestClient, restTemplate);
    }

    @Test
    void addItemRequestShouldCallPostWithCorrectParameters() {
        long userId = 1L;
        ItemRequestDto itemRequestDto = createItemRequestDto();

        try {
            itemRequestClient.addItemRequest(userId, itemRequestDto);
        } catch (Exception ignored) {

        }

        verify(restTemplate).exchange(
                eq(""),
                eq(HttpMethod.POST),
                argThat(httpEntity ->
                        httpEntity != null &&
                                httpEntity.getHeaders().containsKey("X-Sharer-User-Id") &&
                                httpEntity.getBody() == itemRequestDto
                ),
                eq(Object.class)
        );
    }

    @Test
    void getItemRequestsShouldCallGetWithCorrectParameters() {
        long userId = 1L;

        try {
            itemRequestClient.getItemRequests(userId);
        } catch (Exception ignored) {

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
    void getAllItemRequestsShouldCallGetWithCorrectParameters() {
        long userId = 1L;

        try {
            itemRequestClient.getAllItemRequests(userId);
        } catch (Exception ignored) {

        }

        verify(restTemplate).exchange(
                eq("/all"),
                eq(HttpMethod.GET),
                argThat(httpEntity ->
                        httpEntity != null &&
                                httpEntity.getHeaders().containsKey("X-Sharer-User-Id")
                ),
                eq(Object.class)
        );
    }

    @Test
    void getItemRequestByIdShouldCallGetWithCorrectParameters() {
        long userId = 1L;
        long requestId = 1L;

        try {
            itemRequestClient.getItemRequestById(userId, requestId);
        } catch (Exception ignored) {

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
    void addItemRequestWithHttpErrorShouldReturnErrorResponse() {
        long userId = 1L;
        ItemRequestDto itemRequestDto = createItemRequestDto();

        HttpStatusCodeException exception = Mockito.mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getResponseBodyAsByteArray()).thenReturn("Invalid request".getBytes());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> result = itemRequestClient.addItemRequest(userId, itemRequestDto);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getItemRequestByIdWithHttpErrorShouldReturnErrorResponse() {
        long userId = 1L;
        long requestId = 999L;

        HttpStatusCodeException exception = Mockito.mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(exception.getResponseBodyAsByteArray()).thenReturn("Request not found".getBytes());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> result = itemRequestClient.getItemRequestById(userId, requestId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private ItemRequestDto createItemRequestDto() {
        return new ItemRequestDto(
                null,
                "Test description",
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
