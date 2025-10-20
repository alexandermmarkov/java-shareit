package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.user.dto.UserDto;

@Data
@AllArgsConstructor
public class ItemResponseDto {
    private Long id;
    private String name;
    private UserDto owner;
}
