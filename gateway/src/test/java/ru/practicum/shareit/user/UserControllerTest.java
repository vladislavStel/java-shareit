package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private final UserClient userClient;

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

}