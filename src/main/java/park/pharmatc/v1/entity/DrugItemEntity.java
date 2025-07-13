package park.pharmatc.v1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "drug_item")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrugItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_seq", nullable = false)
    private String itemSeq;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "edi_code")
    private String ediCode;

    @Column(name = "form_code_name")
    private String formCodeName;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entp_seq")
    private DrugCompanyEntity company;

    @OneToMany(mappedBy = "drugItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DrugDimensionsEntity> dimensions;

    @OneToMany(mappedBy = "drugItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DrugImagesEntity> images;
}
