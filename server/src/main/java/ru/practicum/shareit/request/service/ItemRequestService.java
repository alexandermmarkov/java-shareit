package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto addItemRequest(Long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestResponseDto> getItemRequests(Long userId);

    List<ItemRequestDto> getAllItemRequests(Long userId);

    ItemRequestResponseDto getItemRequestById(Long userId, Long requestId);
}
