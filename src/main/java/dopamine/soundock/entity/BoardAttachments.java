package dopamine.soundock.entity;


import dopamine.soundock.enums.FileType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@Getter
@Setter
@Table(name = "board_attachments")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoardAttachments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_attachment_id")
    private Integer boardAttachmentId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @NotNull
    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type",  nullable = false)
    private FileType fileType;

    @NotNull
    @Column(name = "file_key", nullable = false)
    private String fileKey;

    @Column(name = "sequence", nullable = true)
    private Integer sequence;

    @Column(name = "original_filename", nullable = true)
    private String originalFilename;

}
