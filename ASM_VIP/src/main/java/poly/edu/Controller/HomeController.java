package poly.edu.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import poly.edu.DTO.ProductDTO;
import poly.edu.entity.ProductEntity;
import poly.edu.service.FlashSaleService;
import poly.edu.service.ProductService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/java5/asm")
public class HomeController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private FlashSaleService flashSaleService;

    @GetMapping({"/", "/home", "/index"})
    public String home(Model model) {
        // Get new products
        List<ProductEntity> newProducts = productService.newProducts();
        model.addAttribute("newProducts", newProducts);
        
        // Get best selling products
        List<ProductEntity> bestProducts = productService.bestProducts();
        model.addAttribute("bestProducts", bestProducts);
        
        // Check and get Flash Sale information
        boolean isFlashSaleActive = flashSaleService.isFlashSaleActive();
        model.addAttribute("isFlashSaleActive", isFlashSaleActive);
        
        if (isFlashSaleActive) {
            // Get end time for countdown
            LocalDateTime flashSaleEndTime = flashSaleService.getFlashSaleEndTime();
            model.addAttribute("flashSaleEndTime", flashSaleEndTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            // Get flash sale products (limited to 4 for homepage)
            List<ProductDTO> flashSaleProducts = flashSaleService.getFlashSaleProductsForHomepage();
            model.addAttribute("flashSaleProducts", flashSaleProducts);
            
            // Get discount name for display
            flashSaleService.getCurrentFlashSale().ifPresent(discount -> {
                model.addAttribute("flashSaleName", discount.getDiscountName());
            });
        }
        
        return "homepage";
    }
} 