package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    private User author;
    private User owner;
    private Item item;
    private Item item1;
    private Comment comment;

    @BeforeEach
    void setUp() {
        author = User.builder()
                .name("name")
                .email("email@email.com")
                .build();
        author = userRepository.save(author);

        owner = User.builder()
                .name("name2")
                .email("email2@email.com")
                .build();
        owner = userRepository.save(owner);

        item = Item.builder()
                .name("name")
                .description("description")
                .isAvailable(true)
                .owner(owner)
                .build();
        itemRepository.save(item);

        item1 = Item.builder()
                .name("name1")
                .description("description1")
                .owner(owner)
                .isAvailable(true)
                .build();
        itemRepository.save(item1);

        comment = Comment.builder()
                .text("comment")
                .item(item)
                .author(author)
                .build();
        commentRepository.save(comment);
    }

    @Test
    void contextLoads() {
        assertNotNull(testEntityManager);
    }

    @Test
    void shouldFindAllByItemId_ReturnEmptyList() {
        //EmptyList
        List<Comment> comments = commentRepository.findCommentsByItem_Id(99L);
        assertNotNull(comments);
        assertEquals(0, comments.size());
    }

    @Test
    void shouldFindAllByItemId_ReturnListComments() {
        //Single List
        List<Comment> comments = commentRepository.findCommentsByItem_Id(comment.getItem().getId());
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals(comments.get(0).getId(), comment.getId());
    }

    @Test
    void shouldFindAllByItemId_ReturnRollback() {
        List<Comment> comments = commentRepository.findAll();
        assertEquals(1, comments.size());
    }

    @Test
    void shouldFindAllByItemIdIn_ReturnEmptyList() {
        List<Comment> comments = commentRepository.findCommentsByItemIn(List.of(item1));
        assertNotNull(comments);
        assertEquals(0, comments.size());
    }

    @Test
    void shouldFindAllByItemIdIn_ReturnListComments() {
        List<Comment> comments = commentRepository.findCommentsByItemIn(List.of(item));
        assertNotNull(comments);
        assertEquals(1, comments.size());
    }

    @Test
    void shouldFindAllByItemIdInWhenSort_ReturnListComments() {
        Comment comment1 = new Comment();
        comment1.setText("comment1");
        comment1.setItem(item1);
        comment1.setAuthor(author);
        commentRepository.save(comment1);

        List<Comment> comments = commentRepository.findCommentsByItemIn(List.of(item, item1));
        assertNotNull(comments);
        assertEquals(2, comments.size());
        assertEquals(comments.get(1).getId(), comment1.getId());
    }

}