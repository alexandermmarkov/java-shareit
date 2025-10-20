package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ItemRequestMapper {

    ItemRequestDto toItemRequestDto(ItemRequest itemRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "requestor", source = "requestor")
    ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User requestor);

    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "name", source = "item.name")
    @Mapping(target = "owner", source = "item.owner")
    ItemResponseDto toItemResponse(Item item);

    @Mapping(target = "items", source = "itemResponseDtoList")
    ItemRequestResponseDto toItemRequestResponseDto(ItemRequest itemRequest, List<ItemResponseDto> itemResponseDtoList);
}
