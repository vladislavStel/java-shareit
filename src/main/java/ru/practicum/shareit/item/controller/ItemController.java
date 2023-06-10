package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.validation.GroupValidation.Create;

import javax.validation.constraints.Positive;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public List<ItemDto> getUserItems(@Validated @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemService.getUserItems(userId);
    }

    @GetMapping("{id}")
    public ItemDto getItemById(@PathVariable("id") @Positive Long itemId) {
        return itemService.getItemById(itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> getSearchResultItem(@Validated @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                             @RequestParam String text) {
        return itemService.getSearchResultItem(userId, text);
    }

    @PostMapping
    public ItemDto createItem(@Validated @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                              @Validated(Create.class) @RequestBody ItemDto itemDto) {
        return itemService.saveItem(userId, itemDto);
    }

    @PatchMapping("{id}")
    public ItemDto updateItem(@Validated @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                              @PathVariable("id") @Positive Long itemId, @RequestBody ItemDto itemDto) {
        return itemService.updateItem(userId, itemId, itemDto);
    }

}