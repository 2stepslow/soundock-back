package dopamine.soundock.entity;

import dopamine.soundock.enums.UserRole;
import dopamine.soundock.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    @NotBlank
    @Email
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank
    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @NotBlank
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Min(0)
    @Column(name = "points", nullable = false)
    private int points;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status",  nullable = false)
    private UserStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createDatetime;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Min(0)
    @Column(name = "pop_balance", nullable = false)
    private Integer popBalance;

    // 재화 변동액 처리
    // 재화 충전
    public void increasePopBalance(int amount){
        if (this.popBalance == null){
            this.popBalance = 0;
        }
        if (amount < 0){
            throw new IllegalArgumentException("잘못된 요청값 입니다.");
        }
        this.popBalance += amount;

    }

    // 결제 취소, 재화 사용
    public void decreasePopBalance(int amount){
        // change Amount가 +-로 들어올거임
        if (this.popBalance == null){
            popBalance = 0;
        }
        this.popBalance -= amount;

        if (this.popBalance < 0) {
            throw new IllegalArgumentException("잘못된 요청값 입니다.");
        }
    }
}
