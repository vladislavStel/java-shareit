package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.dto.BookingItemDto;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemOwnerDto {

    Long id;

    String name;

    String description;

    Boolean available;

    BookingItemDto lastBooking;

    BookingItemDto nextBooking;

    Long requestId;

    List<CommentDto> comments;

}