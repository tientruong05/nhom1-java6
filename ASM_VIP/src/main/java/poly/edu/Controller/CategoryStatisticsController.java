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
import poly.edu.DTO.CategoryRevenueDTO;
import poly.edu.service.ProductService;

@Controller
@RequestMapping("/java5/asm/statistics")
public class CategoryStatisticsController {

    @Autowired
    private ProductService productService;

    @GetMapping("/business")
    public String revenueStatistics(Model model,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CategoryRevenueDTO> revenueStats = productService.getCategoryRevenue(pageable);

        model.addAttribute("revenueStats", revenueStats);
        model.addAttribute("currentPage", revenueStats.getNumber());
        model.addAttribute("totalPages", revenueStats.getTotalPages());
        model.addAttribute("pageSize", revenueStats.getSize());
        return "business-statistics";
    }
}