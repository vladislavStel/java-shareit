package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.dto.CommentCreateDto;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;

import java.util.List;

public interface ItemService {

    List<ItemOwnerDto> getAllItemsByUserId(Long userId, Integer from, Integer size);

    ItemOwnerDto getItemById(Long userId, Long itemId);

    List<ItemDto> getSearchItem(String text, Integer from, Integer size);

    ItemDto saveItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    CommentDto createComment(Long userId, Long itemId, CommentCreateDto commentCreateDto);

}