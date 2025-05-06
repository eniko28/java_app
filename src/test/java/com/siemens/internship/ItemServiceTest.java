package com.siemens.internship;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        Item item1 = new Item(1L, "Item1", "Desc1", "NEW", "email1@example.com");
        Item item2 = new Item(2L, "Item2", "Desc2", "NEW", "email2@example.com");

        when(itemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

        List<Item> result = itemService.findAll();

        assertEquals(2, result.size());
        assertEquals("Item1", result.get(0).getName());
    }

    @Test
    void testFindByIdFound() {
        Item item = new Item(1L, "Item1", "Desc1", "NEW", "email@example.com");
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Optional<Item> result = itemService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Item1", result.get().getName());
    }

    @Test
    void testFindByIdNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Item> result = itemService.findById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void testSave() {
        Item item = new Item(null, "NewItem", "NewDesc", "NEW", "valid@email.com");
        Item saved = new Item(1L, "NewItem", "NewDesc", "NEW", "valid@email.com");

        when(itemRepository.save(item)).thenReturn(saved);

        Item result = itemService.save(item);

        assertNotNull(result.getId());
        assertEquals("NewItem", result.getName());
    }

    @Test
    void testProcessItemsAsync() throws Exception {
        // Setup dummy IDs and items
        when(itemRepository.findAllIds()).thenReturn(Arrays.asList(1L, 2L));

        Item item1 = new Item(1L, "Item1", "Desc", "NEW", "test1@email.com");
        Item item2 = new Item(2L, "Item2", "Desc", "NEW", "test2@email.com");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));

        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> processedItems = future.get();

        assertEquals(2, processedItems.size());
        for (Item item : processedItems) {
            assertEquals("PROCESSED", item.getStatus());
        }

        verify(itemRepository, times(2)).save(any(Item.class));
    }

    @Test
    void testDeleteById() {
        doNothing().when(itemRepository).deleteById(1L);

        itemService.deleteById(1L);

        verify(itemRepository, times(1)).deleteById(1L);
    }
}
