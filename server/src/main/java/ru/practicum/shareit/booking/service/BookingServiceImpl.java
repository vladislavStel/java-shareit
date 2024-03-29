package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.StatusBooking;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.UnsupportedStateException;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemService itemService;
    private final UserService userService;

    @Override
    public List<BookingDto> getBookingsCurrentUser(Long userId, String state, Integer from, Integer size) {
        userService.validateUserById(userId);
        List<Booking> bookings;
        LocalDateTime time = LocalDateTime.now();
        Pageable page = PageRequest.of(from / size, size, Sort.by("start").descending());
        switch (state.toUpperCase()) {
            case "ALL":
                bookings = bookingRepository.findByBookerId(userId, page);
                break;
            case "PAST":
                bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndBefore(
                        userId, time, time, page);
                break;
            case "FUTURE":
                bookings = bookingRepository.findByBookerIdAndStartAfter(userId, time, page);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(
                        userId, time, time, page);
                break;
            case "WAITING":
                bookings = bookingRepository.findByBookerIdAndStatus(userId, StatusBooking.WAITING, page);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByBookerIdAndStatus(userId, StatusBooking.REJECTED, page);
                break;
            default:
                throw new UnsupportedStateException(String.format("Unknown state: %s", state));
        }
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getBookingsAllItemCurrentUser(Long userId, String state, Integer from, Integer size) {
        userService.validateUserById(userId);
        List<Booking> bookings;
        LocalDateTime time = LocalDateTime.now();
        Pageable page = PageRequest.of(from / size, size, Sort.by("start").descending());
        switch (state.toUpperCase()) {
            case "ALL":
                bookings = bookingRepository.findAllByItem_Owner_Id(userId, page);
                break;
            case "PAST":
                bookings = bookingRepository.findAllByItem_Owner_IdAndEndIsBefore(userId, time, page);
                break;
            case "FUTURE":
                bookings = bookingRepository.findAllByItem_Owner_IdAndStartIsAfter(userId, time, page);
                break;
            case "CURRENT":
                bookings = bookingRepository.findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(userId,
                        time, time, page);
                break;
            case "WAITING":
                bookings = bookingRepository.findAllByItem_Owner_IdAndStatus(userId,
                        StatusBooking.WAITING, page);
                break;
            case "REJECTED":
                bookings = bookingRepository.findAllByItem_Owner_IdAndStatus(userId,
                        StatusBooking.REJECTED, page);
                break;
            default:
                throw new UnsupportedStateException(String.format("Unknown state: %s", state));
        }
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Booking not found: id=%d", bookingId)));
        if (Objects.equals(booking.getBooker().getId(), userId) ||
                Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            return BookingMapper.toBookingDto(booking);
        }
        throw new ObjectNotFoundException(String.format("Wrong user: id=%d", userId));
    }

    @Transactional
    @Override
    public BookingDto createBooking(Long userId, BookingCreateDto bookingCreateDto) {
        User user = userService.getById(userId);
        Item item = itemService.getById(bookingCreateDto.getItemId());
        if (Objects.equals(item.getOwner(), user)) {
            throw new ObjectNotFoundException(String
                    .format("Item with id %d is not available for booking", item.getId()));
        }
        if (!item.getIsAvailable()) {
            throw new ValidationException(String.format("Item with id %d is not available", item.getId()));
        }
        bookingCreateDto.setBookerId(user.getId());
        bookingCreateDto.setStatus(StatusBooking.WAITING);
        return BookingMapper.toBookingDto(bookingRepository.save(BookingMapper
                .toBooking(bookingCreateDto, user, item)));
    }

    @Transactional
    @Override
    public BookingDto approveBooking(Long userId, Long bookingId, Boolean approve) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Booking not found: id=%d", bookingId)));
        User user = userService.getById(userId);
        if (!Objects.equals(booking.getItem().getOwner(), user)) {
            throw new ObjectNotFoundException("You are not the owner of this item!");
        }
        if (booking.getStatus() != StatusBooking.WAITING) {
            throw new ValidationException(String.format("Booking not available: id=%d", bookingId));
        }
        booking.setStatus(approve ? StatusBooking.APPROVED : StatusBooking.REJECTED);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

}