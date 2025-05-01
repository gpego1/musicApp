package br.com.project.music.services; // Ajuste o pacote conforme a sua estrutura

import br.com.project.music.business.entities.Place;
import br.com.project.music.business.repositories.PlaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlaceService {

    private final PlaceRepository placeRepository;

    @Autowired
    public PlaceService(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    public List<Place> getAllPlaces() {
        return placeRepository.findAll();
    }

    public Optional<Place> getPlaceById(Long id) {
        return placeRepository.findById(id);
    }

    public Place createPlace(Place place) {
        return placeRepository.save(place);
    }

    public Place updatePlace(Long id, Place updatedPlace) {
        return placeRepository.findById(id)
                .map(existingPlace -> {
                    updatedPlace.setIdLocalEvento(id);
                    return placeRepository.save(updatedPlace);
                })
                .orElse(null);
    }

    public void deletePlace(Long id) {
        placeRepository.deleteById(id);
    }

    public Optional<Place> getPlaceByLocal(String local) {
        return placeRepository.findByLocal(local);
    }
}