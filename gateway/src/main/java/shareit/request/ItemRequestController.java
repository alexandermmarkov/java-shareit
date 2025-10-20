package shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import shareit.request.dto.ItemRequestDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/requests")
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> addItemRequest(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                                 @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Adding item request {}, userId={}", itemRequestDto, userId);
        return itemRequestClient.addItemRequest(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getItemRequests(@RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("Getting item requests for the user with id={}", userId);
        return itemRequestClient.getItemRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemRequests(@RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("Getting all item requests, userId={}", userId);
        return itemRequestClient.getAllItemRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequestById(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                                     @PathVariable @Positive Long requestId) {
        log.info("Getting the item request with id={}, userId={}", requestId, userId);
        return itemRequestClient.getItemRequestById(userId, requestId);
    }
}
