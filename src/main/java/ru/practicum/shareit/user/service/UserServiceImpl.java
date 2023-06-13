package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.UserAlreadyExistException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long userId) {
        return UserMapper.toUserDto(userRepository.findById(userId));
    }

    @Override
    public UserDto saveUser(UserDto userDto) {
        if (userRepository.isEmailExist(userDto.getEmail())) {
            throw new UserAlreadyExistException("Пользователь с электронной почтой " +
                    userDto.getEmail() + " уже зарегистрирован.");
        }
        return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(userDto)));
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        if (userRepository.userNotExist(userId)) {
            throw new ObjectNotFoundException(String.format("Пользователь не найден: id=%d", userId));
        }
        User user = userRepository.findById(userId);
        if (!user.getEmail().equals(userDto.getEmail()) && userRepository.isEmailExist(userDto.getEmail())) {
            throw new UserAlreadyExistException("Пользователь с электронной почтой " +
                    userDto.getEmail() + " уже зарегистрирован.");
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        return UserMapper.toUserDto(userRepository.update(user));
    }

    @Override
    public void deleteUser(Long userId) {
        if (userRepository.userNotExist(userId)) {
            throw new ObjectNotFoundException(String.format("Пользователь не найден: id=%d", userId));
        }
        userRepository.delete(userId);
    }

}