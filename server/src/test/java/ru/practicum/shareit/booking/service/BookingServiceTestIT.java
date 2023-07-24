package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.StatusBooking;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceTestIT {

    private final EntityManager entityManager;
    private final BookingService bookingService;

    @Test
    void shouldFindByState() {
        User owner = makeUser("name1", "email1@email.com");
        User booker = makeUser("name2", "email2@email.com");
        entityManager.persist(owner);
        entityManager.persist(booker);

        Item item = makeAvailableItem("name", "description", owner);
        entityManager.persist(item);

        LocalDateTime now = LocalDateTime.now();
        List<BookingCreateDto> bookings = List.of(
                makeBookingCreateDto(now, now.plusMinutes(5), item.getId()),
                makeBookingCreateDto(now.plusMinutes(10), now.plusMinutes(15), item.getId()),
                makeBookingCreateDto(now.plusMinutes(20), now.plusMinutes(25), item.getId())
        );
        bookings.stream()
                .map(b -> BookingMapper.toBooking(b, booker, item))
                .forEach(booking -> {
                    booking.setStatus(StatusBooking.WAITING);
                    entityManager.persist(booking);
                });

        entityManager.flush();

        List<BookingDto> targetBookings = bookingService.getBookingsCurrentUser(booker.getId(),
                "ALL", 0, 10);

        assertThat(targetBookings, hasSize(bookings.size()));
        for (BookingCreateDto sourceBooking : bookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    private BookingCreateDto makeBookingCreateDto(LocalDateTime start,
                                          LocalDateTime end,
                                          long itemId) {
        return BookingCreateDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .status(StatusBooking.WAITING)
                .build();
    }

    private User makeUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private Item makeAvailableItem(String name, String description, User owner) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setIsAvailable(true);
        item.setOwner(owner);
        return item;
    }

}