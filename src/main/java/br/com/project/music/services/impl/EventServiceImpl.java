package br.com.project.music.services.impl;

import br.com.project.music.business.dtos.EventDTO;
import br.com.project.music.business.entities.Event;
import br.com.project.music.business.entities.Genre;
import br.com.project.music.business.entities.Reserva;
import br.com.project.music.business.entities.User;
import br.com.project.music.business.repositories.EventRepository;
import br.com.project.music.business.repositories.GenresRepository;
import br.com.project.music.business.repositories.UserRepository;
import br.com.project.music.exceptions.EventCreationException;
import br.com.project.music.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GenresRepository genresRepository;

    @Value("${file.upload.event-images-dir}")
    private String eventUploadDirectory;

    @Override
    @Transactional
    public EventDTO createEvent(EventDTO eventDTO) {
        Optional<Event> existingEvent = eventRepository.findByDataHora(eventDTO.getDataHora());

        if (existingEvent.isPresent()) {
            throw new EventCreationException("Já existe um evento agendado para esta data e hora.");
        }

        Event event = new Event();
        event.setNomeEvento(eventDTO.getNomeEvento());
        event.setDataHora(eventDTO.getDataHora());
        event.setHoraEncerramento(eventDTO.getHoraEncerramento());
        event.setDescricao(eventDTO.getDescricao());
        event.setClassificacao(eventDTO.getClassificacao());
        event.setGeneroMusical(eventDTO.getGeneroMusical());
        event.setLocalEvento(eventDTO.getLocalEvento());
        event.setHost(eventDTO.getHost());
        event.setFoto(eventDTO.getFoto());
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
            event.setHoraEncerramento(eventDTO.getHoraEncerramento());
            event.setDescricao(eventDTO.getDescricao());
            event.setClassificacao(eventDTO.getClassificacao());
            event.setGeneroMusical(eventDTO.getGeneroMusical());
            event.setLocalEvento(eventDTO.getLocalEvento());
            event.setHost(eventDTO.getHost());
            event.setFoto(eventDTO.getFoto());
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
    @Override
    public List<Event> getEventByReserva(Reserva reserva) {
        return eventRepository.findByReservas(reserva);
    }

    @Override
    public List<Event> findEventsWithReservations(){
        return eventRepository.findByHasReservas();
    }

    @Override
    public List<Event> getEventByReservaId(Long reservaId){
        return eventRepository.findByReservas_IdReserva(reservaId);
    }
    public String uploadEventImage(Long eventId, MultipartFile file) throws IOException{
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado com ID: " + eventId));

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(eventUploadDirectory);

        if(!Files.exists(uploadPath)){
            Files.createDirectories(uploadPath);
        }
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        event.setFoto(fileName);
        event.setEventPictureContentType(file.getContentType());
        eventRepository.save(event);
        return fileName;
    }
    public Resource getEventImage(Long eventId) throws IOException{
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado com ID: " + eventId));
        String fileName = event.getFoto();
        if(fileName == null || fileName.isEmpty()){
            throw new RuntimeException("Imagem de evento não encontrada para o ID: " + eventId);
        }
        try {
            Path filePath = Paths.get(eventUploadDirectory).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists() && resource.isReadable()){
                return resource;
            } else {
                throw new RuntimeException("Arquivo de imagem do evento não encontrado ou não legível: " + fileName);
            }
        } catch (MalformedURLException e){
            throw new RuntimeException("Erro na URL do recurso da imagem do evento: " + fileName, e);
        }
    }
    public String getEventImageContentType(Long eventId) throws IOException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado com ID: " + eventId));
        String contentType = event.getEventPictureContentType();
        if (contentType == null || contentType.isEmpty()) {
            String fileName = event.getFoto();
            if (fileName != null && !fileName.isEmpty()) {
                Path filePath = Paths.get(eventUploadDirectory).resolve(fileName).normalize();
                contentType = Files.probeContentType(filePath);
            }
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
        }
        return contentType;
    }
    private EventDTO convertToDTO(Event event) {
        return new EventDTO(
                event.getIdEvento(),
                event.getNomeEvento(),
                event.getDataHora(),
                event.getHoraEncerramento(),
                event.getDescricao(),
                event.getClassificacao(),
                event.getGeneroMusical(),
                event.getLocalEvento(),
                event.getHost(),
                event.getFoto(),
                event.getEventPictureContentType()
        );
    }
}