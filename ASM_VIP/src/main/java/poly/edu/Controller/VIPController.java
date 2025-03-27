package poly.edu.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import poly.edu.DTO.VipCustomerDTO;
import poly.edu.service.VipCustomerService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/java5/asm/statistics/customers")
public class VIPController {

    @Autowired
    private VipCustomerService vipCustomerService;

    @GetMapping
    public String getVipCustomers(Model model,
                                  @RequestParam(name = "year", required = false) Integer year,
                                  @RequestParam(name = "page", defaultValue = "0") int page,
                                  @RequestParam(name = "size", defaultValue = "10") int size) {
        Map<Integer, List<VipCustomerDTO>> vipCustomersByYear = Optional.ofNullable(year)
                .map(y -> vipCustomerService.getTop10VipCustomersByYear(y, page, size))
                .orElseGet(() -> vipCustomerService.getTop10VipCustomersByYear(page, size));

        Integer selectedYear = Optional.of(vipCustomersByYear)
                .filter(map -> !map.isEmpty())
                .map(Map::keySet)
                .map(keys -> keys.iterator().next())
                .orElse(year);

        model.addAttribute("vipCustomersByYear", vipCustomersByYear);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("selectedYear", selectedYear);
        return "VIP";
    }
}