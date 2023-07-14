package ru.practicum.shareit.booking.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.enums.StatusBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "start_date", nullable = false)
    LocalDateTime start;

    @Column(name = "end_date", nullable = false)
    LocalDateTime end;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "item_id", nullable = false)
    Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "booker_id", nullable = false)
    User booker;

    @Enumerated(EnumType.STRING)
    StatusBooking status;

}