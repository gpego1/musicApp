package br.com.project.music.services;
import br.com.project.music.business.dtos.UserDTO;
import br.com.project.music.business.entities.User;
import br.com.project.music.business.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.hibernate.StaleObjectStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createUser(UserDTO userDTO) {
        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setSenha(userDTO.getSenha());
        if(userDTO.getDataCriacao() == null) {
            user.setDataCriacao(Timestamp.from(Instant.now()));
        } else {
            user.setDataCriacao(userDTO.getDataCriacao());
        }
        return userRepository.save(user);
    }
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }
    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public User getUserByEmailAndSenha(String email, String senha) {
        return userRepository.findByEmailAndSenha(email, senha).orElse(null);
    }

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
            if (userDTO.getSenha() != null && !userDTO.getSenha().equals(existingUser.getSenha())) {
                existingUser.setSenha(userDTO.getSenha());
            }
            if(userDTO.getDataCriacao() != null && !userDTO.getDataCriacao().equals(existingUser.getDataCriacao())){
                existingUser.setDataCriacao(userDTO.getDataCriacao());
            }

            return convertToDTO(userRepository.save(existingUser));
        } catch (StaleObjectStateException ex) {
            throw new OptimisticLockException("The data you were trying to update has been modified by another user. Please refresh the data and try again.");
        }
    }
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getSenha(),
                user.getDataCriacao()
        );
    }

}
