package poly.edu.DTO;

import lombok.Data;

@Data
public class VipCustomerDTO {
    private String fullName;
    private String email;
    private String phone;
    private double totalAmount;
    private String formattedTotalAmount;

    public VipCustomerDTO() {
        // Constructor mặc định
    }

    public VipCustomerDTO(String fullName, String email, String phone, double totalAmount) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.totalAmount = totalAmount;
    }

    public VipCustomerDTO(String fullName, String email, String phone, double totalAmount, String formattedTotalAmount) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.totalAmount = totalAmount;
        this.formattedTotalAmount = formattedTotalAmount;
    }
}