package com.soaresdev.productorderapi.services;

import com.soaresdev.productorderapi.dtos.UserDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.UserInsertDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.UserRoleInsertDTO;
import com.soaresdev.productorderapi.entities.Role;
import com.soaresdev.productorderapi.entities.User;
import com.soaresdev.productorderapi.entities.enums.RoleName;
import com.soaresdev.productorderapi.repositories.RoleRepository;
import com.soaresdev.productorderapi.repositories.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;


class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ModelMapper modelMapper;

    private static final UUID RANDOM_UUID = UUID.randomUUID();

    private User user;
    private User diffentUser;
    private UserInsertDTO userInsertDTO;
    private Role role;
    private UserRoleInsertDTO userRoleInsertDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        init();
    }

    @Test
    void shouldLoadUserByUsername() {
        when(userRepository.findByEmailWithEagerRoles(anyString()))
                .thenReturn(Optional.ofNullable(user));

        User responseUser = (User) userService.loadUserByUsername(user.getUsername());

        assertNotNull(responseUser);
        assertEquals(User.class, user.getClass());
        assertEquals(user.getName(), responseUser.getName());
        assertEquals(user.getEmail(), responseUser.getEmail());
        assertEquals(user.getPhone(), responseUser.getPhone());
        assertEquals(user.getPassword(), responseUser.getPassword());
        assertFalse(responseUser.getRoles().isEmpty());
        assertEquals(1, responseUser.getRoles().size());
        assertEquals(role.getRoleNameCode(), responseUser.getRoles().get(0).getRoleNameCode());
        verify(userRepository, times(1)).findByEmailWithEagerRoles(anyString());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenEmailNotExistsInLoadUserByUsername() {
        when(userRepository.findByEmailWithEagerRoles(anyString())).thenReturn(Optional.empty());

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> userService.loadUserByUsername("test@gmail.com"));
        assertEquals("Email not exists", e.getMessage());
        verify(userRepository, times(1)).findByEmailWithEagerRoles(anyString());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldFindAllUsers() {
        when(userRepository.findAll(any(Pageable.class))).
                thenReturn(new PageImpl<>(List.of(user)));

        Page<UserDTO> result = userService.findAll(PageRequest.of(0, 2));

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals(user.getName(), result.getContent().get(0).getName());
        assertEquals(user.getEmail(), result.getContent().get(0).getEmail());
        assertEquals(user.getPhone(), result.getContent().get(0).getPhone());
        verify(userRepository, times(1)).findAll(any(Pageable.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldFindUserByUUID() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(user));

        UserDTO responseUser = userService.findByUUID(RANDOM_UUID.toString());

        assertNotNull(responseUser);
        assertEquals(user.getName(), responseUser.getName());
        assertEquals(user.getEmail(), responseUser.getEmail());
        assertEquals(user.getPhone(), responseUser.getPhone());
        verify(userRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenUserNotExistsInFindUserByUUID() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Throwable e =  assertThrows(EntityNotFoundException.class,
                () -> userService.findByUUID(RANDOM_UUID.toString()));
        assertEquals("User not found", e.getMessage());
        verify(userRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldInsertUser() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(modelMapper.map(any(UserInsertDTO.class), eq(User.class),
                eq("createUserConverter"))).thenReturn(user);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            user.setId(RANDOM_UUID);
            return user;
        });

        UserDTO responseUser = userService.insert(userInsertDTO);

        assertNotNull(responseUser);
        assertEquals(user.getId(), responseUser.getId());
        assertEquals(user.getName(), responseUser.getName());
        assertEquals(user.getEmail(), responseUser.getEmail());
        assertEquals(user.getPhone(), responseUser.getPhone());
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(modelMapper, times(1)).map(any(UserInsertDTO.class),
                eq(User.class), eq("createUserConverter"));
        verify(userRepository, times(1)).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(modelMapper);
    }

    @Test
    void shouldThrowEntityExistsExceptionWhenEmailExistsInInsertUser() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        Throwable e = assertThrows(EntityExistsException.class,
                () -> userService.insert(userInsertDTO));
        assertEquals("Email already exists", e.getMessage());
        verify(userRepository, times(1)).existsByEmail(anyString());
        verifyNoInteractions(modelMapper);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldDeleteUserByUUID() {
        //to delete
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(user));
        //who accesses
        mockGetPrincipalReturns(user);
        doNothing().when(userRepository).delete(any(User.class));

        userService.deleteByUUID(RANDOM_UUID.toString());

        verify(userRepository, times(1)).findById(any(UUID.class));
        verify(userRepository, times(1)).delete(any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenUserNotExistsInDeleteUserByUUID() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> userService.deleteByUUID(RANDOM_UUID.toString()));
        assertEquals("User not found", e.getMessage());
        verify(userRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(userRepository);
    }
    @Test
    void shouldThrowAccessDeniedExceptionWhenNotAdminAndDifferentUsersInDeleteUserByUUID() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(user));
        mockGetPrincipalReturns(diffentUser);

        Throwable e = assertThrows(AccessDeniedException.class,
                () -> userService.deleteByUUID(RANDOM_UUID.toString()));
        assertEquals("Access denied", e.getMessage());
        verify(userRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldUpdateUserByUUID() {
        //to modify
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(user));
        //who accesses
        mockGetPrincipalReturns(user);
        userInsertDTO.setEmail("differentemail@gmail.com");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        doNothing().when(modelMapper).map(any(UserInsertDTO.class), any(User.class),
                eq("updateUserConverter"));
        when(userRepository.save(any(User.class))).thenAnswer(invocationOnMock -> {
            user.setId(RANDOM_UUID);
            user.setEmail(userInsertDTO.getEmail());
            return user;
        });

        UserDTO updatedUser = userService.updateByUUID(RANDOM_UUID.toString(), userInsertDTO);

        assertNotNull(updatedUser);
        assertEquals(user.getId(), updatedUser.getId());
        assertEquals(user.getName(), updatedUser.getName());
        assertEquals(user.getEmail(), updatedUser.getEmail());
        assertEquals(user.getPhone(), updatedUser.getPhone());
        verify(userRepository, times(1)).findById(any(UUID.class));
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(modelMapper, times(1)).map(any(UserInsertDTO.class),
                any(User.class), eq("updateUserConverter"));
        verify(userRepository, times(1)).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(modelMapper);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenUserNotExistsInUpdateUserByUUID() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> userService.updateByUUID(RANDOM_UUID.toString(), userInsertDTO));
        assertEquals("User not found", e.getMessage());
        verify(userRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(modelMapper);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenNotAdminAndDifferentUsersInUpdateUserByUUID() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(user));
        mockGetPrincipalReturns(diffentUser);

        Throwable e = assertThrows(AccessDeniedException.class,
                () -> userService.updateByUUID(RANDOM_UUID.toString(), userInsertDTO));
        assertEquals("Access denied", e.getMessage());
        verify(userRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(modelMapper);
    }

    @Test
    void shouldThrowEntityExistsExceptionWhenNewEmailExistsInUpdateUserByUUID() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(user));
        mockGetPrincipalReturns(user);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        userInsertDTO.setEmail("differentemail@gmail.com");

        Throwable e = assertThrows(EntityExistsException.class,
                () -> userService.updateByUUID(RANDOM_UUID.toString(), userInsertDTO));
        assertEquals("Email already exists", e.getMessage());
        verify(userRepository, times(1)).findById(any(UUID.class));
        verify(userRepository, times(1)).existsByEmail(anyString());
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(modelMapper);
    }


    @Test
    void shouldAddRoleInUser() {
        Role differentRole = new Role(RoleName.ROLE_ADMIN.getCode());
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(user));
        when(roleRepository.findByRoleNameCode(anyInt())).thenReturn(differentRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.addRole(RANDOM_UUID.toString(), userRoleInsertDTO);

        assertTrue(user.getRoles().contains(role));
        verify(userRepository, times(1)).findById(any(UUID.class));
        verify(roleRepository, times(1)).findByRoleNameCode(anyInt());
        verify(userRepository, times(1)).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(roleRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenUserNotExistsInAddRoleInUser() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> userService.addRole(RANDOM_UUID.toString(), userRoleInsertDTO));
        assertEquals("User not found", e.getMessage());
        verify(userRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(roleRepository);
    }

    @Test
    void shouldThrowEntityExistsExceptionWhenRoleExistsInAddRoleInUser() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(user));
        userRoleInsertDTO.setRoleName(RoleName.ROLE_USER);

        Throwable e = assertThrows(EntityExistsException.class,
                () -> userService.addRole(RANDOM_UUID.toString(), userRoleInsertDTO));
        assertEquals("Role already exists in this user", e.getMessage());
        verify(userRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(roleRepository);
    }

    @Test
    void shouldDeleteRoleInUser() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(user));
        when(roleRepository.findByRoleNameCode(anyInt())).thenReturn(role);
        when(userRepository.save(any(User.class))).thenReturn(user);
        userRoleInsertDTO.setRoleName(RoleName.ROLE_USER);

        userService.deleteRole(RANDOM_UUID.toString(), userRoleInsertDTO);

        assertFalse(user.getRoles().contains(role));
        verify(userRepository, times(1)).findById(any(UUID.class));
        verify(roleRepository, times(1)).findByRoleNameCode(anyInt());
        verify(userRepository, times(1)).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(roleRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenUserNotExistsInDeleteRoleInUser() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> userService.deleteRole(RANDOM_UUID.toString(), userRoleInsertDTO));
        assertEquals("User not found", e.getMessage());
        verify(userRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(roleRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenRoleNotExistsInUserDeleteRoleInUser() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(user));

        Throwable e = assertThrows(EntityNotFoundException.class,
                () -> userService.deleteRole(RANDOM_UUID.toString(), userRoleInsertDTO));
        assertEquals("Role not found in this user", e.getMessage());
        verify(userRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(roleRepository);
    }

    private void init() {
        role = new Role(RoleName.ROLE_USER.getCode());
        userRoleInsertDTO = new UserRoleInsertDTO(RoleName.ROLE_ADMIN);
        userInsertDTO = new UserInsertDTO("Test", "test@gmail.com", "test", "test");
        user = new User("Test", "test@gmail.com", "test", "test");
        user.getRoles().add(role);
        diffentUser = new User("Test2", "test2@gmail.com", "test2", "test2");
        diffentUser.getRoles().add(role);
    }

    private void mockGetPrincipalReturns(User user) {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(auth.getPrincipal()).thenReturn(user);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);
    }
}