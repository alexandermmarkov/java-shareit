package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ItemRequestResponseDto {
    private Long id;
    private String description;
    private LocalDateTime created;
    List<ItemResponseDto> items;
}
