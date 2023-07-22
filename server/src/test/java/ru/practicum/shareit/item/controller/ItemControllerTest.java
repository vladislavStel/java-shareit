package ru.practicum.shareit.item.controller;

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
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.service.ItemService;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ItemController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private final ItemService itemService;

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
    void shouldGetAllItems_ReturnListItems() throws Exception {
        when(itemService.getAllItemsByUserId(1L, 0, 10))
                .thenReturn(List.of(itemOwnerDtoBuilder.id(1L).build()));
        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$.size()", is(1)));
    }

    @Test
    void shouldGetAllItemsIfNoUsers_ReturnEmptyList() throws Exception {
        when(itemService.getAllItemsByUserId(1L, 0, 10)).thenReturn(Collections.emptyList());
        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(0)));
    }

    @Test
    void shouldGetItemById_ReturnStatus200AndCorrectJson() throws Exception {
        ItemOwnerDto itemOwnerDto = ItemOwnerDto.builder().id(1L).build();
        String json = mapper.writeValueAsString(itemOwnerDto);

        when(itemService.getItemById(1L, 1L)).thenReturn(itemOwnerDto);
        mockMvc.perform(get(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(json));
    }

    @Test
    void shouldGetItemByIdWhenNotExistingUserId_ReturnStatus404() throws Exception {
        when(itemService.getItemById(999L, 1L))
                .thenThrow(new ObjectNotFoundException(String.format("User not found: id=%d", 999L)));
        mockMvc.perform(get(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 999))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"User not found: id=999\"}"));
    }

    @Test
    void shouldGetItemByIdWhenNotExistingItemId_ReturnStatus404() throws Exception {
        when(itemService.getItemById(1L, 999L))
                .thenThrow(new ObjectNotFoundException(String.format("Item not found: id=%d", 999L)));
        mockMvc.perform(get(url + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"Item not found: id=999\"}"));
    }

    @Test
    void shouldSearchItem_ReturnEmptyList() throws Exception {
        when(itemService.getSearchItem("", 0, 10)).thenReturn(Collections.emptyList());
        mockMvc.perform(get(url + "/search").param("text", ""))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldSearchItemIfFromNegative_ReturnStatus400() throws Exception {
        mockMvc.perform(get(url + "/search").param("from", "-1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].error", is("must be greater than or equal to 0")));
    }

    @Test
    void shouldSearchItemIfSizeZero_ReturnStatus400() throws Exception {
        mockMvc.perform(get(url + "/search").param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].error", is("must be greater than 0")));
    }

    @Test
    void shouldSearchItemIfSizeNegative_ReturnStatus400() throws Exception {
        mockMvc.perform(get(url + "/search").param("size", "-1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].error", is("must be greater than 0")));
    }

    @Test
    void shouldCreateItem_ReturnStatus200AndCorrectJson() throws Exception {
        ItemDto itemDto = itemDtoBuilder.build();
        ItemDto itemDtoResponse = itemDtoBuilder.id(1L).build();
        String json = mapper.writeValueAsString(itemDto);
        String jsonAdded = mapper.writeValueAsString(itemDtoResponse);

        when(itemService.saveItem(1L, itemDto)).thenReturn(itemDtoResponse);
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonAdded));
    }

    @Test
    void shouldCreateItemWithNotFoundUser_ReturnStatus404() throws Exception {
        ItemDto itemDto = itemDtoBuilder.build();
        String json = mapper.writeValueAsString(itemDto);
        when(itemService.saveItem(999L, itemDto))
                .thenThrow(new ObjectNotFoundException(String.format("User not found: id=%d", 999L)));
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 999)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"User not found: id=999\"}"));
    }

    @Test
    void shouldCreateItemWithoutUserId_ReturnStatus500() throws Exception {
        ItemDto itemDto = itemDtoBuilder.build();
        String json = mapper.writeValueAsString(itemDto);
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(content().json("{\"error\":\"Required request header 'X-Sharer-User-Id' " +
                "for method parameter type Long is not present\"}"));
    }

    @Test
    void shouldCreateCommentItem_ReturnStatus200AndCorrectJson() throws Exception {
        CommentDto commentDto = commentDtoBuilder.build();
        CommentDto outCommentDto = commentDtoBuilder.id(1L).authorName("name").created(LocalDateTime.now()).build();
        String json = mapper.writeValueAsString(commentDto);
        String jsonAdded = mapper.writeValueAsString(outCommentDto);

        when(itemService.createComment(1L, 1L, commentDto)).thenReturn(outCommentDto);
        mockMvc.perform(post(url + "/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonAdded));
    }

    @Test
    void shouldCreateCommentItemWithNotFoundUser_ReturnStatus404() throws Exception {
        CommentDto commentDto = commentDtoBuilder.build();
        String json1 = mapper.writeValueAsString(commentDto);
        when(itemService.createComment(999L, 1L, commentDto))
                .thenThrow(new ObjectNotFoundException(String.format("User not found: id=%d", 999L)));
        mockMvc.perform(post(url + "/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 999)
                        .content(json1))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"User not found: id=999\"}"));
    }

    @Test
    void shouldCreateCommentItemWithNotFoundItem_ReturnStatus404() throws Exception {
        CommentDto commentDto = commentDtoBuilder.build();
        String json = mapper.writeValueAsString(commentDto);
        when(itemService.createComment(1L, 999L, commentDto))
                .thenThrow(new ObjectNotFoundException(String.format("Item not found: id=%d", 999L)));
        mockMvc.perform(post(url + "/999/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"Item not found: id=999\"}"));
    }

    @Test
    void shouldPatchItem_ReturnStatus200AndCorrectJson() throws Exception {
        ItemDto itemDto = itemDtoBuilder.id(1L).name("Update").description("Update description")
                .available(true).build();
        ItemDto itemDtoResponse = itemDtoBuilder.id(1L).name("Update").description("Update description")
                .available(true).build();
        String json = mapper.writeValueAsString(itemDto);
        String jsonAdded = mapper.writeValueAsString(itemDtoResponse);

        when(itemService.updateItem(1L, 1L, itemDto)).thenReturn(itemDtoResponse);
        mockMvc.perform(patch(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonAdded));
    }

    @Test
    void shouldPatchItemWithoutUserId_ReturnStatus500() throws Exception {
        ItemDto itemDto = itemDtoBuilder.build();
        String json = mapper.writeValueAsString(itemDto);
        mockMvc.perform(patch(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(content().json("{\"error\":\"Required request header 'X-Sharer-User-Id' " +
                        "for method parameter type Long is not present\"}"));
    }

    @Test
    void shouldPatchItemWithNotFoundItem_ReturnStatus404() throws Exception {
        ItemDto itemDto = itemDtoBuilder.build();
        String json = mapper.writeValueAsString(itemDto);
        when(itemService.updateItem(1L, 999L, itemDto))
                .thenThrow(new ObjectNotFoundException(String.format("Item not found: id=%d", 999L)));
        mockMvc.perform(patch(url + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"Item not found: id=999\"}"));
    }

    @Test
    void shouldPatchItemWithNotFoundUser_ReturnStatus404() throws Exception {
        ItemDto itemDto = itemDtoBuilder.build();
        String json = mapper.writeValueAsString(itemDto);
        when(itemService.updateItem(999L, 1L, itemDto))
                .thenThrow(new ObjectNotFoundException(String.format("User not found: id=%d", 999L)));
        mockMvc.perform(patch(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 999)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"User not found: id=999\"}"));
    }

    @Test
    void shouldPatchItemIfItemName_ReturnStatus200AndCorrectJson() throws Exception {
        ItemDto itemDto = itemDtoBuilder.id(1L).name("Update").build();
        ItemDto itemDtoResponse = itemDtoBuilder.id(1L).name("Update").build();
        String json = mapper.writeValueAsString(itemDto);
        String jsonAdded = mapper.writeValueAsString(itemDtoResponse);

        when(itemService.updateItem(1L, 1L, itemDto)).thenReturn(itemDtoResponse);
        this.mockMvc.perform(patch(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonAdded));
    }

    @Test
    void shouldPatchItemIfItemDescription_ReturnStatus200AndCorrectJson() throws Exception {
        ItemDto itemDto = itemDtoBuilder.id(1L).description("Update description").build();
        ItemDto itemDtoResponse = itemDtoBuilder.id(1L).description("Update description").build();
        String json = mapper.writeValueAsString(itemDto);
        String jsonAdded = mapper.writeValueAsString(itemDtoResponse);

        when(itemService.updateItem(1L, 1L, itemDto)).thenReturn(itemDtoResponse);
        this.mockMvc.perform(patch(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonAdded));
    }

    @Test
    void shouldPatchItemIfItemAvailable_ReturnStatus200AndCorrectJson() throws Exception {
        ItemDto itemDto = itemDtoBuilder.id(1L).available(true).build();
        ItemDto itemDtoResponse = itemDtoBuilder.id(1L).available(true).build();
        String json = mapper.writeValueAsString(itemDto);
        String jsonAdded = mapper.writeValueAsString(itemDtoResponse);

        when(itemService.updateItem(1L, 1L, itemDto)).thenReturn(itemDtoResponse);
        this.mockMvc.perform(patch(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonAdded));
    }

}