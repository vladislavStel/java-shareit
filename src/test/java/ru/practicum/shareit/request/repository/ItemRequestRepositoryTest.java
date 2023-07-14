package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private UserRepository userRepository;

    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "created");
    private static final PageRequest page = PageRequest.of(0, 1, SORT);

    private ItemRequest itemRequest;
    private User requester;

    @BeforeEach
    void setUp() {
        requester = User.builder()
                .name("name")
                .email("email@email.com")
                .build();

        itemRequest = ItemRequest.builder()
                .description("description")
                .requestor(requester)
                .build();
    }

    @Test
    public void contextLoads() {
        assertNotNull(entityManager);
    }

    @Test
    void shouldVerifyBootByPersistRequest() {
        assertNull(itemRequest.getId());
        entityManager.persist(requester);
        entityManager.persist(itemRequest);
        assertNotNull(itemRequest.getId());
    }

    @Test
    void shouldVerifyRepositoryByPersistRequest() {
        assertNull(itemRequest.getId());
        userRepository.save(requester);
        itemRequestRepository.save(itemRequest);
        assertNotNull(itemRequest.getId());
    }

    @Test
    void shouldFindByRequestorId_ReturnEmptyList() {
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestor_Id(1L, SORT);
        assertNotNull(requests);
        assertEquals(0, requests.size());
    }

    @Test
    void shouldFindByRequestorId_ReturnListItemRequestor() {
        entityManager.persist(requester);
        entityManager.persist(itemRequest);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestor_Id(requester.getId(), SORT);
        assertNotNull(requests);
        assertEquals(1, requests.size());
    }

    @Test
    void shouldFindByRequesterIdNotWithPaging_ReturnEmptyList() {
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestor_IdNot(1L, page);
        assertNotNull(requests);
        assertEquals(0, requests.size());
    }

    @Test
    void shouldFindByRequesterIdNotWithPaging_ReturnListItemRequest() {
        entityManager.persist(requester);
        entityManager.persist(itemRequest);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestor_IdNot(2L, page);
        assertEquals(1, requests.size());
    }

    @Test
    void shouldFindByRequesterIdNotWithPaging_ReturnListItemRequestSortedPaging() {
        ItemRequest request2 = new ItemRequest();
        request2.setDescription("description2");
        request2.setRequestor(requester);
        entityManager.persist(requester);
        entityManager.persist(request2);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestor_IdNot(2L, page);
        assertEquals(1, requests.size());
        assertEquals("description2", requests.get(0).getDescription());
    }

}