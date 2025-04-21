package br.com.project.music.services; // Ajuste o pacote conforme a sua estrutura

import br.com.project.music.business.entities.Genre;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GenreService {

    private final Map<Long, Genre> genres = new HashMap<>();
    private Long nextId = 1L;

    public Genre createGenre(Genre genre) {
        genre.setIdGeneroMusical(nextId++);
        genres.put(genre.getIdGeneroMusical(), genre);
        return genre;
    }

    public Optional<Genre> getGenreById(Long id) {
        return Optional.ofNullable(genres.get(id));
    }

    public Optional<Genre> getGenreByName(String name) {
        return genres.values().stream()
                .filter(g -> g.getNomeGenero().equals(name))
                .findFirst();
    }

    public List<Genre> getAllGenres() {
        return new ArrayList<>(genres.values());
    }

    public Genre updateGenre(Long id, Genre updatedGenre) {
        if (genres.containsKey(id)) {
            updatedGenre.setIdGeneroMusical(id);
            genres.put(id, updatedGenre);
            return updatedGenre;
        }
        return null;
    }

    public boolean deleteGenre(Long id) {
        return genres.remove(id) != null;
    }
}