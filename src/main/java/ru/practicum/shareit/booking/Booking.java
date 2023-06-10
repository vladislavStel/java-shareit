package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.enums.StatusBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {

    @Positive
    Long id;

    @NotNull
    @FutureOrPresent
    LocalDate start;

    @NotNull
    @FutureOrPresent
    LocalDate end;

    @NotNull
    Item item;

    @NotNull
    User booker;

    @NotNull
    StatusBooking status;

}