package ru.practicum.shareit.item.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Item {

    Long id;

    String name;

    String description;

    Boolean isAvailable;

    User owner;

    ItemRequest request;

    public Item(String name, String description, Boolean isAvailable, User owner, ItemRequest request) {
        this.name = name;
        this.description = description;
        this.isAvailable = isAvailable;
        this.owner = owner;
        this.request = request;
    }

}