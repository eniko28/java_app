package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ItemServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRepository itemRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Item sampleItem;

    @BeforeEach
    void setup() {
        sampleItem = new Item(1L, "Test Item", "Sample description", "NEW", "test@example.com");
    }

    @Test
    void shouldCreateValidItem() throws Exception {
        when(itemRepository.save(any(Item.class))).thenReturn(sampleItem);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleItem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Test Item")))
                .andExpect(jsonPath("$.email", is("test@example.com")));
    }

    @Test
    void shouldRejectInvalidEmail() throws Exception {
        sampleItem.setEmail("invalid_email");

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleItem)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].defaultMessage", containsString("valid")));
    }

    @Test
    void shouldReturnAllItems() throws Exception {
        when(itemRepository.findAll()).thenReturn(List.of(sampleItem));

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Item")));
    }

    @Test
    void shouldReturnItemById() throws Exception {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test Item")));
    }

    @Test
    void shouldReturnNotFoundForMissingItem() throws Exception {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateItem() throws Exception {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(itemRepository.save(any(Item.class))).thenReturn(sampleItem);

        sampleItem.setDescription("Updated");

        mockMvc.perform(put("/api/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("Updated")));
    }

    @Test
    void shouldDeleteItem() throws Exception {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));
        doNothing().when(itemRepository).deleteById(1L);

        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldProcessItemsAsync() throws Exception {
        Item item2 = new Item(2L, "Item 2", "Desc", "NEW", "mail2@test.com");
        when(itemRepository.findAllIds()).thenReturn(Arrays.asList(1L, 2L));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
        when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArguments()[0]);

        mockMvc.perform(get("/api/items/process"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status", is("PROCESSED")));
    }
}
