package park.pharmatc.v1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "drug_dimensions")
public class DrugDimensionsEntity {

    @Id
    private String itemSeq;

    private Double lengLong;
    private Double lengShort;
    private Double thick;

    @OneToOne
    @MapsId
    @JoinColumn(name = "item_seq")
    private DrugItemEntity drugItem;
}
