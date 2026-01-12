package dopamine.soundock.entity;

import dopamine.soundock.enums.Section;
import dopamine.soundock.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "category")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "parent_id")
    private Integer parentId;

    @Column(name = "section", nullable = false)
    @Enumerated(EnumType.STRING)
    private Section section;

    @Column(name = "category_type", nullable = false)
    private String categoryType;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "write_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

}
