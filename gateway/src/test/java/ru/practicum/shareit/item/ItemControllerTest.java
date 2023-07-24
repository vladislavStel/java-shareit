package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Random;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private final ItemClient itemClient;

    String url = "/items";

    ItemDto.ItemDtoBuilder itemDtoBuilder;
    ItemOwnerDto.ItemOwnerDtoBuilder itemOwnerDtoBuilder;
    CommentDto.CommentDtoBuilder commentDtoBuilder;

    JavaTimeModule module = new JavaTimeModule();
    ObjectMapper mapper = new ObjectMapper().registerModule(module)
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm"));

    @BeforeEach
    void setUp() {
        itemDtoBuilder = ItemDto.builder()
                .name("Item")
                .description("Description for item")
                .available(true)
                .requestId(null);
        itemOwnerDtoBuilder = ItemOwnerDto.builder()
                .name("ItemOwner")
                .description("Description for itemOwner")
                .available(true);
        commentDtoBuilder = CommentDto.builder()
                .text("Test feedback on the use of the item");
    }

    @Test
    void shouldMockMvc() {
        assertNotNull(mockMvc);
    }

    @Test
    void shouldCreateItemIfItemWithoutAvailable_ReturnStatus400() throws Exception {
        ItemDto itemDto = itemDtoBuilder.available(null).build();
        String json = mapper.writeValueAsString(itemDto);
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].fieldName", is("available")))
                .andExpect(jsonPath("$[0].error", is("must not be null")));
    }

    @Test
    void shouldCreateItemIfItemEmptyName_ReturnStatus400() throws Exception {
        ItemDto itemDto = itemDtoBuilder.name("").build();
        String json = mapper.writeValueAsString(itemDto);
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].fieldName", is("name")))
                .andExpect(jsonPath("$[0].error", is("must not be blank")));
    }

    @Test
    void shouldCreateItemIfItemEmptyDescription_ReturnStatus400() throws Exception {
        ItemDto itemDto = itemDtoBuilder.description("").build();
        String json = mapper.writeValueAsString(itemDto);
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].fieldName", is("description")))
                .andExpect(jsonPath("$[0].error", is("must not be blank")));
    }

    @Test
    void shouldCreateCommentItemIfCommentEmptyText_ReturnStatus400() throws Exception {
        CommentDto commentDto = commentDtoBuilder.text("").build();
        String json = mapper.writeValueAsString(commentDto);
        mockMvc.perform(post(url + "/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].fieldName", is("text")))
                .andExpect(jsonPath("$[0].error", is("must not be blank")));
    }

    @Test
    void shouldCreateCommentItemIfCommentTextWrongSize_ReturnStatus400() throws Exception {
        byte[] array = new byte[550];
        new Random().nextBytes(array);
        String generatedString = new String(array, StandardCharsets.UTF_8);
        CommentDto commentDto = commentDtoBuilder.text(generatedString).build();
        String json = mapper.writeValueAsString(commentDto);
        mockMvc.perform(post(url + "/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].fieldName", is("text")))
                .andExpect(jsonPath("$[0].error", is("size must be between 0 and 500")));
    }

    @Test
    void shouldSearchItemIfFromNegative_ReturnStatus400() throws Exception {
        mockMvc.perform(get(url + "/search")
                        .param("from", "-1")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].error", is("must be greater than or equal to 0")));
    }

    @Test
    void shouldSearchItemIfSizeZero_ReturnStatus400() throws Exception {
        mockMvc.perform(get(url + "/search")
                        .param("size", "0")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].error", is("must be greater than 0")));
    }

    @Test
    void shouldSearchItemIfSizeNegative_ReturnStatus400() throws Exception {
        mockMvc.perform(get(url + "/search")
                        .param("size", "-1")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].error", is("must be greater than 0")));
    }

}