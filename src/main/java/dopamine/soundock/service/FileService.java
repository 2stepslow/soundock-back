package dopamine.soundock.service;

import dopamine.soundock.exceptions.CustomException;
import dopamine.soundock.global.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    // AWS S3 사용시 주석 해제
    // private final AmazonS3 s3Client;

    // AWS S3 사용시 주석 해제
    // @Value("${cloud.aws.s3.bucket}")
    // private String bucket;



    /**
     * 로컬 스토리지 업로드 메서드
     */
    public String uploadToLocal(MultipartFile file) {
        try {
            // 저장 디렉터리 생성
            Path root =  Paths.get(AppConstants.File.FILE_UPLOAD_PATH_INQUIRY);
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            // 고유 파일명 생성 (UUID + 확장자)
            // 원본 파일명을 그대로 사용하면 경로 조작 공격 위험이 있으니 파일명도 변경
            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String saveName = UUID.randomUUID() + "_" + extension;

            // 파일 물리 저장
            Path targetPath = root.resolve(saveName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // DB에 저장할 접근 경로
            return AppConstants.File.FILE_DB_URL_INQUIRY + "/" + saveName;

        } catch (IOException e) {
            throw new CustomException("로컬 저장 실패", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("파일 업로드 실패: {}", file.getOriginalFilename(), e);
            throw new CustomException("파일 업로드 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * AWS S3 업로드 메서드 (아직 연결 안해서 사용은 안함)
     */
//    public String uploadToS3(MultipartFile file) {
//        // 저장할 파일명 생성 (UUID 활용)
//        String s3FileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//
//        // S3 전송을 위한 메타 데이터 설정
//        ObjectMetadata metadata = new ObjectMetadata();
//        metadata.setContentLength(file.getSize());
//        metadata.setContentType(file.getContentType());
//
//        try {
//            // S3로 파일 스트림 전송
//            s3Client.putObject(new PutObjectRequest(bucket, s3FileName, file.getInputStream(), metadata)
//                    .withCannedAcl(CannedAccessControlList.PublicRead)); // 외부에 공개할 경우 추가
//
//            // 업로드된 파일의 URL 반환
//            return s3Client.getUrl(bucket, s3FileName).toString();
//        } catch (IOException e) {
//            throw new CustomException("S3 파일 업로드 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            throw new CustomException("알 수 없는 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    /**
     * 파일 용량 체크 및 파일명 체크 메서드
     */
    public void validateFile(MultipartFile file) {
        // 용량 체크 : 20MB
        if (file.getSize() > AppConstants.Validation.MAX_FILE_SIZE) throw new IllegalArgumentException("20MB 초과");

        // 파일명 체크 : 공백/특수문자
        String name = file.getOriginalFilename();
        if (name == null || name.contains(" ") || !name.matches(AppConstants.ValidationPattern.FILE_NAME_REGEX)) {
            throw new IllegalArgumentException("파일명에 공백 및 특수문제를 빼주세요 (점(.), 언더바(_), 하이픈(-)만 허용)");
        }
    }
}
