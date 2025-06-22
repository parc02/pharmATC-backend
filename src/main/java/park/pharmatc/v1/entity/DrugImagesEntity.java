package park.pharmatc.v1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "drug_images")
public class DrugImagesEntity {

    @Id
    private String itemSeq;

    private String itemImage;

    @OneToOne
    @MapsId
    @JoinColumn(name = "item_seq")
    private DrugItemEntity drugItem;
}
