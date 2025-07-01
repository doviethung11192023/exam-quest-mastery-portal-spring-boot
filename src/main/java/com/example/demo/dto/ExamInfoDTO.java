package com.example.demo.dto;

import java.util.Date;
import lombok.Data;

@Data
public class ExamInfoDTO {
    private String maLop;
    private String maMH;
    private int lan;
    private String trinhDo;
    private int soCauThi;
    private int thoiGianThi;
    private Date ngayThi;
    private boolean daThiRoi;
}