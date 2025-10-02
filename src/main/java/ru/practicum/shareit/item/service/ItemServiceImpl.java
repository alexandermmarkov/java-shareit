package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        log.debug("addItem(userId={}, itemDto={})", userId, itemDto);

        User user = getUserIfExists(userId);
        Item item = repository.save(itemMapper.toItem(itemDto, user));

        return itemMapper.toItemDto(item);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.debug("updateItem(userId={}, itemId={}, itemDto={})", userId, itemDto, itemDto);

        User user = getUserIfExists(userId);
        getItemById(userId, itemId);

        itemDto.setId(itemId);
        Item item = repository.save(itemMapper.toItem(itemDto, user));

        return itemMapper.toItemDto(item);
    }

    @Override
    public ItemWithDateDto getItemById(Long userId, Long itemId) {
        log.debug("getItemById(itemId={}", itemId);

        Item item = repository.findById(itemId).orElseThrow(() -> {
            log.debug(repository.findAll().stream().map(Item::getId).toList().toString());
            return new NotFoundException("Вещь с ID = '" + itemId + "' не найдена");
        });

        List<CommentDto> comments = commentRepository.findAllByItemId(item.getId())
                .stream()
                .map(commentMapper::toCommentDto)
                .toList();

        Optional<Booking> lastBooking = bookingRepository
                .findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(item.getId(), LocalDateTime.now(),
                        BookingStatus.APPROVED);
        Optional<Booking> nextBooking = bookingRepository
                .findFirstByItemIdAndStartAfterOrderByStartAsc(item.getId(), LocalDateTime.now());

        boolean isOwner = userId.equals(item.getOwner().getId());
        return itemMapper.toItemWithDateDto(
                item,
                comments,
                isOwner ? lastBooking.map(Booking::getEnd).orElse(null) : null,
                isOwner ? nextBooking.map(Booking::getStart).orElse(null) : null
        );
    }

    @Override
    public List<ItemWithDateDto> getItems(Long userId) {
        log.debug("getItems(userId={})", userId);

        List<Item> items = repository.findByOwnerId(userId);
        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();

        Map<Long, LocalDateTime> lastBookingsMap = getLastBookingsMap(itemIds);
        Map<Long, LocalDateTime> nextBookingsMap = getNextBookingsMap(itemIds);
        Map<Long, List<CommentDto>> commentsMap = getCommentsMap(itemIds);

        return items.stream()
                .map(item -> itemMapper.toItemWithDateDto(
                        item,
                        commentsMap.getOrDefault(item.getId(), List.of()),
                        lastBookingsMap.get(item.getId()),
                        nextBookingsMap.get(item.getId())
                ))
                .toList();
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        log.debug("searchItem(text={}", text);

        if ((text == null) || (text.isBlank())) {
            return Collections.emptyList();
        }
        return repository.findByTextIgnoreCase(text)
                .stream()
                .map(itemMapper::toItemDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        log.debug("addComment(userId={}, itemId={}, commentDto={}", userId, itemId, commentDto);

        User user = getUserIfExists(userId);
        getItemById(userId, itemId);

        Optional<Booking> booking = bookingRepository
                .findByBookerIdAndItemIdAndEndIsBeforeAndStatus(userId, itemId, LocalDateTime.now(),
                        BookingStatus.APPROVED);
        if (booking.isEmpty()) {
            throw new ValidationException("Данная вещь не была забронирована пользователем " +
                    "или срок действия брони ещё не истёк");
        }
        commentDto.setAuthorName(user.getName());
        Comment comment = commentRepository.save(commentMapper.toComment(commentDto, user, booking.get().getItem()));

        return commentMapper.toCommentDto(comment);
    }

    public User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.debug(userRepository.findAll().stream().map(User::getId).toList().toString());
                    return new NotFoundException("Пользователь с ID = '" + userId + "' не найден");
                });
    }

    private Map<Long, LocalDateTime> getLastBookingsMap(List<Long> itemIds) {
        return bookingRepository.findLastBookingsForItems(itemIds,
                        LocalDateTime.now(), BookingStatus.APPROVED)
                .stream()
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        Booking::getEnd
                ));
    }

    private Map<Long, LocalDateTime> getNextBookingsMap(List<Long> itemIds) {
        return bookingRepository.findNextBookingsForItems(itemIds, LocalDateTime.now())
                .stream()
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        Booking::getStart
                ));
    }

    private Map<Long, List<CommentDto>> getCommentsMap(List<Long> itemIds) {
        return commentRepository.findAllByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId(),
                        Collectors.mapping(commentMapper::toCommentDto, Collectors.toList())
                ));
    }
}
