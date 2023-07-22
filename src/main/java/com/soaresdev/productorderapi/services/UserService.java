package com.soaresdev.productorderapi.services;

import com.soaresdev.productorderapi.dtos.UserDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.UserInsertDTO;
import com.soaresdev.productorderapi.entities.User;
import com.soaresdev.productorderapi.repositories.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository userRepository, ModelMapper modelMapper, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public Page<UserDTO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserDTO::new);
    }

    public UserDTO findByUUID(String uuid) {
        return new UserDTO(getUser(uuid));
    }

    @Transactional
    public UserDTO insert(UserInsertDTO userInsertDTO) {
        if(userRepository.existsByEmail(userInsertDTO.getEmail()))
            throw new EntityExistsException("Email already exists");

        userInsertDTO.setPassword(encryptPassword(userInsertDTO.getPassword()));
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

        userInsertDTO.setPassword(encryptPassword(userInsertDTO.getPassword()));
        modelMapper.map(userInsertDTO, user);
        user = userRepository.save(user);
        return new UserDTO(user);
    }

    private User getUser(String uuid) {
        return userRepository.findById(UUID.fromString(uuid))
               .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private String encryptPassword(String password) {
        return bCryptPasswordEncoder.encode(password);
    }
}