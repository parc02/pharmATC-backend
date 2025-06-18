package park.pharmatc.v1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import park.pharmatc.v1.entity.DrugItemEntity;

import java.util.List;
import java.util.Optional;

public interface DrugItemRepository extends JpaRepository<DrugItemEntity, Long> {

    @Query("SELECT i.itemSeq FROM DrugItemEntity i")
    List<String> findAllItemSeqs();


    Optional<DrugItemEntity> findByItemSeq(String itemSeq);
}

