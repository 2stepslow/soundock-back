package dopamine.soundock.service;

import dopamine.soundock.dto.request.CommentCreateRequest;
import dopamine.soundock.dto.response.CommentResponse;
import dopamine.soundock.entity.Board;
import dopamine.soundock.entity.Comment;
import dopamine.soundock.entity.User;
import dopamine.soundock.enums.CategoryType;
import dopamine.soundock.exceptions.AuthRejectedException;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.repository.BoardRepository;
import dopamine.soundock.repository.CommentRepository;
import dopamine.soundock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardService boardService;

    // 댓글 작성
    public CommentResponse createComment(
            CategoryType categoryType,
            Integer boardId,
            CommentCreateRequest createRequest
    ){
        // 로그인한 유저인지 검증(유저 이렇게 넣은건 테스트용)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

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
    public void deleteComment(CategoryType categoryType, Integer boardId, Integer commentId){
        Board board = boardService.getValidatedBoard(boardId, categoryType);

        // 로그인한 유저인지 검증(유저 이렇게 넣은건 테스트용)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // 삭제하려는 commentId에 해당하는 댓글이 있는지
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 댓글입니다."));

        // 이미 삭제된 댓글인지 확인
        if (comment.isDeleted()){
            throw new ResourceNotFoundException("이미 삭제된 댓글입니다.");
        }
        if (!comment.getBoard().getBoardId().equals(board.getBoardId())){
            throw new IllegalArgumentException("해당 게시글의 댓글이 아닙니다.");
        }
        if (!comment.getUser().getId().equals(user.getId())){
            throw new AuthRejectedException("댓글 작성자의 정보와 일치하지 않습니다.");
        }
        // soft delete 실시
        comment.setDeleted(true);
        commentRepository.save(comment);

    }
    // 댓글 조회
    public List<CommentResponse> getComment(CategoryType categoryType, Integer boardId){
        // 카테고리와 boardId에 해당하는 삭제되지 않은 게시글인지 확인
        Board board = boardService.getValidatedBoard(boardId, categoryType);

        // commentRepo에서 해당 boardId에 작성된 댓글이 있는지 확인
        List<Comment> results = commentRepository.findByBoardBoardId(board.getBoardId());
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
