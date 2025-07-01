package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "Giaovien")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Giaovien {

        @Id
        @Column(name = "MAGV", length = 8)
        private String magv;

        @Column(name = "HO", length = 40)
        private String ho;

        @Column(name = "TEN", length = 10)
        private String ten;

        @Column(name = "TRANG_THAI")
        private Boolean trangThai;

        @Column(name = "HAS_ACCOUNT")
        private Boolean hasAccount;

        @Transient
        private String hoTen;

        @Column(name = "SODTLL", length = 15)
        private String soDienThoai;

        @Column(name = "DIACHI", length = 50)
        private String diaChi;

        // Relationships
        @OneToMany(mappedBy = "giaovien", cascade = CascadeType.ALL)
        private List<Bode> danhSachBoDe;

        @OneToMany(mappedBy = "giaovien", cascade = CascadeType.ALL)
        private List<GiaovienDangky> danhSachDangKy;

        // Helper method to get full name
        public String getHoTen() {
                return (ho != null ? ho + " " : "") + (ten != null ? ten : "");
        }

        //Helper method to safely get hasAccount status
        public Boolean getHasAccount() {
                return hasAccount != null ? hasAccount : Boolean.FALSE;
        }
}