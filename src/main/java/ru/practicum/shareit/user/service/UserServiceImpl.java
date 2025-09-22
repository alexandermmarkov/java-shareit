package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public User createUser(UserDto userDto) {
        return repository.createUser(userDto);
    }

    @Override
    public User updateUser(Long userId, User user) {
        return repository.updateUser(userId, user);
    }

    @Override
    public List<UserDto> getUsers() {
        return repository.getUsers();
    }

    @Override
    public User getUserById(Long userId) {
        return repository.getUserById(userId);
    }

    @Override
    public UserDto deleteUserById(Long userId) {
        return repository.deleteUserById(userId);
    }
}
