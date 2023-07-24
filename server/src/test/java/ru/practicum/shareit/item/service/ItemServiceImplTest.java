package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.enums.StatusBooking;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    ItemRepository itemRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    UserService userService;
    @InjectMocks
    ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private Comment comment;

    @BeforeEach
    void setUp() {
        LocalDateTime start = LocalDateTime.now().minusSeconds(120);
        LocalDateTime end = LocalDateTime.now().minusSeconds(60);
        owner = User.builder()
                .id(1L)
                .name("Name")
                .email("email@email.com")
                .build();

        booker = User.builder()
                .id(2L)
                .name("Name2")
                .email("email2@email.com")
                .build();

        item = Item.builder()
                .id(1L)
                .name("NameItem")
                .description("Description")
                .isAvailable(true)
                .owner(owner)
                .build();

        booking = Booking.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(StatusBooking.APPROVED)
                .build();

        comment = Comment.builder()
                .id(1L)
                .text("Text")
                .author(booker)
                .item(item)
                .created(LocalDateTime.now())
                .build();
        }

    @Test
    void shouldGetAllItemsByUserId_ReturnEmptyList() {
        long userId = booker.getId();
        when(itemRepository.findAllByOwnerId(any(), any())).thenReturn(Collections.emptyList());

        List<ItemOwnerDto> itemDtos = itemService.getAllItemsByUserId(userId, 0, 1);

        assertNotNull(itemDtos);
        assertEquals(0, itemDtos.size());
    }

    @Test
    void shouldGetAllItemsByUserId_ReturnListItems() {
        long userId = owner.getId();
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by("id"));
        when(itemRepository.findAllByOwnerId(userId, pageRequest)).thenReturn(List.of(item));
        when(bookingRepository.findBookingsByItemIn(List.of(item))).thenReturn(List.of(booking));

        List<ItemOwnerDto> itemOwnerDtos = itemService.getAllItemsByUserId(userId, 0, 1);

        assertNotNull(itemOwnerDtos);
        assertEquals(1, itemOwnerDtos.size());
        assertEquals(booking.getId(), itemOwnerDtos.get(0).getLastBooking().getId());
    }

    @Test
    void shouldGetItemById_ReturnItem() {
        long ownerId = owner.getId();
        long itemId = item.getId();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findBookingsByItem_Id(itemId)).thenReturn(List.of(booking));
        when(commentRepository.findCommentsByItem_Id(itemId)).thenReturn(List.of(comment));

        ItemOwnerDto itemOwnerDto = itemService.getItemById(ownerId, itemId);

        assertNotNull(itemOwnerDto);
        assertEquals(itemId, itemOwnerDto.getId());
        assertEquals(comment.getId(), itemOwnerDto.getComments().get(0).getId());
    }

    @Test
    void shouldGetSearchItem_ReturnListItems() {
        when(itemRepository.search(any(), any())).thenReturn(List.of(item));

        List<ItemDto> itemDtos = itemService.getSearchItem("NameItem", 0, 1);

        assertNotNull(itemDtos);
        assertEquals(1, itemDtos.size());
        assertEquals(item.getId(), itemDtos.get(0).getId());
    }

    @Test
    void shouldGetSearchItem_ReturnEmptyList() {
        List<ItemDto> itemDtos = itemService.getSearchItem("", 0, 1);

        assertNotNull(itemDtos);
        assertEquals(0, itemDtos.size());
    }

    @Test
    void shouldSaveItem_ReturnSavedItemDto() {
        long userId = owner.getId();
        long itemId = item.getId();
        when(itemRepository.save(any())).thenReturn(item);

        ItemDto saveItemDto = ItemDto.builder()
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getIsAvailable())
                .build();
        ItemDto itemDto = itemService.saveItem(userId, saveItemDto);
        assertNotNull(itemDto);
        assertEquals(itemId, itemDto.getId());
        verify(itemRepository, times(1)).save(any());
    }

    @Test
    void shouldUpdateItem_ReturnItemDto() {
        long userId = owner.getId();
        long itemId = item.getId();
        when(userService.getById(userId)).thenReturn(owner);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        String newName = "nameUpdate";
        String newDescription = "newDescription";
        item.setName(newName);
        item.setDescription(newDescription);
        when(itemRepository.save(any())).thenReturn(item);
        ItemDto itemDtoToUpdate = ItemDto.builder()
                .name(newName)
                .description(newDescription)
                .build();
        ItemDto itemDto = itemService.updateItem(userId, itemId, itemDtoToUpdate);
        assertNotNull(itemDto);
        assertEquals("nameUpdate", itemDto.getName());
    }

    @Test
    void shouldCreateComment_ReturnCommentDto() {
        long userId = booker.getId();
        long itemId = item.getId();
        when(userService.getById(userId)).thenReturn(booker);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository
                .findBookingsByItem_IdAndStatusOrderByEndAsc(itemId, StatusBooking.APPROVED))
                .thenReturn(List.of(booking));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        CommentDto commentDto = CommentDto.builder().text("text").build();

        CommentDto commentDtoOut = itemService.createComment(userId, itemId, commentDto);

        assertNotNull(commentDtoOut);
        assertEquals(comment.getId(), commentDtoOut.getId());
        verify(commentRepository, times(1)).save(any());
    }

    @Test
    void shouldCreateCommentWhenNotBookingCompleted_ReturnValidationException() {
        long itemId = item.getId();
        long ownerId = owner.getId();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository
                .findBookingsByItem_IdAndStatusOrderByEndAsc(anyLong(), any()))
                .thenReturn(Collections.emptyList());
        String error = "You can add a comment only after the booking is completed.";

        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.createComment(ownerId, itemId, CommentDto.builder().text("text").build()));

        assertEquals(error, exception.getMessage());
    }

}