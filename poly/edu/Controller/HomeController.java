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
        // Lấy sản phẩm mới
        List<ProductEntity> newProducts = productService.newProducts();
        model.addAttribute("newProducts", newProducts);
        
        // Lấy sản phẩm bán chạy
        List<ProductEntity> bestProducts = productService.bestProducts();
        model.addAttribute("bestProducts", bestProducts);
        
        // Kiểm tra và lấy thông tin Flash Sale
        boolean isFlashSaleActive = flashSaleService.isFlashSaleActive();
        model.addAttribute("isFlashSaleActive", isFlashSaleActive);
        
        if (isFlashSaleActive) {
            // Lấy thời gian kết thúc để hiển thị đếm ngược
            LocalDateTime flashSaleEndTime = flashSaleService.getFlashSaleEndTime();
            model.addAttribute("flashSaleEndTime", flashSaleEndTime);
            
            // Lấy sản phẩm flash sale
            List<ProductDTO> saleProducts = flashSaleService.getFlashSaleProducts();
            model.addAttribute("saleProducts", saleProducts);
        }
        
        return "homepage";
    }
    
    // Các mapping khác...
} 