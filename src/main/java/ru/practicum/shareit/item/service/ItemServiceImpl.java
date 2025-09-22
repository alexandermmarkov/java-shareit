package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;

    @Override
    public Item addItem(Long userId, ItemDto itemDto) {
        return repository.addItem(userId, itemDto);
    }

    @Override
    public Item updateItem(Long userId, Long itemId, ItemDto itemDto) {
        return repository.updateItem(userId, itemId, itemDto);
    }

    @Override
    public Item getItemById(Long itemId) {
        return repository.getItemById(itemId);
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
