package ru.practicum.shareit.request.controller;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ItemRequestController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private final ItemRequestService itemRequestService;

    String url = "/requests";

    ItemRequestDto.ItemRequestDtoBuilder itemRequestDtoBuilder;

    JavaTimeModule module = new JavaTimeModule();
    ObjectMapper mapper = new ObjectMapper().registerModule(module)
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm"));

    @BeforeEach
    void setUp() {
        itemRequestDtoBuilder = ItemRequestDto.builder()
                .description("Description for request")
                .created(LocalDateTime.now());
    }

    @Test
    void shouldMockMvc() {
        assertNotNull(mockMvc);
    }

    @Test
    void getAllRequestsWithOffers_ReturnList() throws Exception {
        when(itemRequestService.getAllRequestsWithOffers(1L))
                .thenReturn(List.of(itemRequestDtoBuilder.id(1L).build()));
        mockMvc.perform(get(url)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$.size()", is(1)));
    }

    @Test
    void getAllRequestsWithOffers_ReturnEmptyList() throws Exception {
        when(itemRequestService.getAllRequestsWithOffers(1L)).thenReturn(Collections.emptyList());
        mockMvc.perform(get(url)
                        .header("X-Sharer-User-Id", 2))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllRequestsWithOffersWithNotFoundUser_ReturnStatus404() throws Exception {
        when(itemRequestService.getAllRequestsWithOffers(999L))
                .thenThrow(new ObjectNotFoundException(String.format("User not found: id=%d", 999L)));
        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 999))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"User not found: id=999\"}"));
    }

    @Test
    void shouldGetRequestWithOffersById_ReturnStatus200AndCorrectJson() throws Exception {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().id(1L).build();
        String json = mapper.writeValueAsString(itemRequestDto);

        when(itemRequestService.getRequestWithOffersById(1L, 1L)).thenReturn(itemRequestDto);
        mockMvc.perform(get(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(json));
    }

    @Test
    void shouldGetRequestWithOffersByIdWhenNotExistingUserId_ReturnStatus404() throws Exception {
        when(itemRequestService.getRequestWithOffersById(999L, 1L))
                .thenThrow(new ObjectNotFoundException(String.format("User not found: id=%d", 999L)));
        mockMvc.perform(get(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 999))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"User not found: id=999\"}"));
    }

    @Test
    void shouldGetRequestWithOffersByIdWhenNotExistingRequestId_ReturnStatus404() throws Exception {
        when(itemRequestService.getRequestWithOffersById(1L, 999L))
                .thenThrow(new ObjectNotFoundException(String.format("Request not found: id=%d", 999L)));
        mockMvc.perform(get(url + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"Request not found: id=999\"}"));
    }

    @Test
    void shouldGetRequests_ReturnStatus200AndCorrectJson() throws Exception {
        when(itemRequestService.getRequests(1L, 0, 1))
                .thenReturn(List.of(itemRequestDtoBuilder.id(1L).build()));
        mockMvc.perform(get(url + "/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$.size()", is(1)));
    }

    @Test
    void shouldGetRequestsIfFromNegative_ReturnStatus400() throws Exception {
        mockMvc.perform(get(url + "/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "-1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].error", is("must be greater than or equal to 0")));
    }

    @Test
    void shouldGetRequestsIfSizeZero_ReturnStatus400() throws Exception {
        mockMvc.perform(get(url + "/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].error", is("must be greater than 0")));
    }

    @Test
    void shouldGetRequestsIfSizeNegative_ReturnStatus400() throws Exception {
        mockMvc.perform(get(url + "/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("size", "-1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].error", is("must be greater than 0")));
    }

    @Test
    void shouldCreateRequest_ReturnStatus200AndCorrectJson() throws Exception {
        ItemRequestDto itemRequestDto = itemRequestDtoBuilder.build();
        ItemRequestDto outItemRequestDto = itemRequestDtoBuilder.id(1L).build();
        String json = mapper.writeValueAsString(itemRequestDto);
        String jsonAdded = mapper.writeValueAsString(outItemRequestDto);

        when(itemRequestService.saveRequest(1L, itemRequestDto)).thenReturn(outItemRequestDto);
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonAdded));
    }

    @Test
    void shouldCreateRequestWithNotFoundUser_ReturnStatus404() throws Exception {
        ItemRequestDto itemRequestDto = itemRequestDtoBuilder.build();
        String json = mapper.writeValueAsString(itemRequestDto);
        when(itemRequestService.saveRequest(999L, itemRequestDto))
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
    void shouldCreateRequestIfRequestEmptyDescription_ReturnStatus400() throws Exception {
        ItemRequestDto itemRequestDto = itemRequestDtoBuilder.description("").build();
        String json = mapper.writeValueAsString(itemRequestDto);
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
    void shouldCreateRequestIfRequestDescriptionWrongSize_ReturnStatus400() throws Exception {
        byte[] array = new byte[220];
        new Random().nextBytes(array);
        String generatedString = new String(array, StandardCharsets.UTF_8);
        ItemRequestDto itemRequestDto = itemRequestDtoBuilder.description(generatedString).build();
        String json = mapper.writeValueAsString(itemRequestDto);
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].fieldName", is("description")))
                .andExpect(jsonPath("$[0].error", is("size must be between 0 and 200")));
    }

}