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
    public User createUser(UserDto userDto) {
        User newUser = UserMapper.toUser(userDto, generateID());
        if (USERS.contains(UserMapper.toUser(userDto, null))) {
            throw new ValidationException("Пользователь с email = " + userDto.getEmail() + " уже существует!");
        }
        USERS.add(newUser);

        return newUser;
    }

    @Override
    public User updateUser(Long userId, User user) {
        if (user.getEmail() != null && USERS.contains(user)) {
            throw new ValidationException("Пользователь с email = " + user.getEmail() + " уже существует!");
        }
        Optional<User> userOptional = USERS.stream()
                .filter(userInList -> userInList.getId().equals(userId))
                .findFirst();
        if (userOptional.isEmpty()) {
            throw new NotFoundException("Пользователь с ID = " + userId + " не найден!");
        }
        User userToUpdate = userOptional.get();
        userToUpdate.setEmail(user.getEmail() != null ? user.getEmail() : null);
        userToUpdate.setName(user.getName() != null ? user.getName() : null);

        return userToUpdate;
    }

    @Override
    public List<UserDto> getUsers() {
        return USERS.stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public User getUserById(Long userId) {
        Optional<User> userToFind = USERS.stream()
                .filter(user -> Objects.equals(user.getId(), userId))
                .findFirst();
        if (userToFind.isEmpty()) {
            throw new NotFoundException("Пользователь с ID = " + userId + " не найден!");
        }
        return userToFind.get();
    }

    @Override
    public UserDto deleteUserById(Long userId) {
        User user = getUserById(userId);
        USERS.remove(user);
        return UserMapper.toUserDto(user);
    }

    private long generateID() {
        return ++userId;
    }
}
