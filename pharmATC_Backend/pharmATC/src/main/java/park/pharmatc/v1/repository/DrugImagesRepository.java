package park.pharmatc.v1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import park.pharmatc.v1.entity.DrugImagesEntity;

public interface DrugImagesRepository extends JpaRepository<DrugImagesEntity, Long> {
}
