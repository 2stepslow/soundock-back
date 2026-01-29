package dopamine.soundock.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Builder
@Table(name = "verification_tokens")
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_token_id")
    private Integer id;

    // 인증번호
    @NotBlank
    @Column(name = "token", nullable = false, unique = true)
    private String token;

    // 회원 Id
    @NotNull
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    // 만료 시간
    @NotNull
    @Column(name = "expiration_at", nullable = false)
    private LocalDateTime expiryDate;

    @NotNull
    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

    @CreatedDate
    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;
}
