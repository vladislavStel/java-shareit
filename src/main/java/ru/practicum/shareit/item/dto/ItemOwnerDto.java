package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.item.comment.dto.CommentDto;

import java.util.Set;

@Getter @Setter @Builder @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemOwnerDto {

    Long id;

    String name;

    String description;

    Boolean available;

    BookingItemDto lastBooking;

    BookingItemDto nextBooking;

    Long requestId;

    Set<CommentDto> comments;

}