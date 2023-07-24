package ru.practicum.shareit.user.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.validation.GroupValidation.Create;
import ru.practicum.shareit.validation.GroupValidation.Update;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {

    Long id;

    @NotBlank(groups = {Create.class})
    @Pattern(regexp = "[A-Za-z]+(?:(?:, |-)[A-Za-z]+)*", groups = {Create.class, Update.class})
    String name;

    @NotBlank(groups = {Create.class})
    @Email(regexp = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$", groups = {Create.class, Update.class})
    String email;

}