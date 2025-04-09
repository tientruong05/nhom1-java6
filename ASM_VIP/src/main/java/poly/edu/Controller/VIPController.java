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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/java5/asm/statistics/customers")
public class VIPController {

    @Autowired
    private VipCustomerService vipCustomerService;

    @GetMapping
    public String getVipCustomers(Model model,
                                 @RequestParam(name = "year", required = false) Integer year,
                                 @RequestParam(name = "quarter", required = false) Integer quarter,
                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                 @RequestParam(name = "size", defaultValue = "10") int size) {
        Map<Integer, List<VipCustomerDTO>> vipCustomersByPeriod;
        Integer selectedYear = year;
        Integer selectedQuarter = quarter;

        if (year != null && quarter != null) {
            vipCustomersByPeriod = vipCustomerService.getTop10VipCustomersByQuarter(year, quarter, page, size);
        } else {
            vipCustomersByPeriod = vipCustomerService.getTop10VipCustomersByYear(year, page, size);
            if (vipCustomersByPeriod.isEmpty() && year == null) {
                selectedYear = vipCustomerService.getLatestYear();
                vipCustomersByPeriod = vipCustomerService.getTop10VipCustomersByYear(selectedYear, page, size);
            } else if (!vipCustomersByPeriod.isEmpty() && year == null) {
                selectedYear = vipCustomersByPeriod.keySet().iterator().next();
            }
        }

        model.addAttribute("vipCustomersByYear", vipCustomersByPeriod);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("selectedQuarter", selectedQuarter);

        return "VIP";
    }
}