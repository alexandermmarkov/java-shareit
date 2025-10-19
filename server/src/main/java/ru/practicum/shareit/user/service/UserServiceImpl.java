package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(UserDto userDto) {
        log.debug("createUser(userDto={})", userDto);

        User user = repository.save(userMapper.toUser(userDto));
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.debug("updateUser(userId={}, userDto={})", userId, userDto);
        UserDto oldUserDto = getUserById(userId);

        userDto.setId(userId);
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            userDto.setEmail(oldUserDto.getEmail());
        }
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            userDto.setName(oldUserDto.getName());
        }
        User user = repository.save(userMapper.toUser(userDto));
        return userMapper.toUserDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers() {
        log.debug("getUsers()");

        return repository.findAll()
                .stream()
                .map(userMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        log.debug("getUserById(userId={})", userId);

        User user = repository.findById(userId).orElseThrow(() -> {
            log.debug(repository.findAll().stream().map(User::getId).toList().toString());
            return new NotFoundException("Пользователь с ID = '" + userId + "' не найден");
        });

        return userMapper.toUserDto(user);
    }

    @Override
    public void deleteUserById(Long userId) {
        log.debug("deleteUserById(deleteUserById={})", userId);

        repository.deleteById(userId);
    }
}
