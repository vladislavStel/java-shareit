package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.StatusBooking;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.UnsupportedStateException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.validation.DateValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "start");
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Mock
    BookingRepository bookingRepository;
    @Mock
    private UserService userService;
    @Mock
    private ItemService itemService;
    @Mock
    private DateValidator dateValidator;
    @InjectMocks
    BookingServiceImpl bookingService;

    private User owner;
    private User booker;

    private User user;
    private Item item;
    private Booking booking;
    private BookingCreateDto bookingCreateDto;

    @BeforeEach
    void setUp() {
        LocalDateTime start = NOW.minusSeconds(120);
        LocalDateTime end = NOW.minusSeconds(60);
        owner = User.builder()
                .id(1L)
                .name("Name")
                .email("email@email.com")
                .build();

        booker = User.builder()
                .id(2L)
                .name("Name2")
                .email("email2@email.com")
                .build();

        user = User.builder()
                .id(3L)
                .name("Name3")
                .email("email3@email.com")
                .build();

        item = Item.builder()
                .id(1L)
                .name("NameItem")
                .description("description")
                .isAvailable(true)
                .owner(owner)
                .build();

        booking = Booking.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(StatusBooking.APPROVED)
                .build();

        bookingCreateDto = BookingCreateDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(end)
                .build();
    }

    @Test
    void shouldGetBookingsCurrentUserWhenDifferentState() {
        int from = 0;
        int size = 1;
        long userId = booker.getId();
        PageRequest page = PageRequest.of(0, size, SORT);

        // All
        when(bookingRepository.findByBookerId(userId, page)).thenReturn(List.of(booking));
        List<BookingDto> bookingDtos = bookingService.getBookingsCurrentUser(userId, "ALL", from, size);

        assertNotNull(bookingDtos);
        assertEquals(1, bookingDtos.size());
        assertEquals(booking.getId(), bookingDtos.get(0).getId());

        // PAST
        when(bookingRepository.findByBookerIdAndStartBeforeAndEndBefore(anyLong(),
                any(), any(), any())).thenReturn(List.of(booking));

        bookingDtos = bookingService.getBookingsCurrentUser(userId, "PAST", from, size);

        assertNotNull(bookingDtos);
        assertEquals(1, bookingDtos.size());

        // FUTURE
        booking.setStart(NOW.plusSeconds(60));

        when(bookingRepository.findByBookerIdAndStartAfter(anyLong(),
                any(), any())).thenReturn(List.of(booking));

        bookingDtos = bookingService.getBookingsCurrentUser(userId, "FUTURE", from, size);

        assertNotNull(bookingDtos);
        assertEquals(1, bookingDtos.size());

        // CURRENT
        booking.setEnd(NOW.plusSeconds(120));

        when(bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(anyLong(),
                any(), any(), any())).thenReturn(List.of(booking));

        bookingDtos = bookingService.getBookingsCurrentUser(userId, "CURRENT", from, size);

        assertNotNull(bookingDtos);
        assertEquals(1, bookingDtos.size());

        // WAITING
        booking.setStatus(StatusBooking.WAITING);

        when(bookingRepository.findByBookerIdAndStatus(anyLong(),
                any(), any())).thenReturn(List.of(booking));

        bookingDtos = bookingService.getBookingsCurrentUser(userId, "WAITING", from, size);

        assertNotNull(bookingDtos);
        assertEquals(1, bookingDtos.size());

        // REJECTED
        booking.setStatus(StatusBooking.REJECTED);

        bookingDtos = bookingService.getBookingsCurrentUser(userId, "REJECTED", from, size);

        assertNotNull(bookingDtos);
        assertEquals(1, bookingDtos.size());

        // Wrong State
        String error = "Unknown state: ERROR";
        UnsupportedStateException exception = assertThrows(UnsupportedStateException.class,
                () -> bookingService.getBookingsCurrentUser(userId, "ERROR", from, size));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void getBookingsAllItemCurrentUser() {
        int from = 0;
        int size = 1;
        long userId = owner.getId();
        PageRequest page = PageRequest.of(0, size, SORT);

        // ALL
        when(bookingRepository.findAllByItem_Owner_Id(userId, page)).thenReturn(List.of(booking));

        List<BookingDto> bookingOutDtos = bookingService.getBookingsAllItemCurrentUser(userId, "ALL", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertEquals(booking.getId(), bookingOutDtos.get(0).getId());

        // PAST
        when(bookingRepository.findAllByItem_Owner_IdAndEndIsBefore(anyLong(),
                any(), any())).thenReturn(List.of(booking));

        bookingOutDtos = bookingService.getBookingsAllItemCurrentUser(userId, "PAST", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        // FUTURE
        booking.setStart(NOW.plusSeconds(60));
        when(bookingRepository.findAllByItem_Owner_IdAndStartIsAfter(anyLong(),
                any(), any())).thenReturn(List.of(booking));

        bookingOutDtos = bookingService.getBookingsAllItemCurrentUser(userId, "FUTURE", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        // CURRENT
        booking.setEnd(NOW.plusSeconds(120));
        when(bookingRepository.findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(anyLong(),
                any(), any(), any())).thenReturn(List.of(booking));

        bookingOutDtos = bookingService.getBookingsAllItemCurrentUser(userId, "CURRENT", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        // WAITING
        booking.setStatus(StatusBooking.WAITING);
        when(bookingRepository.findAllByItem_Owner_IdAndStatus(anyLong(),
                any(), any())).thenReturn(List.of(booking));

        bookingOutDtos = bookingService.getBookingsAllItemCurrentUser(userId, "WAITING", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        // REJECTED
        booking.setStatus(StatusBooking.REJECTED);
        when(bookingRepository.findAllByItem_Owner_IdAndStatus(anyLong(),
                any(), any())).thenReturn(List.of(booking));

        bookingOutDtos = bookingService.getBookingsAllItemCurrentUser(userId, "REJECTED", from, size);

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        // Wrong State
        String error = "Unknown state: ERROR";
        UnsupportedStateException exception = assertThrows(UnsupportedStateException.class,
                () -> bookingService.getBookingsCurrentUser(userId, "ERROR", from, size));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void shouldGetBookingByIdWithBooker_ReturnBookingDto() {
        long bookerId = owner.getId();
        long bookingId = booking.getId();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        BookingDto bookingDto = bookingService.getBookingById(bookerId, bookingId);
        assertNotNull(bookingDto);
        assertEquals(booking.getId(), bookingDto.getId());
    }

    @Test
    void shouldGetBookingByIdWhenWrongUser_ReturnObjectNotFoundException() {
        long userId = user.getId();
        long bookingId = booking.getId();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        String error = String.format("Wrong user: id=%d", userId);
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getBookingById(userId, bookingId));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void shouldCreateBooking_ReturnBookingDto() {
        long bookerId = booker.getId();
        long itemId = item.getId();

        when(userService.getById(bookerId)).thenReturn(booker);
        when(itemService.getById(itemId)).thenReturn(item);
        when(bookingRepository.save(any())).thenReturn(booking);
        when(dateValidator.isCorrectDate(any(), any())).thenReturn(true);

        BookingDto bookingOutDto = bookingService.createBooking(bookerId, bookingCreateDto);

        assertNotNull(bookingOutDto);
        assertEquals(booking.getId(), bookingOutDto.getId());
    }

    @Test
    void shouldCreateBookingWhenItemNotAvailable_ReturnObjectNotFoundException() {
        item.setIsAvailable(false);
        long bookerId = booker.getId();
        long itemId = item.getId();
        when(userService.getById(bookerId)).thenReturn(booker);
        when(itemService.getById(itemId)).thenReturn(item);
        String error = String.format("Item with id %d is not available", itemId);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookerId, bookingCreateDto));
        assertEquals(error, ex.getMessage());
    }

    @Test
    void shouldCreateBookingWhenFailDateValidation_ReturnValidationException() {
        long bookerId = booker.getId();
        long itemId = item.getId();
        when(itemService.getById(itemId)).thenReturn(item);
        when(dateValidator.isCorrectDate(any(), any())).thenReturn(false);
        bookingCreateDto = BookingCreateDto.builder()
                .itemId(item.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
        String error = "Date is not correct";

        ValidationException ex = assertThrows(ValidationException.class,
                    () -> bookingService.createBooking(bookerId, bookingCreateDto));
        assertEquals(error, ex.getMessage());
    }

    @Test
    void shouldCreateBookingWhenFailOwnerItem_ReturnObjectNotFoundException() {
        long ownerId = owner.getId();
        long itemId = item.getId();

        when(userService.getById(ownerId)).thenReturn(owner);
        when(itemService.getById(itemId)).thenReturn(item);
        String error = String.format("Item with id %d is not available for booking", item.getId());

        ObjectNotFoundException exception = assertThrows(
                ObjectNotFoundException.class, () -> bookingService.createBooking(ownerId, bookingCreateDto));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void shouldApproveBooking_ReturnUpdatedBookingDto() {
        long userId = owner.getId();
        long bookingId = booking.getId();
        booking.setStatus(StatusBooking.WAITING);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(userService.getById(userId)).thenReturn(owner);
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDto bookingDto = bookingService.approveBooking(userId, bookingId, false);

        assertNotNull(bookingDto);
        assertEquals(booking.getId(), bookingDto.getId());
    }

    @Test
    void shouldApproveBookingWhenBookingNotFound_ReturnObjectNotFoundException() {
        long userId = owner.getId();
        long bookingId = booking.getId();
        String error = String.format("Booking not found: id=%d", bookingId);

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                    () -> bookingService.approveBooking(userId, bookingId, true));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void shouldApproveBookingWhenNotOwner_ReturnObjectNotFoundException() {
        long userId = owner.getId();
        long bookingId = booking.getId();
        String error = "You are not the owner of this item!";
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.approveBooking(userId, bookingId, true));
        assertEquals(error, exception.getMessage());
    }

    @Test
    void shouldApproveBookingWhenNotAvailable_ReturnValidationException() {
        long userId = owner.getId();
        long bookingId = booking.getId();
        String error = String.format("Booking not available: id=%d", bookingId);
        when(userService.getById(userId)).thenReturn(owner);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.approveBooking(userId, bookingId, true));
        assertEquals(error, exception.getMessage());
    }

}