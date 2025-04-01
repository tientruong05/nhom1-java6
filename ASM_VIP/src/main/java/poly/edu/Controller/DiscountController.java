package poly.edu.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import poly.edu.DTO.ProductDTO;
import poly.edu.entity.*;
import poly.edu.repository.*;
import poly.edu.service.DiscountService;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/java5/asm/admin/discounts")
public class DiscountController {

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private DiscountService discountService;
    
    @Autowired
    private DiscountDetailRepository discountDetailRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public String showDiscounts(Model model) {
        List<DiscountEntity> discounts = discountRepository.findAll();
        model.addAttribute("discounts", discounts);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("subCategories", subCategoryRepository.findAll());
        model.addAttribute("products", productRepository.findAll());
        return "discounts";
    }

    @PostMapping("/create")
    public String createDiscount(@ModelAttribute DiscountEntity discount,
                                 @RequestParam(value = "categoryIds", required = false) List<Integer> categoryIds,
                                 @RequestParam(value = "subCategoryIds", required = false) List<Integer> subCategoryIds,
                                 @RequestParam(value = "productIds", required = false) List<Integer> productIds) {
        discountRepository.save(discount);
        saveDiscountDetails(discount.getId(), categoryIds, subCategoryIds, productIds);
        return "redirect:/java5/asm/admin/discounts";
    }

    @GetMapping("/edit/{id}")
    @ResponseBody
    public String showEditForm(@PathVariable int id, Model model) throws JsonProcessingException {
        DiscountEntity discount = discountRepository.findById(id).orElse(null);
        List<CategoryEntity> categories = categoryRepository.findAll();
        List<SubCategoryEntity> subCategories = subCategoryRepository.findAll();
        List<ProductEntity> products = productRepository.findAll();
        List<DiscountDetailEntity> discountDetails = discountDetailRepository.findByDiscount(discount);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Map<String, Object> data = new HashMap<>();
        data.put("discount", discount);
        data.put("categories", categories);
        data.put("subCategories", subCategories);
        List<ProductDTO> productDTOs = new ArrayList<>();
        for(ProductEntity product : products){
            ProductDTO dto = new ProductDTO();
            dto.setId(product.getId());
            dto.setName(product.getName());
            dto.setImage(product.getImage());
            dto.setPrice(product.getPrice());
            dto.setDiscountedPrice(product.getDiscountedPrice());
            dto.setDiscountPercentage(product.getDiscountPercentage());
            dto.setDiscounted(product.isDiscounted());
            productDTOs.add(dto);
        }
        data.put("products", productDTOs);
        data.put("discountDetails", discountDetails);

        return objectMapper.writeValueAsString(data);
    }

    @PostMapping("/edit/{id}")
    @Transactional
    public String updateDiscount(@PathVariable int id, @ModelAttribute DiscountEntity discount,
                                 @RequestParam(value = "categoryIds", required = false) List<Integer> categoryIds,
                                 @RequestParam(value = "subCategoryIds", required = false) List<Integer> subCategoryIds,
                                 @RequestParam(value = "productIds", required = false) List<Integer> productIds) {
        discount.setId(id);
        discountRepository.save(discount);
        discountDetailRepository.deleteByDiscountId(id);
        saveDiscountDetails(id, categoryIds, subCategoryIds, productIds);
        return "redirect:/java5/asm/admin/discounts";
    }

    private void saveDiscountDetails(int discountId, List<Integer> categoryIds, List<Integer> subCategoryIds, List<Integer> productIds) {
        DiscountEntity savedDiscount = discountRepository.findById(discountId).orElse(null);
        if (categoryIds != null) {
            for (Integer categoryId : categoryIds) {
                DiscountDetailEntity detail = new DiscountDetailEntity();
                detail.setDiscount(savedDiscount);
                detail.setCategory(categoryRepository.findById(categoryId).orElse(null));
                detail.setStatus(1);
                discountDetailRepository.save(detail);
            }
        }
        if (subCategoryIds != null) {
            for (Integer subCategoryId : subCategoryIds) {
                DiscountDetailEntity detail = new DiscountDetailEntity();
                detail.setDiscount(savedDiscount);
                detail.setSubCategory(subCategoryRepository.findById(subCategoryId).orElse(null));
                detail.setStatus(1);
                discountDetailRepository.save(detail);
            }
        }
        if (productIds != null) {
            for (Integer productId : productIds) {
                DiscountDetailEntity detail = new DiscountDetailEntity();
                detail.setDiscount(savedDiscount);
                detail.setProduct(productRepository.findById(productId).orElse(null));
                detail.setStatus(1);
                discountDetailRepository.save(detail);
            }
        }
    }
    @Transactional
    @GetMapping("/delete/{id}")
    public String deleteDiscount(@PathVariable int id) {
        discountDetailRepository.deleteByDiscountId(id);
    	discountService.delete(id);
        return "redirect:/java5/asm/admin/discounts";
    }
}
