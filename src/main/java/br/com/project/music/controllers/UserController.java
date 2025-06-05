package br.com.project.music.controllers;
import br.com.project.music.business.dtos.UserDTO;
import br.com.project.music.business.repositories.MusicoRepository;
import br.com.project.music.business.repositories.UserRepository;
import br.com.project.music.exceptions.EntityNotFoundException;
import br.com.project.music.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;


@RestController
@RequestMapping("/users")
@Tag(name="Usuários", description = "Gerenciamento de usuários do sistema")

public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

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
        if (userRepository.count() == 0) {
            logger.warn("No users found in the database.");
            return ResponseEntity.noContent().build();
        }
        logger.info("Returning all users from the database.");
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
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        try {
            UserDTO updatedUser = userService.updateUser(id, userDTO);
            return ResponseEntity.ok(updatedUser);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser (@PathVariable Long id){
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}