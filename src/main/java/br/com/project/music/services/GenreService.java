package br.com.project.music.services; // Ajuste o pacote conforme a sua estrutura

import br.com.project.music.business.entities.Genre;
import br.com.project.music.business.repositories.GenresRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GenreService {

    private final GenresRepository genreRepository;

    @Autowired
    public GenreService(GenresRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public Genre createGenre(Genre genre) {
        return genreRepository.save(genre);
    }

    public Optional<Genre> getGenreById(Long id) {
        return genreRepository.findById(id);
    }

    public Optional<Genre> getGenreByName(String nomeGenero) {
        return genreRepository.findByNomeGenero(nomeGenero);
    }

    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    public Genre updateGenre(Long id, Genre updatedGenre) {
        return genreRepository.findById(id)
                .map(existingGenre -> {
                    updatedGenre.setIdGeneroMusical(id);
                    return genreRepository.save(updatedGenre);
                })
                .orElse(null);
    }

    public boolean deleteGenre(Long id) {
        if (genreRepository.existsById(id)) {
            genreRepository.deleteById(id);
            return true;
        }
        return false;
    }
}