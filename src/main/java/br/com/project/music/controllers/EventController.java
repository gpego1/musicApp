package br.com.project.music.controllers;

import br.com.project.music.business.dtos.EventDTO;
import br.com.project.music.business.entities.Event;
import br.com.project.music.business.entities.User;
import br.com.project.music.business.repositories.EventRepository;
import br.com.project.music.business.repositories.UserRepository;
import br.com.project.music.services.EventService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/eventos")
@Tag(name="Eventos", description = "Gerenciamento de eventos")

public class EventController {
    private final EventService eventService;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Autowired
    public EventController(EventService eventService, EventRepository eventRepository, UserRepository userRepository) {
        this.eventService = eventService;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<EventDTO>> getAllEvents() {
        List<EventDTO> events = eventService.getAllEvents();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<EventDTO>> getEventById(@PathVariable Long id) {
        Optional<EventDTO> event = eventService.getEventById(id);
        if (event.isPresent()){
            return new ResponseEntity<>(event, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/search")
    public List<Event> findByNomeEventoContaining(@RequestParam String nomeEvento) {
        List<Event> events;
        if (nomeEvento == null || nomeEvento.isEmpty()) {
            events = eventRepository.findAll();
        } else {
            events = eventRepository.findByNomeEventoContaining(nomeEvento);
        }
        for (Event event : events) {
            if (event.getIdEvento() != null) {
                event.setFoto("/eventos/" + event.getIdEvento() + "/image");
            } else {
                event.setFoto(null);
            }
        }
        return events;
    }

    @GetMapping("/host/{hostId}")
    public ResponseEntity<List<Event>> getEventsByHost(@PathVariable Long hostId){
        List<Event> events = eventService.getEventsByHostId(hostId);
        if(events.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(events);
    }
    @GetMapping("/host/{hostId}/future")
    public ResponseEntity<List<Event>> getFutureEventsByHost(@PathVariable Long hostId){
        Optional<User> hostOptional = userRepository.findById(hostId);
        if(hostOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        LocalDateTime now = LocalDateTime.now();
        List<Event> futureEvents = eventRepository.findByHostIdAndDataHoraAfterOrderByDataHoraAsc(hostId, now);
        if(futureEvents.isEmpty()){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
        return ResponseEntity.ok(futureEvents);
    }
    @GetMapping("/host/{hostId}/past")
    public ResponseEntity<List<Event>> getPastEventsByHost(@PathVariable Long hostId){
        Optional<User> hostOptional = userRepository.findById(hostId);
        if(hostOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        LocalDateTime now = LocalDateTime.now();
        List<Event> pastEvents = eventRepository.findByHostIdAndDataHoraBeforeOrderByDataHoraDesc(hostId, now);
        if(pastEvents.isEmpty()){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
        return ResponseEntity.ok(pastEvents);
    }
    @GetMapping("/{idEvento}/host-id")
    public ResponseEntity<Long> getHostIdByEvent(@PathVariable Long idEvento) {
        Optional<Event> eventOptional = eventRepository.findById(idEvento);
        if(eventOptional.isPresent()){
            Event event = eventOptional.get();
            if(event.getHost() != null && event.getHost().getId() != null){
                return ResponseEntity.ok(event.getHost().getId());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/genre/{genreId}")
    public ResponseEntity<List<Event>> gentEventsByGenre(@PathVariable Long genreId){
        List<Event> events = eventService.getEventsByGenreId(genreId);
        if(events.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(events);
    }
    @GetMapping("/future")
    public ResponseEntity<List<Event>> getFutureEvents(){
        List<Event> futureEvents = eventService.getFutureEvents();
        if(futureEvents.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(futureEvents);
    }
    @GetMapping("/past")
    public ResponseEntity<List<Event>> getPastEvents(){
        List<Event> pastEvents = eventService.getPastEvents();
        if(pastEvents.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(pastEvents);
    }
    @GetMapping("/{id}/status")
    public ResponseEntity<String> getEventStatus(@PathVariable Long id) {
        Optional<EventDTO> eventOptional = eventService.getEventById(id);
        if (eventOptional.isPresent()) {
            Optional<Event> eventEntityOptional = eventRepository.findByIdEvento(id);
            if (eventEntityOptional.isPresent()) {
                String status = eventService.getEventStatus(eventEntityOptional.get());
                return new ResponseEntity<>(status, HttpStatus.OK);
            } else {
                return ResponseEntity.notFound().build();
            }
        }
        return ResponseEntity.notFound().build();
    }
    @GetMapping("/on-date")
    public ResponseEntity<List<Event>> getEventsOnDate(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime date){
        List<Event> eventsOnDate = eventService.getEventsOnDate(date);
        if(eventsOnDate.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(eventsOnDate);
    }
    @GetMapping("/reservas/{reservaId}")
    public ResponseEntity<List<Event>> getEventsByReservaId(@PathVariable Long reservaId){
        List<Event> events = eventService.getEventByReservaId(reservaId);
        if(events.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(events);
    }
    @PostMapping
    public ResponseEntity<EventDTO> createEvent(@RequestBody EventDTO eventDTO) {
        EventDTO savedEvent = eventService.createEvent(eventDTO);
        return new ResponseEntity<>(savedEvent, HttpStatus.CREATED);
    }
    @PostMapping("/{eventId}/upload")
    public ResponseEntity<String> uploadEventImage(@PathVariable Long eventId, @RequestParam("foto") MultipartFile file) {
             try {
                 String s3Key = eventService.uploadEventImage(eventId, file);
                 return ResponseEntity.ok("Imagem carregada com sucesso! Chave S3: " + s3Key);
             } catch (IOException e) {
               return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao carregar imagem: " + e.getMessage());
            }
         }
    @GetMapping("/{eventId}/image")
    public ResponseEntity<Void> getEventImage(@PathVariable Long eventId) {
        try {
            URL s3ImageUrl = eventService.getEventImage(eventId);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(s3ImageUrl.toURI())
                    .build();

        } catch (RuntimeException e) {
            if (e.getMessage().contains("Evento não encontrado") || e.getMessage().contains("Chave S3 da imagem de evento não encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            System.err.println("Erro ao buscar imagem do evento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (URISyntaxException e) {
            System.err.println("Erro de sintaxe de URI ao redirecionar para imagem S3: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            System.err.println("Erro inesperado ao processar requisição de imagem: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<EventDTO> updateEvent(@PathVariable Long id, @RequestBody EventDTO eventDTO) {
        EventDTO updatedEvent = eventService.updateEvent(id, eventDTO);
        if (updatedEvent != null) {
            return new ResponseEntity<>(updatedEvent, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEventById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}