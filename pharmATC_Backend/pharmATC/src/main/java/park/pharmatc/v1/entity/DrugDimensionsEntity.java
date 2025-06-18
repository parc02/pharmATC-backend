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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double lengLong;          // 크기 장축
    private Double lengShort;         // 크기 단축
    private Double thick;             // 크기 두께

    @OneToOne
    @JoinColumn(name = "item_id", referencedColumnName = "id")
    private DrugItemEntity drugItem;  // 약품 연관 관계
}
