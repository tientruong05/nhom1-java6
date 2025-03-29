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

import java.io.UnsupportedEncodingException;
import java.util.Optional;

@Controller
@RequestMapping("/java5/asm/crud/subcategories")
public class SubCategoryController {

    @Autowired
    private SubCategoryService subCategoryService;

    @Autowired
    private CategoryService categoryService;

    /**
     * Hiển thị danh sách thương hiệu theo danh mục được chọn, có phân trang
     */
    @GetMapping
    public String getSubCategoriesByCategory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String success,
            Model model) {
        // Xác định danh mục được chọn, nếu không có thì lấy danh mục đầu tiên
        String selectedCategory = Optional.ofNullable(categoryName)
                .filter(name -> !name.trim().isEmpty())
                .orElseGet(() -> categoryService.getAllCategories().stream()
                        .findFirst()
                        .map(CategoryEntity::getName)
                        .orElse(""));

        // Lấy danh sách thương hiệu theo danh mục với phân trang
        Page<SubCategoryEntity> subCategoryPage = subCategoryService.getSubCategoriesByCategory(selectedCategory, page - 1, size);

        // Truyền dữ liệu vào model
        model.addAttribute("subCategories", subCategoryPage.getContent());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", subCategoryPage.getTotalPages());
        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("error", error);
        model.addAttribute("success", success);

        return "subcategories";
    }

    /**
     * Xử lý lưu thương hiệu (thêm mới hoặc cập nhật)
     * @throws UnsupportedEncodingException 
     */
    @PostMapping("/save")
    public String saveSubCategory(
            @RequestParam("id") Optional<Integer> id,
            @RequestParam("categoryName") String categoryName,
            @RequestParam("subCategoriesName") String subCategoriesName,
            @RequestParam("status") int status,
            Model model) throws UnsupportedEncodingException {
        try {
            // Kiểm tra xem thương hiệu (categoryName) đã tồn tại chưa
            CategoryEntity category = categoryService.getAllCategories().stream()
                    .filter(cat -> cat.getName().equalsIgnoreCase(categoryName.trim()))
                    .findFirst()
                    .orElse(null);

            // Nếu thương hiệu chưa tồn tại, tạo mới
            if (category == null) {
                category = new CategoryEntity();
                category.setName(categoryName.trim());
                category.setStatus(1);
                categoryService.createCategory(category);
            }

            // Tạo hoặc cập nhật SubCategoryEntity
            SubCategoryEntity subCategory;
            if (id.isPresent() && id.get() > 0) {
                subCategory = subCategoryService.getSubCategoryById(id.get());
                if (subCategory == null) {
                    return "redirect:/java5/asm/crud/subcategories?error=" + java.net.URLEncoder.encode("Không tìm thấy thương hiệu với ID: " + id.get(), "UTF-8");
                }
            } else {
                subCategory = new SubCategoryEntity();
            }

            subCategory.setSubCategoriesName(subCategoriesName);
            subCategory.setCategory(category);
            subCategory.setStatus(status);

            if (subCategory.getId() == 0) {
                subCategoryService.addSubCategory(subCategory);
                return "redirect:/java5/asm/crud/subcategories?success=" + java.net.URLEncoder.encode("Thêm thương hiệu thành công", "UTF-8");
            } else {
                subCategoryService.updateSubCategory(subCategory);
                return "redirect:/java5/asm/crud/subcategories?success=" + java.net.URLEncoder.encode("Cập nhật thương hiệu thành công", "UTF-8");
            }
        } catch (Exception e) {
            return "redirect:/java5/asm/crud/subcategories?error=" + java.net.URLEncoder.encode("Lỗi khi lưu thương hiệu: " + e.getMessage(), "UTF-8");
        }
    }

    /**
     * Xóa thương hiệu theo ID và hiển thị thông báo trong modal
     */
    @GetMapping("/delete/{id}")
    public String deleteSubCategory(
            @PathVariable int id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String categoryName,
            Model model) {
        try {
            Optional<SubCategoryEntity> subCategoryOpt = Optional.ofNullable(subCategoryService.getSubCategoryById(id));
            if (subCategoryOpt.isPresent()) {
                subCategoryService.deleteSubCategory(id);
                model.addAttribute("modalMessage", "Xóa thương hiệu thành công");
            } else {
                model.addAttribute("modalMessage", "Không tìm thấy thương hiệu với ID: " + id);
            }
        } catch (Exception e) {
            model.addAttribute("modalMessage", "Lỗi khi xóa thương hiệu: " + e.getMessage());
        }

        // Tải lại danh sách thương hiệu
        String selectedCategory = Optional.ofNullable(categoryName)
                .filter(name -> !name.trim().isEmpty())
                .orElseGet(() -> categoryService.getAllCategories().stream()
                        .findFirst()
                        .map(CategoryEntity::getName)
                        .orElse(""));

        Page<SubCategoryEntity> subCategoryPage = subCategoryService.getSubCategoriesByCategory(selectedCategory, page - 1, 10);
        model.addAttribute("subCategories", subCategoryPage.getContent());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", subCategoryPage.getTotalPages());
        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("showModal", true); // Dùng để hiển thị modal

        return "subcategories";
    }
}