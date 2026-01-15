package dopamine.soundock.service;

import dopamine.soundock.dto.CommentCreateRequest;
import dopamine.soundock.dto.CommentResponse;
import dopamine.soundock.entity.Board;
import dopamine.soundock.entity.Comment;
import dopamine.soundock.entity.User;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.repository.BoardRepository;
import dopamine.soundock.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    // 댓글 작성
    public CommentResponse createComment(
            Integer boardId,
            CommentCreateRequest createRequest
    ){
        // 로그인한 유저인지 검증
        // boardId에 해당하는 게시글 있는지 확인
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 게시글입니다."));
        // 삭제된 게시글인지 확인
        if(board.getDeletedDateTime() != null){
            throw new IllegalArgumentException("삭제된 게시글입니다.");
        }
        // 댓글 작성 시도자가 로그인 상태인지 확인
        // 댓글 작성
        Comment comment = Comment.builder()
                .board(board)
                // .user(user)
                .content(createRequest.getContent())
                .build();
        // 댓글 저장
        commentRepository.save(comment);
        // comment를 CommentResponse dto에 실어서 보내주기
        return CommentResponse.from(comment);
    }
}
