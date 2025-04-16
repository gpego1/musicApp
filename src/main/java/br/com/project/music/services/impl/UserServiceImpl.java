package br.com.project.music.services.impl;

import br.com.project.music.business.dtos.UserDTO;
import br.com.project.music.business.entities.User;
import br.com.project.music.business.repositories.UserRepository;
import br.com.project.music.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.hibernate.StaleObjectStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setSenha(passwordEncoder.encode(userDTO.getSenha()));
        user.setDataCriacao(Timestamp.from(Instant.now()));
        user.setUserType(userDTO.getUserType() != null ? userDTO.getUserType() : "USER");
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User getUserByEmailAndSenha(String email, String senha) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(senha, user.getSenha())) {
                return user;
            }
        }
        throw new EntityNotFoundException("Invalid email or password");
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao foi encotrado " + email));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getSenha())
                .authorities(user.getUserType() != null ? user.getUserType() : "USER")
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        try {
            if (userDTO.getName() != null && !userDTO.getName().equals(existingUser.getName())) {
                existingUser.setName(userDTO.getName());
            }
            if (userDTO.getEmail() != null && !userDTO.getEmail().equals(existingUser.getEmail())) {
                existingUser.setEmail(userDTO.getEmail());
            }
            if (userDTO.getSenha() != null && !passwordEncoder.matches(userDTO.getSenha(), existingUser.getSenha())) {
                existingUser.setSenha(passwordEncoder.encode(userDTO.getSenha()));
            }
            if (userDTO.getDataCriacao() != null && !userDTO.getDataCriacao().equals(existingUser.getDataCriacao())) {
                existingUser.setDataCriacao(userDTO.getDataCriacao());
            }
            return convertToDTO(userRepository.save(existingUser));
        } catch (StaleObjectStateException ex) {
            throw new OptimisticLockException("The data you were trying to update has been modified by another user. Please refresh the data and try again.");
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getSenha(),
                user.getDataCriacao(),
                user.getUserType()
        );
    }
}