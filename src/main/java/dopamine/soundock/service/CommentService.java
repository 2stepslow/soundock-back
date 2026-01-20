package dopamine.soundock.service;

import dopamine.soundock.dto.CommentCreateRequest;
import dopamine.soundock.dto.CommentResponse;
import dopamine.soundock.entity.Board;
import dopamine.soundock.entity.Comment;
import dopamine.soundock.entity.User;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.repository.BoardRepository;
import dopamine.soundock.repository.CommentRepository;
import dopamine.soundock.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    // 댓글 작성
    public CommentResponse createComment(
            Integer boardId,
            CommentCreateRequest createRequest
    ){
        // 로그인한 유저인지 검증(유저 이렇게 넣은건 테스트용)
        User user = userRepository.findById(1)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        // boardId에 해당하는 게시글 있는지 확인
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 게시글입니다."));
        // 삭제된 게시글인지 확인
        if(board.getDeletedDateTime() != null){
            throw new ResourceNotFoundException("삭제된 게시글입니다.");
        }
        // 댓글 작성 시도자가 로그인 상태인지 확인
        // 댓글 작성
        Comment comment = Comment.builder()
                .board(board)
                .user(user)
                .content(createRequest.getContent())
                .build();
        // 댓글 저장
        commentRepository.save(comment);
        // comment를 CommentResponse dto에 실어서 보내주기
        return CommentResponse.from(comment);
    }
    // 댓글 삭제
    public void deleteComment(Integer commentId){
        // 댓글 작성자와 현재 삭제 시도 이용자가 일치하는지
        // 삭제하려는 commentId에 해당하는 댓글이 있는지
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 댓글입니다."));
        // 이미 삭제된 댓글인지 확인
        if (comment.isDeleted()){
            throw new ResourceNotFoundException("이미 삭제된 댓글입니다.");
        }
        // 부모 댓글인지 확인 -> commentId랑 parent_comment_id랑 같으면 삭제하도록?

        // soft delete 실시
        comment.setDeleted(true);
        commentRepository.save(comment);

    }
    // 댓글 조회???
    public List<CommentResponse> getComment(Integer boardId){
        // 조회하려는 boardId에 해당하는 게시글이 존재하는지 확인
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 게시글입니다."));

        // commentRepo에서 해당 boardId에 작성된 댓글이 있는지 확인
        List<Comment> results = commentRepository.findByBoardBoardId(boardId);
        if (results.isEmpty()){
            throw new ResourceNotFoundException("작성된 댓글이 없습니다.");
        }

        List<CommentResponse> responses = new ArrayList<>();

        // 댓글을 commentResponse로 전환해서 보내줌
        for (Comment comment : results){
            CommentResponse commentResponse = CommentResponse.from(comment);
            responses.add(commentResponse);
        }
        return responses;
    }
}
