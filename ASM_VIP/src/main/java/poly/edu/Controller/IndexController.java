package poly.edu.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import poly.edu.config.FlashSaleManager;
import poly.edu.entity.ProductEntity;
import poly.edu.service.ProductService;

import java.util.List;

@Controller
@RequestMapping("/java5/asm")
public class IndexController {

    @Autowired
    private ProductService productService;

    @RequestMapping("index")
    public String index(Model model) {
        List<ProductEntity> newProducts = productService.newProducts();
        List<ProductEntity> bestProducts = productService.bestProducts();
        List<ProductEntity> saleProducts = productService.getSaleProducts();

        model.addAttribute("newProducts", newProducts);
        model.addAttribute("bestProducts", bestProducts);
        model.addAttribute("saleProducts", saleProducts);
        model.addAttribute("isFlashSaleActive", FlashSaleManager.isFlashSaleActive());
        model.addAttribute("flashSaleEndTime", FlashSaleManager.getEndTime());

        return "homepage";
    }

    @RequestMapping("search")
    public String searchAll(Model model,
                            @RequestParam("q") String search,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "8") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductEntity> products = productService.findSearchAll(search, pageable);

        model.addAttribute("currentPage", products.getNumber());
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("pageSize", products.getSize());
        model.addAttribute("totalItems", products.getTotalElements());
        model.addAttribute("products", products.getContent());
        model.addAttribute("search", search);
        model.addAttribute("isFlashSaleActive", FlashSaleManager.isFlashSaleActive());
        model.addAttribute("flashSaleEndTime", FlashSaleManager.getEndTime());

        return "search";
    }
}