package br.com.project.music.services;

import br.com.project.music.business.dtos.EventDTO;
import br.com.project.music.business.entities.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventService {
    EventDTO createEvent(EventDTO eventDTO);
    List<EventDTO> getAllEvents();
    Optional<EventDTO> getEventById(Long id);
    EventDTO updateEvent(Long id, EventDTO eventDTO);
    void deleteEventById(Long id);
    List<Event> getEventsByHostId(Long hostId);
    List<Event> getEventsByGenreId(Long genreId);
    List<Event> findByData(LocalDateTime data);
    List<Event> getFutureEvents();
    List<Event> getPastEvents();
    String getEventStatus(Event event);
    List<Event> getEventsOnDate(LocalDateTime date);
}