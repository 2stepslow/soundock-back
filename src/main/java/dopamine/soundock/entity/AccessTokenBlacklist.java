package dopamine.soundock.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
@Builder
@Table(name = "access_token_blacklist")
public class AccessTokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "access_token_blacklist_id")
    private int id;

    @Column(name = "access_token", nullable = false)
    private String accessToken;

    @Column(name = "expiration_at",  nullable = false)
    private LocalDateTime expirationAt;
}
