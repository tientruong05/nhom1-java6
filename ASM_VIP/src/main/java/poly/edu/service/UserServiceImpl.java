package poly.edu.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import poly.edu.entity.UserEntity;
import poly.edu.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Page<UserEntity> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }

    @Override
    public Optional<UserEntity> getUserById(int id) {
        return userRepository.findById(id);
    }

    @Override
    public List<UserEntity> searchUsersByName(String fullName) {
        return userRepository.findByFullNameContaining(fullName).stream()
                .filter(user -> user != null && user.getFullName() != null)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<UserEntity> getUsersByRole(int role) {
        return userRepository.findByRole(role).stream()
                .filter(user -> user != null)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Optional<UserEntity> getUserByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email));
    }

    @Override
    public Optional<UserEntity> getUserByEmailAndPassword(String email, String password) {
        return Optional.ofNullable(userRepository.findByEmailAndPassword(email, password));
    }

    @Override
    public void createUser(UserEntity user) {
        Optional.ofNullable(user).ifPresent(userRepository::save);
    }

    @Override
    public void updateUser(UserEntity user) {
        Optional.ofNullable(user).ifPresent(userRepository::save);
    }

    @Override
    public void deleteUser(int id) {
        Optional.of(id).filter(i -> i > 0).ifPresent(userRepository::deleteById);
    }

    @Override
    public boolean existsByEmail(String email) {
        return Optional.ofNullable(email)
                .map(userRepository::existsByEmail)
                .orElse(false);
    }

    @Override
    public Optional<UserEntity> findByUsernameAndEmail(String username, String email) {
        return Optional.ofNullable(userRepository.findByUsernameAndEmail(username, email));
    }

    @Override
    public boolean existsByPhone(String phone) {
        return Optional.ofNullable(phone)
                .map(userRepository::existsByPhone)
                .orElse(false);
    }

    @Override
    public boolean existsByUsername(String username) {
        return Optional.ofNullable(username)
                .map(userRepository::existsByUsername)
                .orElse(false);
    }
}