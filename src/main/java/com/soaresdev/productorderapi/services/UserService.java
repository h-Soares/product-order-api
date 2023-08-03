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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public UserService(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmailWithEagerRoles(email).
               orElseThrow(() -> new EntityNotFoundException("Email not exists"));
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

        User user = modelMapper.map(userInsertDTO, User.class, "createUserConverter");
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

        modelMapper.map(userInsertDTO, user, "updateUserConverter");
        user = userRepository.save(user);
        return new UserDTO(user);
    }

    private User getUser(String uuid) {
        return userRepository.findById(UUID.fromString(uuid))
               .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}