package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.enums.StatusBooking;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerId(Long userId, Pageable page);

    List<Booking> findByBookerIdAndStartBeforeAndEndBefore(Long userId, LocalDateTime start,
                                                           LocalDateTime end, Pageable page);

    List<Booking> findByBookerIdAndStartAfter(Long userId, LocalDateTime start, Pageable page);

    List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Long userId, LocalDateTime start,
                                                          LocalDateTime end, Pageable page);

    List<Booking> findByBookerIdAndStatus(Long userId, StatusBooking status, Pageable page);

    List<Booking> findAllByItem_Owner_Id(Long userId, Pageable page);

    List<Booking> findAllByItem_Owner_IdAndEndIsBefore(Long userId, LocalDateTime end, Pageable page);

    List<Booking> findAllByItem_Owner_IdAndStartIsAfter(Long userId, LocalDateTime start, Pageable page);

    List<Booking> findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(Long userId, LocalDateTime start,
                                                                      LocalDateTime end, Pageable page);

    List<Booking> findAllByItem_Owner_IdAndStatus(Long userId, StatusBooking status, Pageable page);

    List<Booking> findBookingsByItem_Id(Long itemId);

    List<Booking> findBookingsByItemIn(List<Item> items);

    List<Booking> findBookingsByItem_IdAndStatusOrderByEndAsc(Long itemId, StatusBooking status);

}