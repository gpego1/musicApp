package br.com.project.music.controllers;

import br.com.project.music.business.dtos.EventDTO;
import br.com.project.music.business.entities.Event;
import br.com.project.music.business.repositories.EventRepository;
import br.com.project.music.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/eventos")
public class EventController {
    private final EventService eventService;
    private final EventRepository eventRepository;

    @Autowired
    public EventController(EventService eventService, EventRepository eventRepository) {
        this.eventService = eventService;
        this.eventRepository = eventRepository;
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

    @GetMapping("/host/{hostId}")
    public ResponseEntity<List<Event>> getEventsByHost(@PathVariable Long hostId){
        List<Event> events = eventService.getEventsByHostId(hostId);
        if(events.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(events);
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
    @PostMapping
    public ResponseEntity<EventDTO> createEvent(@RequestBody EventDTO eventDTO) {
        EventDTO savedEvent = eventService.createEvent(eventDTO);
        return new ResponseEntity<>(savedEvent, HttpStatus.CREATED);
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