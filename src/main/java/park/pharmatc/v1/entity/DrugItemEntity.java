package park.pharmatc.v1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "drug_item")
public class DrugItemEntity {

    @Id
    private String itemSeq;

    private String itemName;

    private String ediCode;

    private String formCodeName;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entp_seq")
    private DrugCompanyEntity company;

    @OneToOne(mappedBy = "drugItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private DrugDimensionsEntity dimensions;

    @OneToOne(mappedBy = "drugItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private DrugImagesEntity image;
}
