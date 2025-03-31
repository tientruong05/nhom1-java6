package poly.edu.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import poly.edu.dao.CategoryDAO;
import poly.edu.dao.SubCategoryDAO;
import poly.edu.entity.CategoryEntity;
import poly.edu.entity.SubCategoryEntity;
import poly.edu.service.CategoryService;

import java.util.List;

@ControllerAdvice
public class MenuController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryDAO categoryDAO;

    @Autowired
    private SubCategoryDAO subCategoryDAO;

    @ModelAttribute("categories")
    public List<CategoryEntity> getCategories() {
        return categoryService.getCategoriesByStatus(1); // 1 = active
    }

    @ModelAttribute("watchBrands")
    public List<CategoryEntity> getWatchBrands() {
        return categoryDAO.findByStatus(1);
    }
}