package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    ItemWithDateDto getItemById(Long userId, Long itemId);

    List<ItemWithDateDto> getItems(Long userId);

    List<ItemDto> searchItem(String text);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}
