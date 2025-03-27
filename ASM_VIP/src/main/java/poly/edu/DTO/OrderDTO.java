package poly.edu.DTO;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OrderDTO {
    private int id;
    private String fullName;     // Khớp với fullname trong orders
    private String address;      // Khớp với address trong orders
    private String phone;        // Khớp với phone trong orders
    private double totalAmount;  // Tổng tiền (tính từ order_detail)
    private Date orderDate;      // Ngày đặt hàng (lấy từ order_detail)
    private long orderDateInMillis; // Hỗ trợ giao diện
    private List<OrderDetailDTO> orderDetails; // Danh sách chi tiết đơn hàng

    public OrderDTO(int id, String fullName, String address, String phone, double totalAmount, Date orderDate) {
        this.id = id;
        this.fullName = fullName;
        this.address = address;
        this.phone = phone;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
        this.orderDateInMillis = orderDate != null ? orderDate.getTime() : 0;
    }
}