package dopamine.soundock.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "user_grades")
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserGrade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer gradeId;

    @Column(name = "grade", nullable = false)
    private String grade;

    @Column(name = "activity_score", nullable = false)
    private int activityScore;
}
