package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    JavaTimeModule module = new JavaTimeModule();
    ObjectMapper mapper = new ObjectMapper().registerModule(module)
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm"));

    @Test
    void shouldMockMvc() {
        assertNotNull(mockMvc);
    }

    @ParameterizedTest
    @ArgumentsSource(BookingProvider.class)
    void shouldCreateBookingIfFieldNull_ReturnStatus400(BookingCreateDto bookingCreateDto) throws Exception {
        String json = mapper.writeValueAsString(bookingCreateDto);
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].error", is("must not be null")));
    }

    @Test
    void shouldGetBookingsCurrentUserIfFromNegative_ReturnStatus400() throws Exception {
        mockMvc.perform(get(url)
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "WAITING")
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].error", is("must be greater than or equal to 0")));
    }

    @Test
    void shouldGetBookingsAllItemCurrentUserIfFromNegative_ReturnStatus400() throws Exception {
        mockMvc.perform(get(url + "/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "WAITING")
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].error", is("must be greater than or equal to 0")));
    }

    private static class BookingProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    BookingCreateDto.builder().itemId(null).start(LocalDateTime.now().plusMinutes(1))
                            .end(LocalDateTime.now().plusMinutes(2)).build(),
                    BookingCreateDto.builder().itemId(1L).start(null).end(LocalDateTime.now().plusMinutes(2)).build(),
                    BookingCreateDto.builder().itemId(1L).start(LocalDateTime.now().plusMinutes(1)).end(null).build())
                    .map(Arguments::of);
        }
    }

}