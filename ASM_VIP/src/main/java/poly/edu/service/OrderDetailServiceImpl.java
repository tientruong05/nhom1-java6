package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import poly.edu.dao.OrderDetailDAO;
import poly.edu.entity.OrderDetailEntity;
import poly.edu.entity.OrderEntity;
import poly.edu.repository.OrderDetailRepository;

import java.util.List;
import java.util.Optional;

@Service
public class OrderDetailServiceImpl implements OrderDetailService {

    private final OrderDetailDAO orderDetailDAO;
    private final OrderDetailRepository orderDetailRepository;

    @Autowired
    public OrderDetailServiceImpl(OrderDetailDAO orderDetailDAO, OrderDetailRepository orderDetailRepository) {
        this.orderDetailDAO = orderDetailDAO;
        this.orderDetailRepository = orderDetailRepository;
    }

    @Override
    public Optional<OrderDetailEntity> getOrderDetailById(int id) {
        return orderDetailDAO.findById(id);
    }

    @Override
    public List<OrderDetailEntity> getAllOrderDetails() {
        return orderDetailDAO.findAll();
    }

    @Override
    public void addOrderDetail(OrderDetailEntity orderDetail) {
        Optional.ofNullable(orderDetail).ifPresent(orderDetailDAO::save);
    }

    @Override
    public void updateOrderDetail(OrderDetailEntity orderDetail) {
        Optional.ofNullable(orderDetail).ifPresent(orderDetailDAO::update);
    }

    @Override
    public void removeOrderDetail(int id) {
        orderDetailDAO.findById(id).ifPresent(order -> orderDetailDAO.delete(id));
    }

    @Override
    public List<OrderDetailEntity> findByOrder(OrderEntity order) {
        return orderDetailRepository.findByOrder(order);
    }

    @Override
    public OrderDetailEntity save(OrderDetailEntity orderDetail) {
        return Optional.ofNullable(orderDetail)
                .map(orderDetailRepository::save)
                .orElse(null);
    }

    @Override
    public OrderDetailEntity findById(int id) {
        return orderDetailRepository.findById(id).orElse(null);
    }

    @Override
    public List<OrderDetailEntity> findAll() {
        return orderDetailRepository.findAll();
    }

    @Override
    public void deleteById(int id) {
        Optional.of(id).filter(i -> i > 0).ifPresent(orderDetailRepository::deleteById);
    }

    @Override
    public List<OrderDetailEntity> getOrderDetailsByUserId(int userId) {
        return orderDetailRepository.findByOrderUserId(userId);
    }

    @Override
    public Page<OrderDetailEntity> findByOrders(List<OrderEntity> orders, Pageable pageable) {
        return orderDetailRepository.findByOrderIn(orders, pageable);
    }
}