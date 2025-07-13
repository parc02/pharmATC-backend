package park.pharmatc.v1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import park.pharmatc.v1.entity.DrugItemEntity;

import java.util.List;
import java.util.Optional;

public interface DrugItemRepository extends JpaRepository<DrugItemEntity, Long> {

    // `ediCode`로 검색하여 `company`와 함께 조회
    @Query("SELECT DISTINCT di FROM DrugItemEntity di LEFT JOIN FETCH di.company WHERE di.ediCode = :ediCode")
    List<DrugItemEntity> findByEdiCodeWithCompany(@Param("ediCode") String ediCode);

    // `itemName`으로 검색하여 `company`와 함께 조회
    @Query("SELECT DISTINCT di FROM DrugItemEntity di LEFT JOIN FETCH di.company WHERE di.itemName LIKE %:itemName%")
    List<DrugItemEntity> findByItemNameContainingWithCompany(@Param("itemName") String itemName);

    // `itemSeq`로 검색하여 `company`와 함께 조회
    @Query("SELECT DISTINCT di FROM DrugItemEntity di LEFT JOIN FETCH di.company WHERE di.itemSeq = :itemSeq")
    Optional<DrugItemEntity> findByItemSeqWithCompany(@Param("itemSeq") String itemSeq);

    // `itemSeq`로 검색하여 `dimensions`와 함께 조회
    @Query("SELECT di FROM DrugItemEntity di LEFT JOIN FETCH di.dimensions WHERE di.itemSeq = :itemSeq")
    Optional<DrugItemEntity> findByItemSeqWithDimensions(@Param("itemSeq") String itemSeq);

    // `itemSeq`로 검색하여 `images`와 함께 조회
    @Query("SELECT di FROM DrugItemEntity di LEFT JOIN FETCH di.images WHERE di.itemSeq = :itemSeq")
    Optional<DrugItemEntity> findByItemSeqWithImages(@Param("itemSeq") String itemSeq);

    // `itemSeq`로 단일 항목을 조회
    Optional<DrugItemEntity> findByItemSeq(String itemSeq);

    // `id`로 검색하여 단일 항목을 조회 (Long 사용)
    @Query("SELECT di FROM DrugItemEntity di LEFT JOIN FETCH di.company WHERE di.id = :id")
    Optional<DrugItemEntity> findById(@Param("id") Long id);

    // `id`로 검색하여 `dimensions`와 `images`를 개별적으로 조회 (Long 사용)
    @Query("SELECT di FROM DrugItemEntity di LEFT JOIN FETCH di.dimensions WHERE di.id = :id")
    Optional<DrugItemEntity> findByIdWithDimensions(@Param("id") Long id);

    @Query("SELECT di FROM DrugItemEntity di LEFT JOIN FETCH di.images WHERE di.id = :id")
    Optional<DrugItemEntity> findByIdWithImages(@Param("id") Long id);

    // `itemSeq`와 `id`로 동시에 처리하는 메소드
    @Query("SELECT di FROM DrugItemEntity di LEFT JOIN FETCH di.company WHERE di.itemSeq = :itemSeq OR di.id = :id")
    List<DrugItemEntity> findByItemSeqOrId(@Param("itemSeq") String itemSeq, @Param("id") Long id);
}
