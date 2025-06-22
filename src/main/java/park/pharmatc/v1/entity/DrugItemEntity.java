package park.pharmatc.v1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "drug_item")
@Getter
@Setter
public class DrugItemEntity {

    @Id
    @Column(name = "item_seq", nullable = false)
    private String itemSeq; // 약품 고유코드 (PK)

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "edi_code")
    private String ediCode;

    @Column(name = "form_code_name")
    private String formCodeName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entp_seq")
    private DrugCompanyEntity company;

    @OneToOne(mappedBy = "drugItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private DrugDimensionsEntity dimensions;

    @OneToOne(mappedBy = "drugItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private DrugImagesEntity image;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
