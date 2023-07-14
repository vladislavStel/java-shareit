package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.UserAlreadyExistException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long userId) {
        return UserMapper.toUserDto(userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("User not found: id=%d", userId))));
    }

    @Transactional
    @Override
    public UserDto saveUser(UserDto userDto) {
        try {
            return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(userDto)));
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistException("User with email " +
                    userDto.getEmail() + " is already registered.");
        }
    }

    @Transactional
    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = getById(userId);
        User validuser = userRepository.findByEmail(userDto.getEmail()).orElse(null);
        if (validuser != null && !Objects.equals(validuser.getId(), userId)) {
            throw new UserAlreadyExistException("User with email " +
                    userDto.getEmail() + " is already registered.");
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        validateUserById(userId);
        userRepository.deleteById(userId);
    }

    @Override
    public User getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("User not found: id=%d", userId)));
    }

    @Override
    public void validateUserById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ObjectNotFoundException(String.format("User not found: id=%d", userId));
        }
    }

}