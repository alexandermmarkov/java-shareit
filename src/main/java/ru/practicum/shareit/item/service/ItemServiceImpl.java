package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;

    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        return repository.addItem(userId, itemDto);
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        return repository.updateItem(userId, itemId, itemDto);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        return ItemMapper.toItemDto(repository.getItemById(itemId));
    }

    @Override
    public List<ItemDto> getItems(Long userId) {
        return repository.getItems(userId);
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        return repository.searchItem(text);
    }
}
