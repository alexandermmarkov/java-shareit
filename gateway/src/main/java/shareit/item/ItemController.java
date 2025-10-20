package shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import shareit.item.dto.CommentDto;
import shareit.item.dto.ItemDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> addItem(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                          @Valid @RequestBody ItemDto itemDto) {
        log.info("Creating item {}, userId={}", itemDto, userId);
        return itemClient.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                             @PathVariable @Positive Long itemId,
                                             @RequestBody ItemDto itemDto) {
        log.info("Updating item with id={} to {}, userId={}", itemId, itemDto, userId);
        return itemClient.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemWithDateById(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                                      @PathVariable @Positive Long itemId) {
        log.info("Getting item with id={}, userId={}", itemId, userId);
        return itemClient.getItemWithDateById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getItems(@RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("Getting items for the user with id={}", userId);
        return itemClient.getItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                              @RequestParam String text) {
        log.info("Searching items with description having '{}', userId={}", text, userId);
        if (text.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        return itemClient.searchItem(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                             @PathVariable @Positive Long itemId,
                                             @Valid @RequestBody CommentDto commentDto) {
        log.info("Adding comment '{}' for the item with id={}, userId={}", commentDto, itemId, userId);
        return itemClient.addComment(userId, itemId, commentDto);
    }
}
