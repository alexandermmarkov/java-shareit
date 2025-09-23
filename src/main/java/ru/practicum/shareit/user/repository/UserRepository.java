package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserRepository {
    UserDto createUser(UserDto userDto);

    UserDto updateUser(Long userId, UserDto userDto);

    List<UserDto> getUsers();

    UserDto getUserById(Long userId);

    UserDto deleteUserById(Long userId);
}
