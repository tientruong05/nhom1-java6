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

import java.io.IOException;
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

        Integer statusInt = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusInt = Integer.parseInt(status);
            } catch (NumberFormatException e) {
                statusInt = null;
            }
        }

        Page<ProductDTO> productPage = productService.getFilteredProducts(search, categoryId, subCategoryId, statusInt, pageable);

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

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new ProductEntity());
        model.addAttribute("subcategories", subCategoryService.getAllSubCategories());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "product-modal-content";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute ProductEntity product,
                              @RequestParam(value = "status", required = false) String status,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              Model model) throws IOException {
        product.setStatus(Optional.ofNullable(status)
                .map(s -> s.equals("1") ? 1 : 0)
                .orElse(0));

        Optional.ofNullable(imageFile)
                .filter(file -> !file.isEmpty())
                .ifPresent(file -> {
                    String originalFileName = file.getOriginalFilename();
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
                        throw new RuntimeException("Không thể lưu ảnh.", e);
                    }
                });

        if (product.getId() == 0 && (product.getImage() == null || product.getImage().isEmpty())) {
            product.setImage("default.png");
        }

        productService.saveProduct(product);
        return "redirect:/java5/asm/crud/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable int id) {
        productService.deleteProduct(id);
        return "redirect:/java5/asm/crud/products";
    }
}