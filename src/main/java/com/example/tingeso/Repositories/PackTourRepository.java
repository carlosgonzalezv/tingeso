package com.example.tingeso.Repositories;

import com.example.tingeso.Entities.PackTourEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackTourRepository extends JpaRepository<PackTourEntity, Long> {

    @Query("SELECT p FROM PackTourEntity p WHERE p.status != 'INACTIVO'")
    List<PackTourEntity> findAllActiveTours();

}
