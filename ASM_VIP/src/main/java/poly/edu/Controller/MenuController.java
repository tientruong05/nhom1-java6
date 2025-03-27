package poly.edu.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import poly.edu.dao.SubCategoryDAO;
import poly.edu.entity.CategoryEntity;
import poly.edu.entity.SubCategoryEntity;
import poly.edu.service.CategoryService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

@ControllerAdvice
public class MenuController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SubCategoryDAO subCategoryDAO;

    @ModelAttribute("categories")
    public List<CategoryEntity> getCategories() {
        return categoryService.getCategoriesByStatus(1); // 1 = active
    }

    @ModelAttribute("watchBrands")
    public List<SubCategoryEntity> getWatchBrands() {
        return subCategoryDAO.findByStatus(1).stream()
                .filter(distinctByKey(SubCategoryEntity::getSubCategoriesName))
                .limit(10)
                .toList(); // Java 16+, nếu dùng Java 8 thì thay bằng collect(Collectors.toList())
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}