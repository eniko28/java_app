package com.siemens.internship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    /**
     * Process all items asynchronously and return successfully processed items
     */
    public CompletableFuture<List<Item>> processItemsAsync() {
        List<Long> ids = itemRepository.findAllIds();
        List<CompletableFuture<Item>> futures = new ArrayList<>();

        for (Long id : ids) {
            futures.add(
                    CompletableFuture.supplyAsync(() -> {
                        Optional<Item> optionalItem = itemRepository.findById(id);
                        if (optionalItem.isEmpty()) return null;

                        Item item = optionalItem.get();
                        item.setStatus("PROCESSED");
                        return itemRepository.save(item);
                    }, executor).exceptionally(ex -> {
                        System.err.println("Failed to process item with ID: " + id + ", Error: " + ex.getMessage());
                        return null;
                    })
            );
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }
}


