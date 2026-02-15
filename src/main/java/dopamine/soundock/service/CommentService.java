package dopamine.soundock.service;

import dopamine.soundock.dto.request.CommentCreateRequest;
import dopamine.soundock.dto.response.CommentListResponse;
import dopamine.soundock.dto.response.CommentResponse;
import dopamine.soundock.entity.Board;
import dopamine.soundock.entity.Comment;
import dopamine.soundock.entity.CommentLike;
import dopamine.soundock.entity.User;
import dopamine.soundock.enums.NotificationType;
import dopamine.soundock.exceptions.AuthRejectedException;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.repository.BoardRepository;
import dopamine.soundock.repository.CommentLikeRepository;
import dopamine.soundock.repository.CommentRepository;
import dopamine.soundock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final NotificationService notificationService;

    // 댓글 작성
    @Transactional
    public CommentResponse createComment(
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
                .createdDateTime(LocalDateTime.now())
                .build();
        // 댓글 저장
        commentRepository.save(comment);

        // 본인의 게시글이 아닐 때만 알림 생성
        if (!board.getUser().getId().equals(user.getId())) {
            notificationService.createNotification(
                    board.getUser(),
                    user,
                    NotificationType.COMMENT,
                    "님이 회원님의 게시글에 댓글을 남겼습니다",
                    board
            );
        }


        // 게시글에 달린 댓글 수 조회
        Integer countComment = commentRepository.countByIsDeletedIsFalseAndBoard(board);

        return CommentResponse.of(comment, countComment);
    }

    // 댓글 삭제
    @Transactional
    public Integer deleteComment(Integer boardId, Integer commentId){
        Board board = boardRepository.findByBoardIdAndDeletedDateTimeIsNull(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 카테고리에서 게시글을 찾을 수 없거나 삭제된 게시글입니다."));

        // 로그인한 유저인지 검증
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // 삭제하려는 commentId에 해당하는 댓글이 있는지
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 댓글입니다."));

        // 댓글 삭제 하려는 id가 해당 게시글에 작성된 것이 맞는지 확인
        if(!comment.getBoard().getBoardId().equals(boardId)){
            throw new IllegalArgumentException("해당 게시글에 작성된 댓글이 아닙니다.");
        }

        // 이미 삭제된 댓글인지 확인
        if (comment.isDeleted()){
            throw new ResourceNotFoundException("이미 삭제된 댓글입니다.");
        }
        if (!comment.getUser().getId().equals(user.getId())){
            throw new AuthRejectedException("댓글 작성자의 정보와 일치하지 않습니다.");
        }
        // soft delete 실시
        comment.setDeleted(true);
        commentRepository.save(comment);

        return commentRepository.countByIsDeletedIsFalseAndBoard(board);
    }

    // 댓글 조회
    @Transactional(readOnly = true)
    public CommentListResponse getComment(Integer boardId){
        // 카테고리와 boardId에 해당하는 삭제되지 않은 게시글인지 확인
        Board board = boardRepository.findByBoardIdAndDeletedDateTimeIsNull(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 카테고리에서 게시글을 찾을 수 없거나 삭제된 게시글입니다."));

        // commentRepo에서 해당 boardId에 작성된 댓글이 있는지 확인
        List<Comment> results = commentRepository.findByIsDeletedIsFalseAndBoard(board);
        if (results.isEmpty()){
            return CommentListResponse.builder()
                    .commentResponse(new ArrayList<>())
                    .countComment(0)
                    .build();
        }
        Integer countComment = results.size();

        List<CommentResponse> responses = new ArrayList<>();

        // 로그인한 유저 여부 확인
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());

        Set<Integer> toggledLikeIds = new HashSet<>();

        // 로그인한 유저는 자신의 좋아요 여부 표시
        if (isLoggedIn){
            String email = auth.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

            // 유저가 좋아요한 댓글들 찾기
            List<CommentLike> likes = commentLikeRepository.findAllByUserAndCommentIn(user, results);

            // 유저가 좋아요한 댓글들의 Id만 추출해 Set으로 만듦
            toggledLikeIds = likes.stream()
                    .map(like -> like.getComment().getCommentId())
                    .collect(Collectors.toSet());

            // 댓글을 commentResponse로 전환해서 보내줌
            for (Comment comment : results){
                boolean toggledLike = toggledLikeIds.contains(comment.getCommentId());
                CommentResponse commentResponse = CommentResponse.fromForLoginUser(comment, toggledLike);
                responses.add(commentResponse);
            }
        } else {
            for (Comment comment : results) {
                CommentResponse commentResponse = CommentResponse.from(comment);
                responses.add(commentResponse);
            }
        }

        return CommentListResponse.builder()
                .commentResponse(responses)
                .countComment(countComment)
                .build();
    }

    // 댓글 수정
    @Transactional
    public CommentResponse updateComment(Integer commentId, CommentCreateRequest updateRequest){
        // 로그인한 유저인지 검증
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // commentId의 해당하는 댓글 존재 여부 확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 댓글을 찾을 수 없습니다."));

        if (comment.isDeleted()){
            throw new IllegalArgumentException("이미 삭제된 댓글입니다.");
        }

        // 댓글 작성자와 로그인 유저 일치하는지 확인
        if (!user.getId().equals(comment.getUser().getId())){
            throw new AuthRejectedException("해당 댓글 작성자와 일치하지 않아 수정이 불가합니다.");
        }

        if (updateRequest.getContent() != null){
            comment.updateComment(updateRequest);
        }
        commentRepository.save(comment);
        log.info("해당 댓글이 수정되었습니다. 댓글 id : {}", commentId);
        return CommentResponse.from(comment);
    }

    // 댓글 좋아요
    @Transactional
    public CommentResponse recommendComment(Integer commentId){
        // 로그인한 유저인지 검증
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // comment Id와 일치하는 댓글 존재하는지 검증
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("추천하려는 댓글을 찾을 수 없습니다."));

        // 삭제된 댓글인지 검증
        if (comment.isDeleted()){
            throw new IllegalArgumentException("삭제된 댓글입니다. 추천을 할 수 없습니다.");
        }

        // user Id와 comment Id가 테이블에 정보가 있는지 확인(이미 추천 눌렀으면 테이블에서 삭제)
       boolean toggledLike = commentLikeRepository.existsByCommentAndUser(comment, user);

        // toggledComment == false면 좋아요-유저 테이블 기록, 좋아요 +1 카운트
        if (!toggledLike){
            CommentLike commentLike = CommentLike.builder()
                    .user(user)
                    .comment(comment)
                    .build();
            commentLikeRepository.save(commentLike);
            comment.increaseLike();
        } else {
            // toggledComment == true면 좋아요-유저 테이블 기록 삭제, 좋아요 -1 카운트
            commentLikeRepository.deleteByCommentAndUser(comment, user);
            comment.decreaseLike();
        }

        return CommentResponse.builder()
                .likeCount(comment.getLikeCount())
                .toggledLike(!toggledLike)
                .build();
    }
}
