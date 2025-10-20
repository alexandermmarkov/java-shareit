package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.user.dto.UserDto;

@Data
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private UserDto owner;
    private Boolean available;
    private Long requestId;
}
