package dopamine.soundock.service;


import dopamine.soundock.dto.response.MyPostsResponse;
import dopamine.soundock.entity.Board;
import dopamine.soundock.entity.User;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.repository.BoardRepository;
import dopamine.soundock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MyActivityService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

@Transactional(readOnly = true)
public Page<MyPostsResponse> getMyPosts(Pageable pageable) {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자 입니다."));

    Page<Board> boards = boardRepository.findByUserAndIsDeletedFalse(user, pageable);

    Page<MyPostsResponse> myPostsResponses = boards.map(board ->
            new MyPostsResponse(
                    board.getBoardId(),
                    board.getCategory(),
                    board.getTitle(),
                    board.getCreatedDateTime(),
                    board.getViews(),
                    board.getLikes()
                    )
    );
    return myPostsResponses;
    }

}
