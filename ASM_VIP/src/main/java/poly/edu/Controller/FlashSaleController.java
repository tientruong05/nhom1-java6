package poly.edu.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import poly.edu.DTO.ProductDTO;
import poly.edu.service.FlashSaleService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/java5/asm")
public class FlashSaleController {

    @Autowired
    private FlashSaleService flashSaleService;

    @GetMapping("/flash-sale")
    public String showFlashSalePage(Model model) {
        boolean isFlashSaleActive = flashSaleService.isFlashSaleActive();
        model.addAttribute("isFlashSaleActive", isFlashSaleActive);
        
        if (isFlashSaleActive) {
            // Get the end time and format it consistently
            LocalDateTime endTime = flashSaleService.getFlashSaleEndTime();
            model.addAttribute("flashSaleEndTime", endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            List<ProductDTO> flashSaleProducts = flashSaleService.getFlashSaleProducts();
            model.addAttribute("flashSaleProducts", flashSaleProducts);
            
            // Get discount name for display
            flashSaleService.getCurrentFlashSale().ifPresent(discount -> {
                model.addAttribute("flashSaleName", discount.getDiscountName());
            });
        }
        
        return "flash-sale";
    }
} 