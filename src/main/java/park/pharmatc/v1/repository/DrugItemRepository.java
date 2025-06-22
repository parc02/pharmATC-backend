package park.pharmatc.v1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import park.pharmatc.v1.entity.DrugItemEntity;

import java.util.List;
import java.util.Optional;

public interface DrugItemRepository extends JpaRepository<DrugItemEntity, String> {

    @Query("SELECT di FROM DrugItemEntity di " +
            "LEFT JOIN FETCH di.company " +
            "LEFT JOIN FETCH di.dimensions " +
            "LEFT JOIN FETCH di.image " +
            "WHERE di.ediCode = :ediCode")
    List<DrugItemEntity> findByEdiCodeWithAll(@Param("ediCode") String ediCode);

    @Query("SELECT di FROM DrugItemEntity di " +
            "LEFT JOIN FETCH di.company " +
            "LEFT JOIN FETCH di.dimensions " +
            "LEFT JOIN FETCH di.image " +
            "WHERE di.itemName LIKE %:itemName%")
    List<DrugItemEntity> findByItemNameContainingWithAll(@Param("itemName") String itemName);

    @Query("SELECT di FROM DrugItemEntity di " +
            "LEFT JOIN FETCH di.company " +
            "LEFT JOIN FETCH di.dimensions " +
            "LEFT JOIN FETCH di.image")
    List<DrugItemEntity> findAllWithAssociations();

    @Query("SELECT di FROM DrugItemEntity di " +
            "LEFT JOIN FETCH di.company " +
            "LEFT JOIN FETCH di.dimensions " +
            "LEFT JOIN FETCH di.image " +
            "WHERE di.itemSeq = :itemSeq")
    Optional<DrugItemEntity> findByItemSeqWithAssociations(@Param("itemSeq") String itemSeq);

    Optional<DrugItemEntity> findByItemSeq(String itemSeq); // 기존 기본 쿼리 (변경 안 해도 됨)
}
