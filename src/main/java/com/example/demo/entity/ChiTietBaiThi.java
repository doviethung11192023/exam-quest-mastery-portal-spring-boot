package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ChiTietBaiThi")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietBaiThi {

    @EmbeddedId
    private ChiTietBaiThiId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "MASV", referencedColumnName = "MASV", insertable = false, updatable = false),
            @JoinColumn(name = "MAMH", referencedColumnName = "MAMH", insertable = false, updatable = false),
            @JoinColumn(name = "LAN", referencedColumnName = "LAN", insertable = false, updatable = false)
    })
    private BangDiem bangDiem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "MAMH", referencedColumnName = "MAMH", insertable = false, updatable = false),
            @JoinColumn(name = "CAUHOI", referencedColumnName = "CAUHOI", insertable = false, updatable = false)
    })
    private Bode bode;

    @Column(name = "TRALOI", length = 1)
    private String traLoi;

    @PrePersist
    @PreUpdate
    private void validateTraLoi() {
        // Allow empty string or null values
        if (traLoi == null || traLoi.isEmpty()) {
            return;
        }

        // For non-empty values, validate that they are A, B, C, or D
        if (!traLoi.matches("^[A-D]$")) {
            throw new IllegalArgumentException("TRALOI must be A, B, C or D");
        }
    }
}