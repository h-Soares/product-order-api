package com.soaresdev.productorderapi.repositories;

import com.soaresdev.productorderapi.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByName(String name);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "DELETE FROM tb_product_category " +  "WHERE category_id IN (SELECT id FROM tb_category WHERE id = :uuid); " +
                    "DELETE FROM tb_category WHERE id = :uuid")
    void deleteByUUID(UUID uuid);
}