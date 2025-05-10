package com.siemens.internship.controller;

import com.siemens.internship.model.Item;
import com.siemens.internship.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService){
        this.itemService = itemService;
    }

    /**
     * Retrieves all items.
     */
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return ResponseEntity.ok(itemService.findAll());
    }

    /**
     * Creates a new item with validation.
     */
    @PostMapping
    public ResponseEntity<?> createItem(@Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        Item saved = itemService.save(item);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * Retrieves a specific item by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Updates an item with validation.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        Optional<Item> existing = itemService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        item.setId(id);
        return ResponseEntity.ok(itemService.save(item));
    }

    /**
     * Deletes an item by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (itemService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        itemService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Asynchronously processes all items.
     */
    @GetMapping("/process")
    public ResponseEntity<List<Item>> processItems() throws ExecutionException, InterruptedException {
        List<Item> processedItems = itemService.processItemsAsync().get();
        return ResponseEntity.ok(processedItems);
    }
}
