package dopamine.soundock.service;


import dopamine.soundock.dto.response.MyCommentsResponse;
import dopamine.soundock.dto.response.MyPostsResponse;
import dopamine.soundock.entity.Board;
import dopamine.soundock.entity.Comment;
import dopamine.soundock.entity.User;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.repository.BoardRepository;
import dopamine.soundock.repository.CommentRepository;
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
    private final CommentRepository commentRepository;

    // 내가 쓴 게시글 조회
    @Transactional(readOnly = true)
    public Page<MyPostsResponse> getMyPosts(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자 입니다."));

        Page<Board> boards = boardRepository.findByUserAndIsDeletedFalse(user, pageable);

        return boards.map(board ->
                new MyPostsResponse(
                        board.getBoardId(),
                        board.getCategory(),
                        board.getTitle(),
                        board.getCreatedDateTime(),
                        board.getViews(),
                        board.getLikes()
                )
        );
    }

    // 내가 쓴 댓글 조회
    @Transactional(readOnly = true)
    public Page<MyCommentsResponse> getMyComments(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자 입니다."));

        Page<Comment> comments = commentRepository.findByUserAndIsDeletedFalse(user, pageable);

        return comments.map(comment ->
                new MyCommentsResponse(
                        comment.getCommentId(),
                        comment.getCreatedDateTime(),
                        comment.getBoard().getCategory().getCategoryType(),
                        comment.getContent(),
                        comment.getBoard().getBoardId(),
                        comment.getBoard().getTitle(),
                        comment.getLikeCount()
                )
        );
    }

}
