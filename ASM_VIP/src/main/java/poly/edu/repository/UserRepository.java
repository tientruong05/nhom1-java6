package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import poly.edu.DTO.VipCustomerDTO;
import poly.edu.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    
    UserEntity findByUsername(String username);

    @Query("SELECT new poly.edu.DTO.VipCustomerDTO(u.fullName, u.email, u.phone, SUM(od.qty * od.price)) " +
            "FROM UserEntity u " +
            "JOIN OrderEntity o ON u.id = o.user.id " +
            "JOIN OrderDetailEntity od ON o.id = od.order.id " +
            "GROUP BY u.id, u.fullName, u.email, u.phone " +
            "ORDER BY SUM(od.qty * od.price) DESC")
    List<VipCustomerDTO> findTop10VipCustomers();

    List<UserEntity> findByFullNameContaining(String fullName);

    @Query("SELECT u FROM UserEntity u WHERE u.role = :role")
    List<UserEntity> findByRole(int role);

    @Query("SELECT u FROM UserEntity u WHERE u.email = :email")
    UserEntity findByEmail(String email);

    @Query("SELECT u FROM UserEntity u WHERE u.email = :email AND u.password = :password")
    UserEntity findByEmailAndPassword(String email, String password);

    boolean existsByEmail(String email);

    // Thêm phương thức mới
    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);

    @Query("SELECT u FROM UserEntity u WHERE u.username = :username AND u.email = :email")
    UserEntity findByUsernameAndEmail(String username, String email);
}