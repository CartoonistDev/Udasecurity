import image.service.FakeImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import security.application.StatusListener;
import security.data.SecurityRepository;
import security.service.SecurityService;

import java.util.HashSet;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {
    SecurityService securityService;

    @Mock
    SecurityRepository securityRepository;

    @Mock
    FakeImageService fakeImageService;

    @Mock
    Set<StatusListener> statusListeners;


    @BeforeEach
    void init(){
        securityService = new SecurityService(securityRepository, fakeImageService);
    }
}
