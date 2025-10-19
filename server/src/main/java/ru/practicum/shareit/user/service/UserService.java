package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto);

    UserDto updateUser(Long userId, UserDto userDto);

    List<UserDto> getUsers();

    UserDto getUserById(Long userId);

    void deleteUserById(Long userId);
}
