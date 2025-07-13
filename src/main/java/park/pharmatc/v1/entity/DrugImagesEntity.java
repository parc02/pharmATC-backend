package park.pharmatc.v1.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "drug_images")
public class DrugImagesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_seq", nullable = false)
    private String itemSeq;

    @Column(name = "item_image")
    private String itemImage;

    @Column(name = "leng_long")
    private Double lengLong;

    @Column(name = "leng_short")
    private Double lengShort;

    @Column(name = "thick")
    private Double thick;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drug_item_id")
    private DrugItemEntity drugItem;
}
