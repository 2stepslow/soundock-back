package dopamine.soundock.service;

import dopamine.soundock.entity.User;
import dopamine.soundock.repository.OauthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OauthService {
    private final OauthRepository oauthRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteConnection(User user) {
        oauthRepository.deleteByUser(user);
    }
}
