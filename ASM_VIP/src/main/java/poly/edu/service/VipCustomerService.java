package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import poly.edu.DTO.VipCustomerDTO;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VipCustomerService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int getLatestYear() {
        String sql = "SELECT MAX(YEAR(order_date)) FROM order_detail";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, Integer.class))
                .orElse(0);
    }

    public Map<Integer, List<VipCustomerDTO>> getTop10VipCustomersByYear(int page, int size) {
        int latestYear = getLatestYear();
        return getTop10VipCustomersByYear(latestYear, page, size);
    }

    public Map<Integer, List<VipCustomerDTO>> getTop10VipCustomersByYear(Integer year, int page, int size) {
        String sql = "SELECT u.full_name, u.email, u.phone, SUM(od.price * od.qty) AS total_amount, YEAR(od.order_date) AS order_year " +
                "FROM users u " +
                "JOIN orders o ON u.id = o.user_id " +
                "JOIN order_detail od ON o.id = od.order_id " +
                "WHERE 1=1 " +
                (year != null ? "AND YEAR(od.order_date) = " + year : "") +
                " GROUP BY u.full_name, u.email, u.phone, YEAR(od.order_date) " +
                "ORDER BY order_year DESC, total_amount DESC";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        Map<Integer, List<VipCustomerDTO>> result = rows.stream()
                .map(row -> {
                    String fullName = (String) row.get("full_name");
                    String email = (String) row.get("email");
                    String phone = (String) row.get("phone");
                    double totalAmount = ((Number) row.get("total_amount")).doubleValue();
                    int orderYear = ((Number) row.get("order_year")).intValue();

                    String formattedTotalAmount = currencyFormat.format(totalAmount);
                    String cleanedTotalAmount = formattedTotalAmount.replaceAll("[^\\d,]", "").replace(",", ".");

                    double parsedTotalAmount = 0.0;
                    try {
                        parsedTotalAmount = Double.parseDouble(cleanedTotalAmount);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing total amount: " + cleanedTotalAmount);
                    }

                    String formattedAmount = currencyFormat.format(parsedTotalAmount);

                    VipCustomerDTO vipCustomer = new VipCustomerDTO(fullName, email, phone, parsedTotalAmount);
                    vipCustomer.setTotalAmount(0.0);
                    vipCustomer.setFormattedTotalAmount(formattedAmount);

                    return new AbstractMap.SimpleEntry<>(orderYear, vipCustomer);
                })
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        // Phân trang
        Map<Integer, List<VipCustomerDTO>> paginatedResult = new HashMap<>();
        result.forEach((orderYear, list) -> {
            int start = page * size;
            int end = Math.min(start + size, list.size());
            if (start < list.size()) {
                paginatedResult.put(orderYear, list.subList(start, end));
            }
        });

        return paginatedResult;
    }
}