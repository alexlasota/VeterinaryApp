package pl.gr.veterinaryapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.gr.veterinaryapp.exception.IncorrectDataException;
import pl.gr.veterinaryapp.exception.ResourceNotFoundException;
import pl.gr.veterinaryapp.model.dto.PetRequestDto;
import pl.gr.veterinaryapp.model.entity.Animal;
import pl.gr.veterinaryapp.model.entity.Client;
import pl.gr.veterinaryapp.model.entity.Pet;
import pl.gr.veterinaryapp.repository.AnimalRepository;
import pl.gr.veterinaryapp.repository.ClientRepository;
import pl.gr.veterinaryapp.repository.PetRepository;
import pl.gr.veterinaryapp.service.PetService;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;
    private final ClientRepository clientRepository;
    private final AnimalRepository animalRepository;

    @Override
    public List<Pet> getAllPets(User user) {
        return petRepository.findAll()
                .stream()
                .filter(pet -> isUserAuthorized(user, pet.getClient()))
                .collect(Collectors.toList());
    }

    @Override
    public Pet getPetById(User user, long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wrong id."));

        if (!isUserAuthorized(user, pet.getClient())) {
            log.error("User: {} is not authorized to access pet with ID: {}", user.getUsername(), id);
            throw new ResourceNotFoundException("Wrong id.");
        }

        return pet;
    }

    @Transactional
    @Override
    public Pet createPet(User user, PetRequestDto petRequestDto) {
        if (petRequestDto.getName() == null) {
            throw new IncorrectDataException("Name cannot be null.");
        }

        if (petRequestDto.getBirthDate() == null) {
            throw new IncorrectDataException("Birth date cannot be null.");
        }

        Animal animal = animalRepository.findById(petRequestDto.getAnimalId())
                .orElseThrow(() -> new IncorrectDataException("Wrong animal id."));
        Client client = clientRepository.findById(petRequestDto.getClientId())
                .orElseThrow(() -> new IncorrectDataException("Wrong client id."));

        if (!isUserAuthorized(user, client)) {
            throw new ResourceNotFoundException("User don't have access to this pet");
        }

        var newPet = new Pet();
        newPet.setName(petRequestDto.getName());
        newPet.setBirthDate(petRequestDto.getBirthDate());
        newPet.setAnimal(animal);
        newPet.setClient(client);

        Pet savedPet = petRepository.save(newPet);
        log.info("Pet created successfully with ID: {} for user: {}", savedPet.getId(), user.getUsername());

        return savedPet;
    }

    @Transactional
    @Override
    public void deletePet(long id) {
        Pet result = petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wrong id."));
        petRepository.delete(result);
        log.info("Pet with ID: {} deleted successfully.", id);
    }

    private boolean isUserAuthorized(User user, Client client) {
        boolean isClient = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_CLIENT"::equalsIgnoreCase);
        if (isClient) {
            if (client.getUser() == null) {
                return false;
            } else {
                return client.getUser().getUsername().equalsIgnoreCase(user.getUsername());
            }
        }
        return true;
    }
}
