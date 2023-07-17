package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceTestIT {

    private final EntityManager entityManager;
    private final UserService userService;

    @Test
    void shouldGetAllUsers_ReturnListUsers() {
        List<UserDto> sourceUsers = List.of(
                makeUserDto("name1", "e1@mail.ru"),
                makeUserDto("name2", "e2@mail.ru"),
                makeUserDto("name3", "e3@mail.ru")
        );

        for (UserDto user : sourceUsers) {
            User entity = UserMapper.toUser(user);
            entityManager.persist(entity);
        }
        entityManager.flush();

        List<UserDto> targetUsers = userService.getAllUsers();

        assertThat(targetUsers, hasSize(sourceUsers.size()));
        for (UserDto sourceUser : sourceUsers) {
            assertThat(targetUsers, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceUser.getName())),
                    hasProperty("email", equalTo(sourceUser.getEmail()))
            )));
        }
    }

    private UserDto makeUserDto(String name, String email) {
        return UserDto.builder()
                .name(name)
                .email(email)
                .build();
    }

}