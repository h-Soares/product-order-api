package com.soaresdev.productorderapi.services;

import com.soaresdev.productorderapi.dtos.UserDTO;
import com.soaresdev.productorderapi.entities.User;
import com.soaresdev.productorderapi.repositories.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDTO> findAll() {
        return userRepository.findAll().stream().map(UserDTO::new).toList(); //page here...
    }

    public UserDTO findByUUID(String uuid) {
        Optional<User> optionalUser = userRepository.findById(UUID.fromString(uuid));
        return new UserDTO(optionalUser.get()); //handle exception here...
    }
}