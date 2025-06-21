package park.pharmatc.v1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "drug_images")
@Getter
@Setter
public class DrugImagesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String itemImage;         // 큰제품이미지

    @OneToOne
    @JoinColumn(name = "item_id", referencedColumnName = "id")
    private DrugItemEntity drugItem;  // 약품 연관 관계
}
