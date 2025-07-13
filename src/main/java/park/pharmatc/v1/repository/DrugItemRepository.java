package park.pharmatc.v1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import park.pharmatc.v1.entity.DrugItemEntity;

import java.util.List;
import java.util.Optional;

public interface DrugItemRepository extends JpaRepository<DrugItemEntity, Long> {

    @Query("SELECT DISTINCT di FROM DrugItemEntity di " +
            "LEFT JOIN FETCH di.company " +
            "LEFT JOIN FETCH di.dimensions " +
            "LEFT JOIN FETCH di.images " +
            "WHERE di.ediCode = :ediCode")
    List<DrugItemEntity> findByEdiCodeWithAll(@Param("ediCode") String ediCode);

    @Query("SELECT DISTINCT di FROM DrugItemEntity di " +
            "LEFT JOIN FETCH di.company " +
            "LEFT JOIN FETCH di.dimensions " +
            "LEFT JOIN FETCH di.images " +
            "WHERE di.itemName LIKE %:itemName%")
    List<DrugItemEntity> findByItemNameContainingWithAll(@Param("itemName") String itemName);

    @Query("SELECT DISTINCT di FROM DrugItemEntity di " +
            "LEFT JOIN FETCH di.company " +
            "LEFT JOIN FETCH di.dimensions " +
            "LEFT JOIN FETCH di.images")
    List<DrugItemEntity> findAllWithAssociations();

    Optional<DrugItemEntity> findByItemSeq(String itemSeq);
}
