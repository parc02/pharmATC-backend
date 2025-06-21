package park.pharmatc.v1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import park.pharmatc.v1.entity.DrugItemEntity;

import java.util.List;
import java.util.Optional;

public interface DrugItemRepository extends JpaRepository<DrugItemEntity, Long> {

    Optional<DrugItemEntity> findByItemSeq(String itemSeq);

    @Query("SELECT d.itemSeq FROM DrugItemEntity d")
    List<String> findAllItemSeqs();

    @Query("SELECT i FROM DrugItemEntity i " +
            "LEFT JOIN FETCH i.company " +
            "LEFT JOIN FETCH i.image " +
            "LEFT JOIN FETCH i.dimensions")
    List<DrugItemEntity> findAllWithAssociations();
}
