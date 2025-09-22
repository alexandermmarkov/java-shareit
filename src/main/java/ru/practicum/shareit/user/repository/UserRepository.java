package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserRepository {
    User createUser(UserDto userDto);

    User updateUser(Long userId, User user);

    List<UserDto> getUsers();

    User getUserById(Long userId);

    UserDto deleteUserById(Long userId);
}
