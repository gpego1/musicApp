package br.com.project.music.controllers;

import br.com.project.music.business.dtos.ArtistUpdateRequest;
import br.com.project.music.business.dtos.UserDTO;
import br.com.project.music.business.entities.Musico;
import br.com.project.music.business.entities.User;
import br.com.project.music.business.repositories.MusicoRepository;
import br.com.project.music.business.repositories.UserRepository;
import br.com.project.music.exceptions.ResourceNotFoundException;
import br.com.project.music.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> updateToArtist(
            @PathVariable Long id,
            @RequestBody ArtistUpdateRequest request) {

        // 1. Validar usuário
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Atualizar role
        user.setRole(User.Role.ARTISTA);
        userRepository.save(user);

        Musico musico = new Musico();
        musico.setNomeArtistico(request.getNome_artistico());
        musico.setRedesSociais(request.getRedes_sociais());
        musico.setUsuario(user);
        musicoRepository.save(musico);

        return ResponseEntity.ok(Map.of(
                "message", "User converted to artist successfully",
                "artist", musico
        ));
    }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteUser (@PathVariable Long id){
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        }
}