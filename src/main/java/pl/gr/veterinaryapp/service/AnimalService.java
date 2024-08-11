package pl.gr.veterinaryapp.service;

import pl.gr.veterinaryapp.model.dto.AnimalRequestDto;
import pl.gr.veterinaryapp.model.entity.Animal;

import java.util.List;

public interface AnimalService {

    AnimalRequestDto getAnimalById(Long id);

    Animal createAnimal(AnimalRequestDto animalRequestDTO);

    void deleteAnimal(Long id);

    List<AnimalRequestDto> getAllAnimals();
}
