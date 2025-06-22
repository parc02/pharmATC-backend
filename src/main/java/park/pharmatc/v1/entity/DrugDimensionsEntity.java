package park.pharmatc.v1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "drug_dimensions")
@Getter
@Setter
public class DrugDimensionsEntity {

    @Id
    @Column(name = "item_seq")
    private String itemSeq;

    // 주 테이블의 PK(item_seq)를 공유하는 1:1 관계 설정
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "item_seq") // FK이자 PK
    private DrugItemEntity drugItem;

    @Column(name = "leng_long")
    private Double lengLong;

    @Column(name = "leng_short")
    private Double lengShort;

    @Column(name = "thick")
    private Double thick;
}
