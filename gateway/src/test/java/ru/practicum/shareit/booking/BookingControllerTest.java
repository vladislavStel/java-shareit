package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.StatusBooking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private final BookingClient bookingClient;

    String url = "/bookings";

    UserDto.UserDtoBuilder userDtoBuilder;
    ItemDto.ItemDtoBuilder itemDtoBuilder;
    BookingCreateDto.BookingCreateDtoBuilder bookingCreateDtoBuilder;
    BookingDto.BookingDtoBuilder bookingDtoBuilder;

    JavaTimeModule module = new JavaTimeModule();
    ObjectMapper mapper = new ObjectMapper().registerModule(module)
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm"));

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        userDtoBuilder = UserDto.builder()
                .id(1L)
                .name("name")
                .email("email@email.ru");
        itemDtoBuilder = ItemDto.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(true);
        bookingCreateDtoBuilder = BookingCreateDto.builder()
                .itemId(1L)
                .start(now.plusMinutes(1))
                .end(now.plusMinutes(2));
        bookingDtoBuilder = BookingDto.builder()
                .id(1L)
                .booker(userDtoBuilder.build())
                .item(itemDtoBuilder.build())
                .start(now.plusMinutes(1))
                .end(now.plusMinutes(2))
                .status(StatusBooking.WAITING);
    }

    @Test
    void shouldMockMvc() {
        assertNotNull(mockMvc);
    }

    @Test
    void shouldCreateBookingIfItemIdNull_ReturnStatus400() throws Exception {
        BookingCreateDto bookingCreateDto = bookingCreateDtoBuilder.itemId(null).build();
        String json = mapper.writeValueAsString(bookingCreateDto);
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].fieldName", is("itemId")))
                .andExpect(jsonPath("$[0].error", is("must not be null")));
    }

    @Test
    void shouldCreateBookingIfStartTimeNull_ReturnStatus400() throws Exception {
        BookingCreateDto bookingCreateDto = bookingCreateDtoBuilder.start(null).build();
        String json = mapper.writeValueAsString(bookingCreateDto);
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].fieldName", is("start")))
                .andExpect(jsonPath("$[0].error", is("must not be null")));
    }

    @Test
    void shouldCreateBookingIfEndTimeNull_ReturnStatus400() throws Exception {
        BookingCreateDto bookingCreateDto = bookingCreateDtoBuilder.end(null).build();
        String json = mapper.writeValueAsString(bookingCreateDto);
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].fieldName", is("end")))
                .andExpect(jsonPath("$[0].error", is("must not be null")));
    }

}