import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import shareit.client.BaseClient;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseClientTest {

    @Mock
    private RestTemplate restTemplate;

    private BaseClient baseClient;

    @BeforeEach
    void setUp() {
        baseClient = new BaseClient(restTemplate);
    }

    @Test
    void getWithoutParametersShouldCallRestTemplate() {
        String path = "/test";
        ResponseEntity<Object> responseEntity = ResponseEntity.ok("test response");

        when(restTemplate.exchange(eq(path), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = baseClient.get(path);

        assertThat(result).isEqualTo(responseEntity);
        verify(restTemplate).exchange(eq(path), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void getWithUserIdShouldCallRestTemplate() {
        String path = "/test";
        long userId = 1L;
        ResponseEntity<Object> responseEntity = ResponseEntity.ok("test response");

        when(restTemplate.exchange(eq(path), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = baseClient.get(path, userId);

        assertThat(result).isEqualTo(responseEntity);
        verify(restTemplate).exchange(eq(path), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void getWithUserIdAndParametersShouldCallRestTemplate() {
        String path = "/test";
        long userId = 1L;
        Map<String, Object> parameters = Map.of("param1", "value1", "param2", "value2");
        ResponseEntity<Object> responseEntity = ResponseEntity.ok("test response");

        when(restTemplate.exchange(eq(path), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), eq(parameters)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = baseClient.get(path, userId, parameters);

        assertThat(result).isEqualTo(responseEntity);
        verify(restTemplate).exchange(eq(path), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), eq(parameters));
    }

    @Test
    void postWithBodyShouldCallRestTemplate() {
        String path = "/test";
        Object requestBody = new Object();
        ResponseEntity<Object> responseEntity = ResponseEntity.ok("test response");

        when(restTemplate.exchange(eq(path), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = baseClient.post(path, requestBody);

        assertThat(result).isEqualTo(responseEntity);
        verify(restTemplate).exchange(eq(path), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void postWithUserIdAndBodyShouldCallRestTemplate() {
        String path = "/test";
        long userId = 1L;
        Object requestBody = new Object();
        ResponseEntity<Object> responseEntity = ResponseEntity.ok("test response");

        when(restTemplate.exchange(eq(path), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = baseClient.post(path, userId, requestBody);

        assertThat(result).isEqualTo(responseEntity);
        verify(restTemplate).exchange(eq(path), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void postWithUserIdParametersAndBodyShouldCallRestTemplate() {
        String path = "/test";
        long userId = 1L;
        Map<String, Object> parameters = Map.of("param1", "value1");
        Object requestBody = new Object();
        ResponseEntity<Object> responseEntity = ResponseEntity.ok("test response");

        when(restTemplate.exchange(eq(path), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class), eq(parameters)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = baseClient.post(path, userId, parameters, requestBody);

        assertThat(result).isEqualTo(responseEntity);
        verify(restTemplate).exchange(eq(path), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class), eq(parameters));
    }

    @Test
    void putWithUserIdAndBodyShouldCallRestTemplate() {
        String path = "/test";
        long userId = 1L;
        Object requestBody = new Object();
        ResponseEntity<Object> responseEntity = ResponseEntity.ok("test response");

        when(restTemplate.exchange(eq(path), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = baseClient.put(path, userId, requestBody);

        assertThat(result).isEqualTo(responseEntity);
        verify(restTemplate).exchange(eq(path), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void patchWithBodyShouldCallRestTemplate() {
        String path = "/test";
        Object requestBody = new Object();
        ResponseEntity<Object> responseEntity = ResponseEntity.ok("test response");

        when(restTemplate.exchange(eq(path), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = baseClient.patch(path, requestBody);

        assertThat(result).isEqualTo(responseEntity);
        verify(restTemplate).exchange(eq(path), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void patchWithUserIdShouldCallRestTemplate() {
        String path = "/test";
        long userId = 1L;
        ResponseEntity<Object> responseEntity = ResponseEntity.ok("test response");

        when(restTemplate.exchange(eq(path), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = baseClient.patch(path, userId);

        assertThat(result).isEqualTo(responseEntity);
        verify(restTemplate).exchange(eq(path), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void patchWithUserIdAndBodyShouldCallRestTemplate() {
        String path = "/test";
        long userId = 1L;
        Object requestBody = new Object();
        ResponseEntity<Object> responseEntity = ResponseEntity.ok("test response");

        when(restTemplate.exchange(eq(path), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = baseClient.patch(path, userId, requestBody);

        assertThat(result).isEqualTo(responseEntity);
        verify(restTemplate).exchange(eq(path), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void deleteWithoutParametersShouldCallRestTemplate() {
        String path = "/test";
        ResponseEntity<Object> responseEntity = ResponseEntity.noContent().build();

        when(restTemplate.exchange(eq(path), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = baseClient.delete(path);

        assertThat(result).isEqualTo(responseEntity);
        verify(restTemplate).exchange(eq(path), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void deleteWithUserIdShouldCallRestTemplate() {
        String path = "/test";
        long userId = 1L;
        ResponseEntity<Object> responseEntity = ResponseEntity.noContent().build();

        when(restTemplate.exchange(eq(path), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = baseClient.delete(path, userId);

        assertThat(result).isEqualTo(responseEntity);
        verify(restTemplate).exchange(eq(path), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void makeAndSendRequestWithHttpStatusCodeExceptionShouldReturnErrorResponse() {
        String path = "/test";
        HttpMethod method = HttpMethod.GET;
        long userId = 1L;

        HttpStatusCodeException exception = Mockito.mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getResponseBodyAsByteArray()).thenReturn("Error message".getBytes());

        when(restTemplate.exchange(eq(path), eq(method), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> result = baseClient.get(path, userId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(restTemplate).exchange(eq(path), eq(method), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void prepareGatewayResponseWith2xxResponseShouldReturnSameResponse() {
        ResponseEntity<Object> originalResponse = ResponseEntity.ok("Success");

        ResponseEntity<Object> result = invokePrepareGatewayResponse(originalResponse);

        assertThat(result).isEqualTo(originalResponse);
    }

    @Test
    void prepareGatewayResponseWith4xxResponseAndBodyShouldReturnResponseWithBody() {
        ResponseEntity<Object> originalResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error body");

        ResponseEntity<Object> result = invokePrepareGatewayResponse(originalResponse);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isEqualTo("Error body");
    }

    @Test
    void prepareGatewayResponseWith4xxResponseWithoutBodyShouldReturnResponseWithoutBody() {
        ResponseEntity<Object> originalResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        ResponseEntity<Object> result = invokePrepareGatewayResponse(originalResponse);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNull();
    }

    private ResponseEntity<Object> invokePrepareGatewayResponse(ResponseEntity<Object> response) {
        try {
            Method method = BaseClient.class.getDeclaredMethod("prepareGatewayResponse", ResponseEntity.class);
            method.setAccessible(true);
            return (ResponseEntity<Object>) method.invoke(baseClient, response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke private method", e);
        }
    }
}
