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
import poly.edu.entity.SubCategoryEntity;
import poly.edu.service.CategoryService;
import poly.edu.service.ProductService;
import poly.edu.service.SubCategoryService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

        Integer statusInt = Optional.ofNullable(status)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try {
                        return Integer.parseInt(s);
                    } catch (NumberFormatException ignored) {
                        return null;
                    }
                })
                .orElse(null);

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
                              @RequestParam("subCategory.id") Integer subCategoryId,
                              @RequestParam("price") String priceStr,
                              @RequestParam("qty") Integer qty,
                              @RequestParam(value = "description", required = false) String description,
                              @RequestParam(value = "status", required = false) String status,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              @RequestParam(value = "id", defaultValue = "0") Integer id,
                              @RequestParam(value = "image", required = false) String existingImage) {
        try {
            ProductEntity product;
            if (id == 0) {
                product = new ProductEntity();
            } else {
                product = productService.getProductById(id)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
            }

            product.setId(id);
            product.setName(name);
            product.setQty(qty);
            product.setDescription(description);

            // Bẫy lỗi giá
            float price;
            try {
                price = Float.parseFloat(priceStr);
                if (price < 0) {
                    return "redirect:/java5/asm/crud/products?error=" + URLEncoder.encode("Giá phải lớn hơn hoặc bằng 0", StandardCharsets.UTF_8);
                }
            } catch (NumberFormatException e) {
                return "redirect:/java5/asm/crud/products?error=" + URLEncoder.encode("Giá không hợp lệ", StandardCharsets.UTF_8);
            }
            product.setPrice(price);

            // Xử lý SubCategory
            Optional<SubCategoryEntity> subCategory = Optional.ofNullable(subCategoryService.getSubCategoryById(subCategoryId));
            if (subCategory.isPresent()) {
                product.setSubCategory(subCategory.get());
            } else {
                return "redirect:/java5/asm/crud/products?error=" + URLEncoder.encode("Hãng không hợp lệ", StandardCharsets.UTF_8);
            }

            // Xử lý status
            product.setStatus("1".equals(status) ? 1 : 0);

            // Xử lý upload ảnh
            try {
                Optional.ofNullable(imageFile)
                        .filter(file -> !file.isEmpty())
                        .ifPresent(file -> {
                            String originalFileName = file.getOriginalFilename();
                            if (originalFileName == null || !originalFileName.contains(".")) {
                                throw new RuntimeException("Tên file không hợp lệ.");
                            }
                            Path targetPath = Paths.get("C:/watch-store/photos", originalFileName);
                            try {
                                Files.createDirectories(targetPath.getParent());
                                if (Files.exists(targetPath)) {
                                    try {
                                        Files.deleteIfExists(targetPath);
                                    } catch (IOException e) {
                                        String newFileName = System.currentTimeMillis() + "_" + originalFileName;
                                        targetPath = Paths.get("C:/watch-store/photos", newFileName);
                                    }
                                }
                                Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                                product.setImage(targetPath.getFileName().toString());
                            } catch (IOException e) {
                                throw new RuntimeException("Không thể lưu ảnh: " + e.getMessage(), e);
                            }
                        });

                // Nếu không có ảnh mới
                if (product.getImage() == null) {
                    if (id == 0) { // Sản phẩm mới
                        product.setImage("default.png");
                    } else { // Sản phẩm cũ
                        if (existingImage == null || existingImage.isEmpty()) {
                            return "redirect:/java5/asm/crud/products?error=" + URLEncoder.encode("Ảnh hiện tại không hợp lệ", StandardCharsets.UTF_8);
                        }
                        product.setImage(existingImage); // Giữ ảnh hiện tại
                    }
                }
            } catch (RuntimeException e) {
                return "redirect:/java5/asm/crud/products?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            }

            // Lưu sản phẩm
            productService.saveProduct(product);
            return "redirect:/java5/asm/crud/products?success=" + URLEncoder.encode("Lưu sản phẩm thành công", StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "redirect:/java5/asm/crud/products?error=" + URLEncoder.encode("Lỗi khi lưu sản phẩm: " + e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable int id) {
        try {
            productService.deleteProduct(id);
            return "redirect:/java5/asm/crud/products?success=" + URLEncoder.encode("Xóa sản phẩm thành công", StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "redirect:/java5/asm/crud/products?error=" + URLEncoder.encode("Lỗi khi xóa sản phẩm: " + e.getMessage(), StandardCharsets.UTF_8);
        }
    }
}