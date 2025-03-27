package poly.edu.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import poly.edu.config.FlashSaleManager;
import poly.edu.dao.SubCategoryDAO;
import poly.edu.entity.ProductEntity;
import poly.edu.entity.SubCategoryEntity;
import poly.edu.service.CategoryService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/java5/asm")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SubCategoryDAO subCategoryDAO;

    @GetMapping("/list-product")
    public String showCategory(@RequestParam(required = false) Integer categoryId,
                               @RequestParam(required = false) Integer subCategoryId,
                               @RequestParam(required = false) String brandName,
                               @RequestParam(required = false) Boolean discount,
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false) String gender,
                               @RequestParam(required = false) String priceRange,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "8") int size,
                               Model model) {
        Pageable pageable = PageRequest.of(page, size);

        return Optional.of(determineProductPage(categoryId, subCategoryId, brandName, discount, search, gender, priceRange, pageable))
                .map(productPage -> {
                    String categoryName = determineCategoryName(categoryId, subCategoryId, brandName, discount);
                    boolean showGenderFilter = determineGenderFilter(categoryId, subCategoryId);

                    model.addAttribute("products", productPage.getContent());
                    model.addAttribute("currentPage", productPage.getNumber());
                    model.addAttribute("totalPages", productPage.getTotalPages());
                    model.addAttribute("pageSize", productPage.getSize());
                    model.addAttribute("totalItems", productPage.getTotalElements());
                    model.addAttribute("categoryName", categoryName);
                    model.addAttribute("categoryId", categoryId);
                    model.addAttribute("subCategoryId", subCategoryId);
                    model.addAttribute("brandName", brandName);
                    model.addAttribute("discount", discount);
                    model.addAttribute("search", search);
                    model.addAttribute("gender", gender);
                    model.addAttribute("priceRange", priceRange);
                    model.addAttribute("showGenderFilter", showGenderFilter);
                    model.addAttribute("isFlashSaleActive", FlashSaleManager.isFlashSaleActive());
                    model.addAttribute("flashSaleEndTime", FlashSaleManager.getEndTime());
                    return "category";
                })
                .orElseGet(() -> "redirect:/java5/asm/");
    }

    private Page<ProductEntity> determineProductPage(Integer categoryId, Integer subCategoryId, String brandName,
                                                     Boolean discount, String search, String gender, String priceRange,
                                                     Pageable pageable) {
        if (Boolean.TRUE.equals(discount)) {
            return categoryService.getDiscountedProducts(search, gender, priceRange, pageable);
        } else if (brandName != null && !brandName.isEmpty()) {
            return categoryService.getProductsByBrandName(brandName, search, gender, priceRange, pageable);
        } else if (subCategoryId != null) {
            return categoryService.getProductsBySubCategory(subCategoryId, search, gender, priceRange, pageable);
        } else if (categoryId != null) {
            return categoryService.getProductsByCategory(categoryId, search, gender, priceRange, pageable);
        } else {
            return categoryService.getAllProducts(search, gender, priceRange, pageable);
        }
    }

    private String determineCategoryName(Integer categoryId, Integer subCategoryId, String brandName, Boolean discount) {
        if (Boolean.TRUE.equals(discount)) {
            return "Hàng giảm giá";
        } else if (brandName != null && !brandName.isEmpty()) {
            return brandName;
        } else if (subCategoryId != null) {
            return categoryService.getSubCategoryName(subCategoryId);
        } else if (categoryId != null) {
            return categoryService.getCategoryName(categoryId);
        } else {
            return "Tất cả sản phẩm";
        }
    }

    private boolean determineGenderFilter(Integer categoryId, Integer subCategoryId) {
        if (subCategoryId != null) {
            return !isGenderSpecificSubCategory(subCategoryId);
        } else if (categoryId != null) {
            return !(categoryId == 1 || categoryId == 2);
        }
        return true;
    }

    private boolean isGenderSpecificSubCategory(Integer subCategoryId) {
        return Optional.ofNullable(subCategoryId)
                .map(subCategoryDAO::findById)
                .map(SubCategoryEntity::getCategory)
                .map(category -> category.getId() == 1 || category.getId() == 2)
                .orElse(false);
    }
}