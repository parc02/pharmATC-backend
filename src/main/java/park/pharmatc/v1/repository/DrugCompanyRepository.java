package park.pharmatc.v1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import park.pharmatc.v1.entity.DrugCompanyEntity;

public interface DrugCompanyRepository extends JpaRepository<DrugCompanyEntity, String> {
    // entpSeq가 PK이므로 기본 CRUD 자동 제공됨
}
