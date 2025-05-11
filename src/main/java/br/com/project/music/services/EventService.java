package br.com.project.music.services;

import br.com.project.music.business.dtos.EventDTO;
import br.com.project.music.business.entities.Event;

import java.util.List;
import java.util.Optional;

public interface EventService {
    EventDTO createEvent(EventDTO eventDTO);
    List<EventDTO> getAllEvents();
    Optional<EventDTO> getEventById(Long id);
    EventDTO updateEvent(Long id, EventDTO eventDTO);
    void deleteEventById(Long id);
    List<Event> getEventsByHostId(Long hostId);
}