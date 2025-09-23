package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private static final List<User> USERS = new ArrayList<>();
    private static long userId = 0;

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto, generateID());
        if (USERS.contains(UserMapper.toUser(userDto, null))) {
            throw new ValidationException("Пользователь с email = " + userDto.getEmail() + " уже существует!");
        }
        USERS.add(user);

        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        if (userDto.getEmail() != null && USERS.contains(UserMapper.toUser(userDto, userId))) {
            throw new ValidationException("Пользователь с email = " + userDto.getEmail() + " уже существует!");
        }
        Optional<User> userOptional = USERS.stream()
                .filter(userInList -> userInList.getId().equals(userId))
                .findFirst();
        if (userOptional.isEmpty()) {
            throw new NotFoundException("Пользователь с ID = " + userId + " не найден!");
        }
        User userToUpdate = userOptional.get();
        userToUpdate.setEmail(userDto.getEmail() != null ? userDto.getEmail() : null);
        userToUpdate.setName(userDto.getName() != null ? userDto.getName() : null);

        return UserMapper.toUserDto(userToUpdate);
    }

    @Override
    public List<UserDto> getUsers() {
        return USERS.stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public UserDto getUserById(Long userId) {
        Optional<User> userToFind = USERS.stream()
                .filter(user -> Objects.equals(user.getId(), userId))
                .findFirst();
        if (userToFind.isEmpty()) {
            throw new NotFoundException("Пользователь с ID = " + userId + " не найден!");
        }
        return UserMapper.toUserDto(userToFind.get());
    }

    @Override
    public UserDto deleteUserById(Long userId) {
        UserDto userDto = getUserById(userId);
        User user = UserMapper.toUser(userDto, userId);
        USERS.remove(user);

        return UserMapper.toUserDto(user);
    }

    private long generateID() {
        return ++userId;
    }
}
