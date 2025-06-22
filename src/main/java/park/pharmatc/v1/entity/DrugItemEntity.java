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
    @Column(name = "item_seq")
    private String itemSeq;

    private String itemName;

    private String ediCode;

    private String formCodeName;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_seq", referencedColumnName = "item_seq")
    private DrugDimensionsEntity dimensions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entp_seq")
    private DrugCompanyEntity company;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_seq", referencedColumnName = "item_seq")
    private DrugImagesEntity image;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // ✅ 이게 없어서 컴파일 실패 중!
}
