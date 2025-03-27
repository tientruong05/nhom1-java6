package poly.edu.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.CategoryEntity;
import poly.edu.entity.SubCategoryEntity;
import poly.edu.service.CategoryService;
import poly.edu.service.SubCategoryService;

import java.util.Optional;

@Controller
@RequestMapping("/java5/asm/crud/categories")
public class SubCategoryController {

    @Autowired
    private SubCategoryService subCategoryService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String getSubCategoriesByCategory(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(required = false) String categoryName,
                                             Model model) {
        String selectedCategory = Optional.ofNullable(categoryName)
                .filter(name -> !name.trim().isEmpty())
                .orElseGet(() -> categoryService.getAllCategories().stream()
                        .findFirst()
                        .map(CategoryEntity::getName)
                        .orElse(""));

        Page<SubCategoryEntity> subCategoryPage = subCategoryService.getSubCategoriesByCategory(selectedCategory, page - 1, size);

        model.addAttribute("subCategories", subCategoryPage.getContent());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", subCategoryPage.getTotalPages());
        model.addAttribute("selectedCategory", selectedCategory);
        return "subcategories";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("subcategory", new SubCategoryEntity());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "subcategory-form";
    }

    @PostMapping("/save")
    public String saveSubCategory(@ModelAttribute SubCategoryEntity subCategory,
                                  @RequestParam("categoryId") int categoryId) {
        CategoryEntity category = categoryService.getCategoryById(categoryId);
        if (category != null) {
            subCategory.setCategory(category);
            if (subCategory.getId() == 0) {
                subCategoryService.addSubCategory(subCategory);
            } else {
                subCategoryService.updateSubCategory(subCategory);
            }
        } else {
            throw new IllegalArgumentException("Category with ID " + categoryId + " not found.");
        }
        return "redirect:/java5/asm/crud/categories";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        return Optional.ofNullable(subCategoryService.getSubCategoryById(id))
                .map(subCategory -> {
                    model.addAttribute("subcategory", subCategory);
                    model.addAttribute("categories", categoryService.getAllCategories());
                    return "subcategory-form";
                })
                .orElse("redirect:/java5/asm/crud/categories");
    }

    @GetMapping("/delete/{id}")
    public String deleteSubCategory(@PathVariable int id) {
        Optional.ofNullable(subCategoryService.getSubCategoryById(id))
                .ifPresent(subCategory -> subCategoryService.deleteSubCategory(id));
        return "redirect:/java5/asm/crud/categories";
    }
}