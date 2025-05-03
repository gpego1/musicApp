package br.com.project.music.controllers;

import br.com.project.music.business.dtos.ArtistUpdateRequest;
import br.com.project.music.business.dtos.UserDTO;
import br.com.project.music.business.entities.Musico;
import br.com.project.music.business.entities.User;
import br.com.project.music.business.repositories.MusicoRepository;
import br.com.project.music.business.repositories.UserRepository;
import br.com.project.music.exceptions.ResourceNotFoundException;
import br.com.project.music.services.UserService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        // 1. Apenas atualiza o role
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String roleStr = request.get("role");
        if (roleStr == null) {
            throw new RuntimeException("Campo 'role' é obrigatório");
        }

        user.setRole(User.Role.valueOf(roleStr.toUpperCase()));
        userRepository.save(user);

        // 2. Retorna resposta simples
        return ResponseEntity.ok().body(Collections.singletonMap(
                "message", "Role atualizado para " + user.getRole()
        ));
    }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteUser (@PathVariable Long id){
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        }
}