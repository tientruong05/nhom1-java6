package poly.edu.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.DTO.ProductDTO;
import poly.edu.entity.ProductEntity;
import poly.edu.service.CategoryService;
import poly.edu.service.ProductService;
import poly.edu.service.SubCategoryService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Controller
@RequestMapping("/java5/asm/crud/products")
public class ProductCRUDController {

    @Autowired
    private ProductService productService;

    @Autowired
    private SubCategoryService subCategoryService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String getAllProducts(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String search,
                                 @RequestParam(required = false) Integer categoryId,
                                 @RequestParam(required = false) Integer subCategoryId,
                                 @RequestParam(required = false) String status) {
        Pageable pageable = PageRequest.of(page, size);

        // Sửa lỗi lambda: Xử lý status thành Integer một cách rõ ràng
        Integer statusInt = status != null && !status.isEmpty() ? Integer.parseInt(status) : null;

        Page<ProductDTO> productPage = productService.getFilteredProducts(
                search, categoryId, subCategoryId, statusInt, pageable
        );

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("search", search);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("subCategoryId", subCategoryId);
        model.addAttribute("status", status);
        model.addAttribute("product", new ProductEntity());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("subcategories", subCategoryService.getAllSubCategories());

        return "products";
    }

    @PostMapping("/save")
    public String saveProduct(@RequestParam("name") String name,
                              @RequestParam("category.id") Integer categoryId,
                              @RequestParam("subCategory.id") Integer subCategoryId,
                              @RequestParam("price") String priceStr,
                              @RequestParam("qty") Integer qty,
                              @RequestParam(value = "description", required = false) String description,
                              @RequestParam(value = "status", required = false) String status,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              @RequestParam(value = "id", defaultValue = "0") Integer id,
                              @RequestParam(value = "image", required = false) String existingImage) {
        String error = productService.saveProductFromForm(name, categoryId, subCategoryId, priceStr, qty, description, status, imageFile, id, existingImage);
        if (error != null) {
            return "redirect:/java5/asm/crud/products?error=" + URLEncoder.encode(error, StandardCharsets.UTF_8);
        }
        return "redirect:/java5/asm/crud/products?success=" + URLEncoder.encode("Lưu sản phẩm thành công", StandardCharsets.UTF_8);
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable int id) {
        String error = productService.deleteProductById(id);
        if (error != null) {
            return "redirect:/java5/asm/crud/products?error=" + URLEncoder.encode(error, StandardCharsets.UTF_8);
        }
        return "redirect:/java5/asm/crud/products?success=" + URLEncoder.encode("Xóa sản phẩm thành công", StandardCharsets.UTF_8);
    }
}