package park.pharmatc.v1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import park.pharmatc.v1.entity.DrugDimensionsEntity;

public interface DrugDimensionsRepository extends JpaRepository<DrugDimensionsEntity, Long> {
}
