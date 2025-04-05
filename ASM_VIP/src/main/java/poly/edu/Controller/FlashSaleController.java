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
    public String showFlashSalePage(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        
        boolean isFlashSaleActive = flashSaleService.isFlashSaleActive();
        model.addAttribute("isFlashSaleActive", isFlashSaleActive);
        
        if (isFlashSaleActive) {
            // Định dạng thời gian kết thúc để JavaScript có thể xử lý
            LocalDateTime endTime = flashSaleService.getFlashSaleEndTime();
            model.addAttribute("flashSaleEndTime", endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            // Lấy danh sách sản phẩm Flash Sale với phân trang
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductDTO> flashSaleProductsPage = flashSaleService.getFlashSaleProductsPaged(pageable);
            
            model.addAttribute("flashSaleProducts", flashSaleProductsPage.getContent());
            model.addAttribute("currentPage", flashSaleProductsPage.getNumber());
            model.addAttribute("totalPages", flashSaleProductsPage.getTotalPages());
            model.addAttribute("totalItems", flashSaleProductsPage.getTotalElements());
            model.addAttribute("pageSize", size);
        }
        
        return "flash-sale";
    }
} 