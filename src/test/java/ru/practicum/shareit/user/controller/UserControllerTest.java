package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private final UserService userService;

    String url = "/users";

    UserDto.UserDtoBuilder userDtoBuilder;

    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        userDtoBuilder = UserDto.builder()
                .name("Test")
                .email("testUser@email.ru");
    }

    @Test
    void shouldMockMvc() {
        assertNotNull(mockMvc);
    }

    @Test
    void shouldGetAllUsers_ReturnListUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(userDtoBuilder.id(1L).build()));
        mockMvc.perform(get(url))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$.size()", is(1)));
    }

    @Test
    void shouldGetAllUsersIfNoUsers_ReturnEmptyList() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());
        mockMvc.perform(get(url))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(0)));
    }

    @Test
    void shouldGetUserById_ReturnStatus200AndCorrectJson() throws Exception {
        UserDto userDto = UserDto.builder().id(1L).build();
        String json = mapper.writeValueAsString(userDto);

        when(userService.getUserById(1L)).thenReturn(userDto);
        mockMvc.perform(get(url + "/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(json));
    }

    @Test
    void shouldGetUserByIdWhenNotExistingId_ReturnStatus404() throws Exception {
        when(userService.getUserById(999L))
                .thenThrow(new ObjectNotFoundException(String.format("User not found: id=%d", 999L)));
        mockMvc.perform(get(url + "/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"User not found: id=999\"}"));
    }

    @Test
    void shouldCreateUser_ReturnStatus200AndCorrectJson() throws Exception {
        UserDto userDto = userDtoBuilder.build();
        UserDto userDtoResponse = userDtoBuilder.id(1L).build();
        String json = mapper.writeValueAsString(userDto);
        String jsonAdded = mapper.writeValueAsString(userDtoResponse);

        when(userService.saveUser(userDto)).thenReturn(userDtoResponse);
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonAdded));
    }

    @Test
    void shouldCreateUserIfUserInvalidEmail_ReturnStatus400() throws Exception {
        UserDto userDto = userDtoBuilder.email("123email.com").build();
        String json = mapper.writeValueAsString(userDto);
        mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].fieldName", is("email")))
                .andExpect(jsonPath("$[0].error", is("must be a well-formed email address")));
    }

    @Test
    void shouldCreateUserIfUserEmailNull_ReturnStatus400() throws Exception {
        UserDto userDto = userDtoBuilder.email(null).build();
        String json = mapper.writeValueAsString(userDto);
        mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].fieldName", is("email")))
                .andExpect(jsonPath("$[0].error", is("must not be blank")));
    }

    @Test
    void shouldCreateUserIfUserNameNull_ReturnStatus400() throws Exception {
        UserDto userDto = userDtoBuilder.name(null).build();
        String json = mapper.writeValueAsString(userDto);
        mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].fieldName", is("name")))
                .andExpect(jsonPath("$[0].error", is("must not be blank")));
    }

    @Test
    void shouldPatchUser_ReturnStatus200AndCorrectJson() throws Exception {
        UserDto userDto = userDtoBuilder.name("Update").email("update@email.ru").build();
        UserDto userDtoResponse = userDtoBuilder.id(1L).name("Update").email("update@email.ru").build();
        String json = mapper.writeValueAsString(userDto);
        String jsonAdded = mapper.writeValueAsString(userDtoResponse);

        when(userService.updateUser(1L, userDto)).thenReturn(userDtoResponse);
        this.mockMvc.perform(patch(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonAdded));
    }

    @Test
    void shouldPatchUserName_ReturnStatus200AndCorrectJson() throws Exception {
        UserDto userDto = userDtoBuilder.name("Update").build();
        UserDto userDtoResponse = userDtoBuilder.id(1L).name("Update").build();
        String json = mapper.writeValueAsString(userDto);
        String jsonAdded = mapper.writeValueAsString(userDtoResponse);

        when(userService.updateUser(1L, userDto)).thenReturn(userDtoResponse);
        this.mockMvc.perform(patch(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonAdded));
    }

    @Test
    void shouldPatchUserEmail_ReturnStatus200AndCorrectJson() throws Exception {
        UserDto userDto = userDtoBuilder.email("update@email.ru").build();
        UserDto userDtoResponse = userDtoBuilder.id(1L).email("update@email.ru").build();
        String json = mapper.writeValueAsString(userDto);
        String jsonAdded = mapper.writeValueAsString(userDtoResponse);

        when(userService.updateUser(1L, userDto)).thenReturn(userDtoResponse);
        this.mockMvc.perform(patch(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonAdded));
    }

    @Test
    void shouldDeleteUser_ReturnStatus200() throws Exception {
        mockMvc.perform(delete(url + "/1"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteUserWhenNotExistingId_ReturnStatus404() throws Exception {
        doThrow(new ObjectNotFoundException(String.format("User not found: id=%d", 999L)))
                .when(userService).deleteUser(999L);
        mockMvc.perform(delete(url + "/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"User not found: id=999\"}"));
    }

}