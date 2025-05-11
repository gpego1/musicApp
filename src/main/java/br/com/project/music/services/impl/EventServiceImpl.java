package br.com.project.music.services.impl;

import br.com.project.music.business.dtos.EventDTO;
import br.com.project.music.business.entities.Event;
import br.com.project.music.business.entities.Genre;
import br.com.project.music.business.entities.User;
import br.com.project.music.business.repositories.EventRepository;
import br.com.project.music.business.repositories.GenresRepository;
import br.com.project.music.business.repositories.UserRepository;
import br.com.project.music.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GenresRepository genresRepository;

    @Override
    @Transactional
    public EventDTO createEvent(EventDTO eventDTO) {
        Event event = new Event();
        event.setNomeEvento(eventDTO.getNomeEvento());
        event.setDataHora(eventDTO.getDataHora());
        event.setDescricao(eventDTO.getDescricao());
        event.setGeneroMusical(eventDTO.getGeneroMusical());
        event.setLocalEvento(eventDTO.getLocalEvento());
        event.setHost(eventDTO.getHost());
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
            event.setHost(eventDTO.getHost());
            return convertToDTO(eventRepository.save(event));
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteEventById(Long id) {
        eventRepository.deleteById(id);
    }

    public List<Event> getEventsByHostId(Long hostId) {
        Optional<User> host = userRepository.findById(hostId);
        if(host.isPresent()) {
            User createdHost = host.get();
            return eventRepository.findByHost(createdHost);
        } else {
            return Collections.emptyList();
        }
    }
    public List<Event> getEventsByGenreId(Long genreId) {
        Optional<Genre> genre = genresRepository.findById(genreId);
        if(genre.isPresent()) {
            Genre createdGenre = genre.get();
            return eventRepository.findByGeneroMusical(createdGenre);
        } else {
            return Collections.emptyList();
        }
    }
    @Override
    public List<Event> findByData(LocalDateTime data){
        return eventRepository.findByData(data);
    }

    @Override
    public List<Event> getFutureEvents(){
        LocalDateTime now = LocalDateTime.now();
        return eventRepository.findByDataHoraAfter(now);
    }

    @Override
    public List<Event> getPastEvents(){
        LocalDateTime now = LocalDateTime.now();
        return eventRepository.findByDataHoraBefore(now);
    }

    @Override
    public String getEventStatus(Event event) {
        LocalDateTime now = LocalDateTime.now();
        if(event.getDataHora().isAfter(now)){
            return "Futuro";
        } else if(event.getDataHora().isBefore(now)){
            return "Passado";
        } else {
            return "Acontecendo Agora";
        }
    }

    @Override
    public List<Event> getEventsOnDate(LocalDateTime date){
        return eventRepository.findByData(date);
    }

    private EventDTO convertToDTO(Event event) {
        return new EventDTO(
                event.getIdEvento(),
                event.getNomeEvento(),
                event.getDataHora(),
                event.getDescricao(),
                event.getGeneroMusical(),
                event.getLocalEvento(),
                event.getHost()
        );
    }
}