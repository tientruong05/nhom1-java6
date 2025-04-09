package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import poly.edu.DTO.VipCustomerDTO;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VipCustomerService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int getLatestYear() {
        String sql = "SELECT MAX(YEAR(od.order_date)) FROM order_detail od";
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
        NumberFormat currencyFormat = new DecimalFormat("#,##0.## VND"); // Custom format for VND

        Map<Integer, List<VipCustomerDTO>> result = rows.stream()
                .map(row -> {
                    String fullName = (String) row.get("full_name");
                    String email = (String) row.get("email");
                    String phone = (String) row.get("phone");
                    double totalAmount = ((Number) row.get("total_amount")).doubleValue();
                    int orderYear = ((Number) row.get("order_year")).intValue();

                    String formattedAmount = currencyFormat.format(totalAmount);

                    VipCustomerDTO vipCustomer = new VipCustomerDTO(fullName, email, phone, totalAmount);
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

    public Map<Integer, List<VipCustomerDTO>> getTop10VipCustomersByQuarter(Integer year, Integer quarter, int page, int size) {
        if (year == null || quarter == null || quarter < 1 || quarter > 4) {
            return new HashMap<>();
        }

        String startDate = "";
        String endDate = "";

        switch (quarter) {
            case 1:
                startDate = year + "-01-01";
                endDate = year + "-03-31";
                break;
            case 2:
                startDate = year + "-04-01";
                endDate = year + "-06-30";
                break;
            case 3:
                startDate = year + "-07-01";
                endDate = year + "-09-30";
                break;
            case 4:
                startDate = year + "-10-01";
                endDate = year + "-12-31";
                break;
        }

        String sql = "SELECT u.full_name, u.email, u.phone, SUM(od.price * od.qty) AS total_amount " +
                "FROM users u " +
                "JOIN orders o ON u.id = o.user_id " +
                "JOIN order_detail od ON o.id = od.order_id " +
                "WHERE od.order_date >= ? AND od.order_date <= ? " +
                "GROUP BY u.full_name, u.email, u.phone " +
                "ORDER BY total_amount DESC";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, startDate, endDate);
        NumberFormat currencyFormat = new DecimalFormat("#,##0.## VND"); // Custom format for VND

        List<VipCustomerDTO> allCustomers = rows.stream()
                .map(row -> {
                    String fullName = (String) row.get("full_name");
                    String email = (String) row.get("email");
                    String phone = (String) row.get("phone");
                    double totalAmount = ((Number) row.get("total_amount")).doubleValue();

                    String formattedAmount = currencyFormat.format(totalAmount);

                    VipCustomerDTO vipCustomer = new VipCustomerDTO(fullName, email, phone, totalAmount);
                    vipCustomer.setFormattedTotalAmount(formattedAmount);
                    return vipCustomer;
                })
                .collect(Collectors.toList());

        Map<Integer, List<VipCustomerDTO>> paginatedResult = new HashMap<>();
        int start = page * size;
        int end = Math.min(start + size, allCustomers.size());
        if (start < allCustomers.size()) {
            paginatedResult.put(quarter, allCustomers.subList(start, end));
        }

        return paginatedResult;
    }
}