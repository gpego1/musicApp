package br.com.project.music.services;

import br.com.project.music.business.dtos.UserDTO;
import br.com.project.music.business.entities.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    boolean checkPassword(User user, String currentPassword);
    void changePassword(User user, String newPassword);
    User registerOrLoginGoogleUser(OAuth2User oauthUser);
    User findOrCreateGoogleUser(String email, String googleId);
    String uploadProfileImage(Long userId, MultipartFile file) throws IOException;
    Optional<User> getUserEntityById(Long id);

}