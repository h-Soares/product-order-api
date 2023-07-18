package com.soaresdev.productorderapi.services;

import com.soaresdev.productorderapi.dtos.UserDTO;
import com.soaresdev.productorderapi.dtos.UserInsertDTO;
import com.soaresdev.productorderapi.entities.User;
import com.soaresdev.productorderapi.repositories.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public UserService(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    public List<UserDTO> findAll() {
        return userRepository.findAll().stream().map(UserDTO::new).toList(); // TODO: page
    }

    public UserDTO findByUUID(String uuid) {
        return new UserDTO(getUser(uuid));
    }

    @Transactional
    //TODO: Encrypt password
    public UserDTO insert(UserInsertDTO userInsertDTO) {
        if(userRepository.existsByEmail(userInsertDTO.getEmail()))
            throw new EntityExistsException("Email already exists");

        User user = modelMapper.map(userInsertDTO, User.class);
        user = userRepository.save(user);
        return new UserDTO(user);
    }

    @Transactional
    public void deleteByUUID(String uuid) {
        userRepository.delete(getUser(uuid));
    }

    @Transactional
    public UserDTO updateByUUID(String uuid, UserInsertDTO userInsertDTO) {
        User user = getUser(uuid);
        if(!userInsertDTO.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(userInsertDTO.getEmail()))
            throw new EntityExistsException("Email already exists");

        modelMapper.map(userInsertDTO, user);
        return new UserDTO(user);
    }

    private User getUser(String uuid) {
        return userRepository.findById(UUID.fromString(uuid))
               .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}