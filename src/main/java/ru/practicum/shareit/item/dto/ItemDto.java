package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.user.dto.UserDto;

@Data
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class ItemDto {
    private Long id;

    @NotBlank(message = "Название вещи не может быть пустым")
    private String name;

    @NotBlank(message = "Описание вещи не может быть пустым")
    private String description;

    private UserDto owner;

    @NotNull(message = "Статус доступности вещи не может быть пустым")
    private Boolean available;

    private Long requestId;
}
