package ru.practicum.shareit.request.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestDto {

    @Positive
    Long id;

    @NotBlank
    @Size(max = 200)
    String description;

    @NotNull
    User requestor;

    @NotNull
    @FutureOrPresent
    LocalDate created;

}