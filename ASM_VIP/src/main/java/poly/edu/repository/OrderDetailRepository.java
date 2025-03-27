package poly.edu.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import poly.edu.entity.OrderDetailEntity;
import poly.edu.entity.OrderEntity;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetailEntity, Integer> {
	List<OrderDetailEntity> findByOrder(OrderEntity order);
	List<OrderDetailEntity> findByOrderUserId(int userId);
	Page<OrderDetailEntity> findByOrderIn(List<OrderEntity> orders, Pageable pageable);
}
