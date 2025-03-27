package poly.edu.DTO;

import lombok.Data;

@Data
public class CategoryRevenueDTO {
    private String categoryName;
    private String subName;
    private double totalRevenue;
    private int totalQty;
    private double maxPrice;
    private double minPrice;
    private double avgPrice;

    public CategoryRevenueDTO() {
        // Constructor mặc định
    }

    public CategoryRevenueDTO(String categoryName, String subName, double totalRevenue, int totalQty, double maxPrice, double minPrice, double avgPrice) {
        this.categoryName = categoryName;
        this.subName = subName;
        this.totalRevenue = totalRevenue;
        this.totalQty = totalQty;
        this.maxPrice = maxPrice;
        this.minPrice = minPrice;
        this.avgPrice = avgPrice;
    }
}