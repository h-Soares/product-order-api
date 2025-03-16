package integrationtests;

import com.soaresdev.productorderapi.dtos.insertDTOs.UserInsertDTO;
import com.soaresdev.productorderapi.dtos.security.LoginDTO;
import com.soaresdev.productorderapi.dtos.security.RefreshDTO;
import com.soaresdev.productorderapi.dtos.security.TokenDTO;
import com.soaresdev.productorderapi.repositories.RoleRepository;
import com.soaresdev.productorderapi.services.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = com.soaresdev.productorderapi.ProductOrderApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@TestPropertySource("/application.properties")
class AuthIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserService userService;

    private TokenDTO token;

    @BeforeAll
    void setup() {
        userService.insert(new UserInsertDTO("Testing", "testing@email.com", "15457812345", "mypass123"));
    }

    @Test
    @Order(1)
    void shouldLoginAndReturnToken() {
        LoginDTO login = new LoginDTO("testing@email.com", "mypass123");

        token = restTemplate.postForEntity("/auth/login", login, TokenDTO.class).getBody();

        assertNotNull(token);
        assertEquals(login.getEmail(), token.getEmail());
        assertTrue(token.getAuthenticated());
        assertNotNull(token.getCreation());
        assertEquals(Instant.class, token.getCreation().getClass());
        assertNotNull(token.getExpiration());
        assertEquals(Instant.class, token.getExpiration().getClass());
        assertNotNull(token.getAccessToken());
        assertEquals(String.class, token.getAccessToken().getClass());
        assertFalse(token.getAccessToken().isBlank());
        assertNotNull(token.getRefreshToken());
        assertEquals(String.class, token.getRefreshToken().getClass());
        assertFalse(token.getRefreshToken().isBlank());
    }

    @Test
    @Order(2)
    void shouldRefreshToken() {
        RefreshDTO refreshDTO = new RefreshDTO(token.getEmail(), token.getRefreshToken());
        HttpEntity<RefreshDTO> request = new HttpEntity<>(refreshDTO);

        TokenDTO newToken = restTemplate.exchange("/auth/refresh", HttpMethod.PUT, request, TokenDTO.class).getBody();

        assertNotNull(newToken);
        assertEquals(refreshDTO.getEmail(), newToken.getEmail());
        assertTrue(newToken.getAuthenticated());
        assertNotNull(newToken.getCreation());
        assertEquals(Instant.class, newToken.getCreation().getClass());
        assertNotNull(newToken.getExpiration());
        assertEquals(Instant.class, newToken.getExpiration().getClass());
        assertNotNull(newToken.getAccessToken());
        assertEquals(String.class, newToken.getAccessToken().getClass());
        assertFalse(newToken.getAccessToken().isBlank());
        assertNotNull(newToken.getRefreshToken());
        assertEquals(String.class, newToken.getRefreshToken().getClass());
        assertFalse(newToken.getRefreshToken().isBlank());
    }
}