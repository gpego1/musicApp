package br.com.project.music.services.impl;

import br.com.project.music.business.dtos.EventDTO;
import br.com.project.music.business.entities.Event;
import br.com.project.music.business.repositories.EventRepository;
import br.com.project.music.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    @Autowired
    private EventRepository eventRepository;

    @Override
    @Transactional
    public EventDTO createEvent(EventDTO eventDTO) {
        Event event = new Event();
        event.setNomeEvento(eventDTO.getNomeEvento());
        event.setDataHora(eventDTO.getDataHora());
        event.setDescricao(eventDTO.getDescricao());
        event.setGeneroMusical(eventDTO.getGeneroMusical());
        event.setLocalEvento(eventDTO.getLocalEvento());
        Event savedEvent = eventRepository.save(event);
        return convertToDTO(savedEvent);
    }

    @Override
    public List<EventDTO> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<EventDTO> getEventById(Long id) {
        return eventRepository.findById(id).map(this::convertToDTO);
    }

    @Override
    @Transactional
    public EventDTO updateEvent(Long id, EventDTO eventDTO) {
        Event event = eventRepository.findById(id).orElse(null);
        if (event != null) {
            event.setNomeEvento(eventDTO.getNomeEvento());
            event.setDataHora(eventDTO.getDataHora());
            event.setDescricao(eventDTO.getDescricao());
            event.setGeneroMusical(eventDTO.getGeneroMusical());
            event.setLocalEvento(eventDTO.getLocalEvento());
            return convertToDTO(eventRepository.save(event));
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteEventById(Long id) {
        eventRepository.deleteById(id);
    }

    private EventDTO convertToDTO(Event event) {
        return new EventDTO(
                event.getIdEvento(),
                event.getNomeEvento(),
                event.getDataHora(),
                event.getDescricao(),
                event.getGeneroMusical(),
                event.getLocalEvento()
        );
    }
}