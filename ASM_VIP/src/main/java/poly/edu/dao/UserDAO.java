package poly.edu.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import poly.edu.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDAO {
    Optional<UserEntity> findById(int id);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByEmailAndPassword(String email, String password);
    boolean existsByEmail(String email);
    long countUsers();
    List<UserEntity> findAllUsers();
    List<UserEntity> findByFullNameContaining(String fullName);
    List<UserEntity> findByRole(int role);
    void deleteByEmail(String email);
    void save(UserEntity user);
    void update(UserEntity user);
    void deleteById(int id);
    Optional<UserEntity> findByUsernameAndEmail(String username, String email);
    void saveAndFlush(UserEntity newUser);
}

@Transactional
@Repository
class UserDAOImpl implements UserDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<UserEntity> findById(int id) {
        return Optional.ofNullable(entityManager.find(UserEntity.class, id));
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return entityManager.createQuery("SELECT u FROM UserEntity u WHERE u.email = :email", UserEntity.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<UserEntity> findByEmailAndPassword(String email, String password) {
        return entityManager.createQuery("SELECT u FROM UserEntity u WHERE u.email = :email AND u.password = :password", UserEntity.class)
                .setParameter("email", email)
                .setParameter("password", password)
                .getResultStream()
                .findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        return entityManager.createQuery("SELECT COUNT(u) FROM UserEntity u WHERE u.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult() > 0;
    }

    @Override
    public long countUsers() {
        return entityManager.createQuery("SELECT COUNT(u) FROM UserEntity u", Long.class)
                .getSingleResult();
    }

    @Override
    public List<UserEntity> findAllUsers() {
        return entityManager.createQuery("SELECT u FROM UserEntity u", UserEntity.class)
                .getResultStream()
                .toList();
    }

    @Override
    public List<UserEntity> findByFullNameContaining(String fullName) {
        return entityManager.createQuery("SELECT u FROM UserEntity u WHERE u.fullName LIKE :fullName", UserEntity.class)
                .setParameter("fullName", "%" + fullName + "%")
                .getResultStream()
                .toList();
    }

    @Override
    public List<UserEntity> findByRole(int role) {
        return entityManager.createQuery("SELECT u FROM UserEntity u WHERE u.role = :role", UserEntity.class)
                .setParameter("role", role)
                .getResultStream()
                .toList();
    }

    @Override
    public void deleteByEmail(String email) {
        entityManager.createQuery("DELETE FROM UserEntity u WHERE u.email = :email")
                .setParameter("email", email)
                .executeUpdate();
    }

    @Override
    public void save(UserEntity user) {
        entityManager.persist(user);
    }

    @Override
    public void update(UserEntity user) {
        entityManager.merge(user);
    }

    @Override
    public void deleteById(int id) {
        Optional.ofNullable(entityManager.find(UserEntity.class, id))
                .ifPresent(entityManager::remove);
    }

    @Override
    public Optional<UserEntity> findByUsernameAndEmail(String username, String email) {
        return entityManager.createQuery("SELECT u FROM UserEntity u WHERE u.username = :username AND u.email = :email", UserEntity.class)
                .setParameter("username", username)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    @Override
    public void saveAndFlush(UserEntity newUser) {
        entityManager.persist(newUser);
        entityManager.flush();
    }
}