package poly.edu.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "discounts")
@Data
public class DiscountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "discount_name", nullable = false)
    private String discountName;

    @Column(name = "discount_value", nullable = false)
    private float discountValue;

    @Column(name = "start_date", nullable = false)
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    @Column(name = "end_date", nullable = false)
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    @Column(name = "status", nullable = false)
    private int status;

    @OneToMany(mappedBy = "discount", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DiscountDetailEntity> discountDetails;

    // Kiểm tra trạng thái hoạt động của discount
    @Transient
    public boolean isActive() {
        Date now = new Date();
        return status == 1 && startDate.before(now) && endDate.after(now);
    }
}