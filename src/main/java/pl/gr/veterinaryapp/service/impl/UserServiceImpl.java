package pl.gr.veterinaryapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.gr.veterinaryapp.exception.IncorrectDataException;
import pl.gr.veterinaryapp.exception.ResourceNotFoundException;
import pl.gr.veterinaryapp.model.dto.UserDto;
import pl.gr.veterinaryapp.model.entity.Role;
import pl.gr.veterinaryapp.model.entity.VetAppUser;
import pl.gr.veterinaryapp.repository.UserRepository;
import pl.gr.veterinaryapp.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Override
    public List<VetAppUser> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public VetAppUser getUser(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with ID: {} not found.", id);
                    return new ResourceNotFoundException("Wrong id.");
                });
    }

    @Override
    @Transactional
    public VetAppUser createUser(UserDto user) {
        userRepository.findByUsername(user.getUsername())
                .ifPresent(u -> {
                    throw new IncorrectDataException("Username exists.");
                });
        VetAppUser newVetAppUser = new VetAppUser();
        newVetAppUser.setUsername(user.getUsername());
        newVetAppUser.setPassword(encoder.encode(user.getPassword()));
        newVetAppUser.setRole(new Role(user.getRole()));

        VetAppUser savedUser = userRepository.save(newVetAppUser);
        log.info("User created successfully with ID: {} and username: {}", savedUser.getId(), savedUser.getUsername());

        return savedUser;
    }

    @Override
    @Transactional
    public void deleteUser(long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Wrong id."));
        userRepository.delete(user);
        log.info("User with ID: {} deleted successfully.", id);
    }
}
