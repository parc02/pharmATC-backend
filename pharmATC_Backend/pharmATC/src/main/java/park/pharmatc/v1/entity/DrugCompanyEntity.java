package park.pharmatc.v1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "drug_company")
@Getter
@Setter
public class DrugCompanyEntity {

    @Id
    private String entpSeq;           // 업체일련번호 (PK)

    private String entpName;          // 업체명

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<DrugItemEntity> drugItems;
}
