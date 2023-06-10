package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserRepository {

    Collection<User> findAll();

    User findById(Long userId);

    User save(User user);

    User update(User user);

    void delete(Long userId);

    boolean userNotExist(Long userId);

    boolean isEmailExist(String email);

}