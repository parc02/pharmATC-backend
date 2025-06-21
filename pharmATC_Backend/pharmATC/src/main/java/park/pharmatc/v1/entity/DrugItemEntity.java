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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String itemSeq;           // 품목일련번호

    private String itemName;          // 품목명
    private String formCodeName;      // 제형코드이름
    private String ediCode;           // 보험코드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entp_seq", referencedColumnName = "entpSeq")
    private DrugCompanyEntity company;  // 업체 연관 관계

    @OneToOne(mappedBy = "drugItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private DrugDimensionsEntity dimensions;

    @OneToOne(mappedBy = "drugItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private DrugImagesEntity image;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
