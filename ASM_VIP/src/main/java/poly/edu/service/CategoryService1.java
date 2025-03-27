package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.entity.CategoryEntity;
import poly.edu.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService1 {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryEntity> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<CategoryEntity> getCategoryById(int id) {
        return categoryRepository.findById(id);
    }

    public CategoryEntity saveCategory(CategoryEntity category) {
        return Optional.ofNullable(category)
                .map(categoryRepository::save)
                .orElse(null);
    }

    public void deleteCategory(int id) {
        Optional.of(id).filter(i -> i > 0).ifPresent(categoryRepository::deleteById);
    }
}