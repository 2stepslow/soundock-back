package dopamine.soundock.service;


import dopamine.soundock.enums.CategoryType;
import dopamine.soundock.exceptions.InvalidFileException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class Validatorservice {

    private final S3Service s3Service;

    public void validateFilesForCategory(CategoryType categoryType, List<MultipartFile> files, String youtubeUrl) {
        switch (categoryType) {
            case SHOWCASE:
                validateShowcase(youtubeUrl);
                break;
            case SPOTLIGHT:
                validateSpotlight(files);
                break;
            case COMMUNITY:
            case REVIEWS:
                validateSingleFile(files);
                break;
            case NOTICE:
                validateNotice(files);
                break;
            case PLAYLISTS:
                break;
            default:
                throw new InvalidFileException("지원하지 않는 카테고리입니다.");
        }
    }

    // SHOWCASE 검증 (YouTube URL만 검증)
    private void validateShowcase(String youtubeUrl) {
        if (!hasYoutubeUrl(youtubeUrl)) {
            throw new InvalidFileException("SHOWCASE는 유튜브 링크가 필요합니다.");
        }
        if (!isValidYoutubeUrl(youtubeUrl)) {
            throw new InvalidFileException("유효하지 않은 유튜브 URL입니다.");
        }
    }

    // SPOTLIGHT 검증 (이미지 다중 업로드 필수, 1~5개)
    private void validateSpotlight(List<MultipartFile> files) {
        List<MultipartFile> validFiles = getValidFiles(files);

        if (validFiles.isEmpty()) {
            throw new InvalidFileException("SPOTLIGHT는 최소 1개의 이미지가 필요합니다.");
        }
        if (validFiles.size() > 5) {
            throw new InvalidFileException("SPOTLIGHT는 최대 5개의 이미지만 업로드 가능합니다.");
        }

        validateAllImagesOnly(validFiles, "SPOTLIGHT");
    }

    // COMMUNITY, REVIEWS
    private void validateSingleFile(List<MultipartFile> files) {
        List<MultipartFile> validFiles = getValidFiles(files);
        if (validFiles.size() > 1) {
            throw new InvalidFileException("해당 게시판은 파일 1개만 업로드 가능합니다.");
        }
    }

    // NOTICE 검증 (파일 다중 업로드 가능)
    private void validateNotice(List<MultipartFile> files) {
        // 다중 업로드 가능, 추가 검증 없음
    }



    // === 헬퍼 메서드 ===
    // 유튜브 URL이 있는지 확인
    private boolean hasYoutubeUrl(String youtubeUrl) {
        return youtubeUrl != null && !youtubeUrl.trim().isEmpty();
    }

    private static final Pattern YOUTUBE_PATTERN = Pattern.compile(
            "(?:https?://)?(?:www\\.)?(?:youtube\\.com/watch\\?v=|youtu\\.be/)([a-zA-Z0-9_-]{11})"
    );

    public boolean isValidYoutubeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        Matcher matcher = YOUTUBE_PATTERN.matcher(url);
        return matcher.find();
    }

    // 비어있지 않은 파일들만 필터링
    private List<MultipartFile> getValidFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        return files.stream()
                .filter(f -> !f.isEmpty())
                .toList();
    }

    // 모든 파일이 이미지인지 검증
    private void validateAllImagesOnly(List<MultipartFile> files, String categoryName) {
        for (MultipartFile file : files) {
            if (!s3Service.isImageFile(file.getOriginalFilename())) {
                throw new InvalidFileException(categoryName + "는 이미지 파일만 업로드 가능합니다.");
            }
        }
    }
}
