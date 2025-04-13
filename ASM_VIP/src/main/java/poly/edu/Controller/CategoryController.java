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
import poly.edu.DTO.ProductDTO;
import poly.edu.service.CategoryService;

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

        // Khởi tạo các biến để xác định loại trang và cách lọc
        Integer effectiveCategoryId = categoryId; // Hãng đồng hồ
        String effectiveGender = gender;          // Giới tính (nam/nữ)
        String pageTitleGender = null;
        boolean isGenderPage = false;
        
        // Xử lý trường hợp đặc biệt: khi gender được chỉ định trực tiếp
        if (gender != null) {
            isGenderPage = true;
            pageTitleGender = gender.equals("male") ? "Đồng hồ nam" : "Đồng hồ nữ";
        }

        // Lấy sản phẩm dựa trên các bộ lọc hiện tại
        Page<ProductDTO> productPage = determineProductPage(effectiveCategoryId, subCategoryId, brandName, discount, search, effectiveGender, priceRange, pageable);

        // Xác định tên danh mục hiển thị
        String categoryName;
        if (pageTitleGender != null) {
            categoryName = pageTitleGender;
        } else if (Boolean.TRUE.equals(discount)) {
            categoryName = "Hàng giảm giá";
        } else if (brandName != null && !brandName.isEmpty()) {
            categoryName = brandName;
        } else if (subCategoryId != null) {
            categoryName = categoryService.getSubCategoryName(subCategoryId);
        } else if (categoryId != null) {
            categoryName = categoryService.getCategoryName(categoryId);
        } else {
            categoryName = "Tất cả sản phẩm";
        }
        
        // Xác định xem có hiện bộ lọc Gender hay không 
        boolean showGenderFilter = !isGenderPage;

        // Đưa dữ liệu vào model
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("pageSize", productPage.getSize());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("categoryId", effectiveCategoryId);
        model.addAttribute("isGenderPage", isGenderPage);
        model.addAttribute("subCategoryId", subCategoryId);
        model.addAttribute("brandName", brandName);
        model.addAttribute("discount", discount);
        model.addAttribute("search", search);
        model.addAttribute("gender", effectiveGender);
        model.addAttribute("priceRange", priceRange);
        model.addAttribute("showGenderFilter", showGenderFilter);
        model.addAttribute("isFlashSaleActive", FlashSaleManager.isFlashSaleActive());
        model.addAttribute("flashSaleEndTime", FlashSaleManager.getEndTime());
        return "category";
    }

    private Page<ProductDTO> determineProductPage(Integer categoryId, Integer subCategoryId, String brandName,
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
}