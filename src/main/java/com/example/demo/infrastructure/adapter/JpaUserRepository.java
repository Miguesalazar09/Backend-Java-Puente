package com.example.demo.infrastructure.adapter;

import com.example.demo.domain.model.User;
import com.example.demo.domain.port.UserRepository;
import com.example.demo.infrastructure.entity.UserEntity;
import com.example.demo.infrastructure.repository.SpringDataUserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository springRepo;

    public JpaUserRepository(SpringDataUserRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public User save(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setName(user.getName());
        entity.setEmail(user.getEmail());
        entity.setPass(user.getPass());
        entity.setRole(user.getRole());
        return toModel(springRepo.save(entity));
    }

    @Override
    public List<User> findAll() {
        return springRepo.findAll().stream().map(this::toModel).collect(Collectors.toList());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springRepo.findByEmail(email).map(this::toModel);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springRepo.findById(id).map(this::toModel);
    }

    @Override
    public void deleteById(UUID id) {
        springRepo.deleteById(id);
    }

    private User toModel(UserEntity entity) {
        return new User(entity.getId(), entity.getName(), entity.getEmail(), entity.getPass(), entity.getRole());
    }
}
