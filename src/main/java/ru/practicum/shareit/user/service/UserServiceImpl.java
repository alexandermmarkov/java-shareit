package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public UserDto createUser(UserDto userDto) {
        return repository.createUser(userDto);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        return repository.updateUser(userId, userDto);
    }

    @Override
    public List<UserDto> getUsers() {
        return repository.getUsers();
    }

    @Override
    public UserDto getUserById(Long userId) {
        return repository.getUserById(userId);
    }

    @Override
    public UserDto deleteUserById(Long userId) {
        return repository.deleteUserById(userId);
    }
}
