package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.BangDiem;
import com.example.demo.entity.BangDiemId;

public interface BackupRepository extends JpaRepository<BangDiem, BangDiemId> {
    @Procedure(name = "sp_BackupDatabase")
    String sp_BackupDatabase(@Param("DatabaseName") String dbName,
                             @Param("BackupFolder") String folder);
}
