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

    @OneToOne
    @JoinColumn(name = "item_id")
    private DrugItemEntity drugItem;

    @Column(name = "leng_long")
    private Double lengLong;

    @Column(name = "leng_short")
    private Double lengShort;

    @Column(name = "thick")
    private Double thick;
}
