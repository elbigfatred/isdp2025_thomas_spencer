package tom.ims.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tom.ims.backend.model.Employee;
import tom.ims.backend.model.Item;
import tom.ims.backend.repository.EmployeeRepository;
import tom.ims.backend.repository.ItemRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public Item getItemById(int id) {
        Optional<Item> optionalItem = itemRepository.findById(id);
        return optionalItem.orElse(null); // Return the item or null if not found
    }

    // Save or update an item
    public void saveItem(Item item) {
        itemRepository.save(item); // This will handle both insert and update
    }

}

