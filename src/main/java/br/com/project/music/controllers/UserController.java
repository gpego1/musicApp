package br.com.project.music.controllers;

import br.com.project.music.business.dtos.ArtistUpdateRequest;
import br.com.project.music.business.dtos.Auth;
import br.com.project.music.business.dtos.UserDTO;
import br.com.project.music.business.entities.Musico;
import br.com.project.music.business.entities.User;
import br.com.project.music.business.repositories.MusicoRepository;
import br.com.project.music.business.repositories.UserRepository;
import br.com.project.music.exceptions.ResourceNotFoundException;
import br.com.project.music.services.UserService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MusicoRepository musicoRepository;

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        UserDTO createdUserDTO = userService.createUser(userDTO);
        return ResponseEntity.ok(createdUserDTO);
    }
    @PostMapping("/upload-profile-image")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("profileImage") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        try {
            userService.updateProfileImage(username, file);
            return ResponseEntity.ok("Foto de perfil atualizada com sucesso.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao salvar a foto de perfil.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro desconhecido ao processar a foto de perfil: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userDTO = userService.getUserById(id);
        if (userDTO != null) {
            return ResponseEntity.ok(userDTO);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        String roleStr = request.get("role");
        if (roleStr != null) {
            try {
                user.setRole(User.Role.valueOf(roleStr.toUpperCase()));
                userRepository.save(user);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Role inválida: " + roleStr);
            }
        }
        String nameStr = request.get("name");
        if (nameStr != null) {
            user.setName(nameStr);
            userRepository.save(user);
        }
        if (roleStr != null && nameStr != null) {
            return ResponseEntity.ok().body(Map.of(
                    "message", "Nome atualizado para " + user.getName() + " e Role atualizado para " + user.getRole()
            ));
        } else if (roleStr != null) {
            return ResponseEntity.ok().body(Collections.singletonMap(
                    "message", "Role atualizado para " + user.getRole()
            ));
        } else if (nameStr != null) {
            return ResponseEntity.ok().body(Collections.singletonMap(
                    "message", "Nome atualizado para " + user.getName()
            ));
        } else {
            return ResponseEntity.badRequest().body(Collections.singletonMap(
                    "message", "Nenhum campo para atualizar fornecido (role ou name)"
            ));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser (@PathVariable Long id){
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}