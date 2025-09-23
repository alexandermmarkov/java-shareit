package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {
    private static final List<Item> ITEMS = new ArrayList<>();
    private static long itemId = 0;
    private final UserRepository userRepository;

    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        if (userId == null) {
            throw new ValidationException("В заголовке не передан ID владельца вещи!");
        }
        UserDto ownerDto = userRepository.getUserById(userId);
        Item newItem = ItemMapper.toItem(itemDto, UserMapper.toUser(ownerDto, userId), generateID());
        ITEMS.add(newItem);

        return ItemMapper.toItemDto(newItem);
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        userRepository.getUserById(userId);
        Item item = getItemById(itemId);

        if (!userId.equals(item.getOwner().getId())) {
            throw new ValidationException("Изменять описание вещи может только её владелец!");
        }

        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());

        return ItemMapper.toItemDto(item);
    }

    @Override
    public Item getItemById(Long itemId) {
        Optional<Item> itemToFind = ITEMS.stream()
                .filter(item -> Objects.equals(item.getId(), itemId))
                .findFirst();

        if (itemToFind.isEmpty()) {
            throw new NotFoundException("Вещь с ID = " + itemId + " не найдена!");
        }

        return itemToFind.get();
    }

    @Override
    public List<ItemDto> getItems(Long userId) {
        return ITEMS.stream()
                .filter(item -> Objects.equals(item.getOwner().getId(), userId))
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        if ((text == null) || (text.isBlank())) {
            return Collections.emptyList();
        }
        String upperCaseText = text.toUpperCase();
        return ITEMS.stream()
                .filter(item -> ((item.getName() != null) && (item.getName().toUpperCase().contains(upperCaseText))
                        || (item.getDescription() != null) && (item.getDescription().toUpperCase().contains(text.toUpperCase()))))
                .filter(Item::getAvailable)
                .map(ItemMapper::toItemDto)
                .toList();
    }

    private long generateID() {
        return ++itemId;
    }
}
