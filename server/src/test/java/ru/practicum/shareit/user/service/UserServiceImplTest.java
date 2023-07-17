package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.UserAlreadyExistException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("name");
        user.setEmail("email@email.com");
    }

    @Test
    void shouldGetAllUsers_ReturnListUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(1, users.size());
    }

    @Test
    void shouldGetAllUsers_ReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserDto> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(0, users.size());
    }

    @Test
    void shouldGetUserById_ReturnUser() {
        long userId = user.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserDto user = userService.getUserById(userId);

        assertNotNull(user);
        assertEquals(userId, user.getId());
    }

    @Test
    void shouldGetUserByIdWhenUserNotFound_ReturnObjectNotFoundException() {
        long userId = 999L;
        String error = String.format("User not found: id=%d", userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                    () -> userService.getUserById(userId));

        assertEquals(error, exception.getMessage());
    }

    @Test
    void shouldSaveUser_ReturnSavedUser() {
        User userToSave = new User();
        userToSave.setName("name");
        userToSave.setEmail("email@mail.com");

        when(userRepository.save(any(User.class))).thenReturn(user);
        UserDto userDto = UserMapper.toUserDto(userToSave);
        UserDto userSaved = userService.saveUser(userDto);

        assertNotNull(userSaved);
        assertEquals(user.getId(), userSaved.getId());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void shouldSaveUserWhenDupticateEmail_ReturnUserAlreadyExistException() {
        UserDto userDto = UserMapper.toUserDto(user);
        String error = ("User with email " + user.getEmail() + " is already registered.");
        when(userRepository.save(any(User.class))).thenThrow(new UserAlreadyExistException("User with email " +
                user.getEmail() + " is already registered."));

        UserAlreadyExistException exception = assertThrows(UserAlreadyExistException.class,
                () -> userService.saveUser(userDto));

        assertEquals(error, exception.getMessage());
    }

    @Test
    void shouldUpdateNameUser_ReturnUpdatedUser() {
        long userId = user.getId();
        String nameUpdated = "nameUpdated";
        User userUpdated = new User();
        userUpdated.setId(userId);
        userUpdated.setName(nameUpdated);
        userUpdated.setEmail(user.getEmail());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(userUpdated);
        UserDto userDtoUpdated = userService.updateUser(userId, UserDto.builder().name(nameUpdated).build());

        assertNotNull(userDtoUpdated);
        assertEquals(userId, userDtoUpdated.getId());
        assertEquals(nameUpdated, userDtoUpdated.getName());
    }

    @Test
    void shouldUpdateEmailUser_ReturnUpdatedUser() {
        long userId = user.getId();
        String emailUpdated = "emailUpdated";
        User userUpdated = new User();
        userUpdated.setId(userId);
        userUpdated.setName(user.getName());
        userUpdated.setEmail(emailUpdated);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(userUpdated);
        UserDto userDtoUpdated = userService.updateUser(userId, UserDto.builder().email(emailUpdated).build());

        assertNotNull(userDtoUpdated);
        assertEquals(userId, userDtoUpdated.getId());
        assertEquals(emailUpdated, userDtoUpdated.getEmail());
    }

    @Test
    void shouldUpdateUserWhenUserNotFound_ReturnObjectNotFoundException() {
        long userId = 999L;
        UserDto updateUser = UserMapper.toUserDto(user);
        String error = String.format("User not found: id=%d", userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> userService.updateUser(userId, updateUser));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void deleteUser() {
        long userId = user.getId();
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }

}