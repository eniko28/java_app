package com.siemens.internship.service;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository){
        this.itemRepository = itemRepository;
    }

    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    /**
     * Asynchronously processes all items by retrieving, updating status,
     * and saving each one. Tracks and returns successfully processed items.
     *
     * @return CompletableFuture containing a list of successfully processed items.
     */
    public CompletableFuture<List<Item>> processItemsAsync() {
        List<Long> ids = itemRepository.findAllIds();

        List<CompletableFuture<Optional<Item>>> futures = ids.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> {
                    try {
                        Optional<Item> itemOpt = itemRepository.findById(id);
                        if (itemOpt.isPresent()) {
                            Item item = itemOpt.get();
                            item.setStatus("PROCESSED");
                            return Optional.of(itemRepository.save(item));
                        }
                    } catch (Exception e) {
                        System.err.println("Error processing item ID " + id + ": " + e.getMessage());
                    }
                    return Optional.<Item>empty(); // <-- Explicitly typed
                }, executor))
                .collect(Collectors.toList()); // Use collect instead of toList() for full type inference

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
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
