package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    User createUser(UserDto userDto);

    User updateUser(Long userId, User user);

    List<UserDto> getUsers();

    User getUserById(Long userId);

    UserDto deleteUserById(Long userId);
}
