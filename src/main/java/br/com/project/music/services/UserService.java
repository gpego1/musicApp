package br.com.project.music.services;

import br.com.project.music.business.dtos.UserDTO;
import br.com.project.music.business.entities.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface UserService extends UserDetailsService {
    UserDTO createUser(UserDTO userDTO);
    List<UserDTO> getAllUsers();
    UserDTO getUserById(Long id);
    Optional<User> getUserByEmail(String email);
    User getUserByEmailAndSenha(String email, String senha);
    UserDTO updateUser(Long id, UserDTO userDTO);
    void deleteUser(Long id);
}
