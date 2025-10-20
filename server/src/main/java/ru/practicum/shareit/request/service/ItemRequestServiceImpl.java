package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository repository;
    private final ItemRequestMapper itemRequestMapper;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto addItemRequest(Long userId, ItemRequestDto itemRequestDto) {
        log.debug("addItemRequest(userId={}, itemRequestDto={})", userId, itemRequestDto);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.debug(userRepository.findAll().stream().map(User::getId).toList().toString());
                    return new NotFoundException("Пользователь с ID = '" + userId + "' не найден");
                });
        ItemRequest itemRequest = repository.save(itemRequestMapper.toItemRequest(itemRequestDto, user));

        return itemRequestMapper.toItemRequestDto(itemRequest);
    }

    @Override
    public List<ItemRequestResponseDto> getItemRequests(Long userId) {
        log.debug("getItemRequests(userId={})", userId);

        List<ItemRequest> itemRequests = repository.findByRequestorIdOrderByCreatedDesc(userId);
        if (itemRequests.isEmpty()) {
            return List.of();
        }

        List<Long> requestIds = itemRequests.stream()
                .map(ItemRequest::getId)
                .toList();

        Map<Long, List<ItemResponseDto>> repliesMap = getRepliesMap(requestIds);

        return itemRequests.stream()
                .map(itemRequest -> itemRequestMapper.toItemRequestResponseDto(
                        itemRequest,
                        repliesMap.getOrDefault(itemRequest.getId(), List.of())
                ))
                .toList();
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Long userId) {
        log.debug("getAllItemRequests(userId={})", userId);

        List<ItemRequest> itemRequests = repository.findByRequestorIdNotOrderByCreatedDesc(userId);
        if (itemRequests.isEmpty()) {
            return List.of();
        }

        return itemRequests.stream()
                .map(itemRequestMapper::toItemRequestDto)
                .toList();
    }

    @Override
    public ItemRequestResponseDto getItemRequestById(Long userId, Long requestId) {
        log.debug("getItemRequestById(userId={}, requestId={})", userId, requestId);

        ItemRequest itemRequest = getRequestIfExists(requestId);
        List<ItemResponseDto> replies = itemRepository.findAllByRequestId(requestId)
                .stream()
                .map(itemRequestMapper::toItemResponse)
                .toList();

        return itemRequestMapper.toItemRequestResponseDto(itemRequest, replies);
    }

    private Map<Long, List<ItemResponseDto>> getRepliesMap(List<Long> requestIds) {
        return itemRepository.findAllByRequestIdIn(requestIds)
                .stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(itemRequestMapper::toItemResponse, Collectors.toList())
                ));
    }

    public ItemRequest getRequestIfExists(Long requestId) {
        return repository.findById(requestId)
                .orElseThrow(() -> {
                    log.debug(repository.findAll().stream().map(ItemRequest::getId).toList().toString());
                    return new NotFoundException("Запрос вещи с ID = '" + requestId + "' не найден");
                });
    }
}
