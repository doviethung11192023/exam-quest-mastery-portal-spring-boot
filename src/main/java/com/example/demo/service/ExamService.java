package com.example.demo.service;

import com.example.demo.dto.ExamInfoDTO;
import com.example.demo.dto.QuestionDTO;
import com.example.demo.dto.ExamSubmissionDTO;
import com.example.demo.dto.ExamResultDTO;
import com.example.demo.entity.BangDiem;
import com.example.demo.entity.BangDiemId;
//import com.example.demo.entity.CT_Bangdiem;
import com.example.demo.entity.Bode;
import com.example.demo.entity.BodeId;
import com.example.demo.entity.ChiTietBaiThi;
import com.example.demo.entity.ChiTietBaiThiId;
import com.example.demo.entity.GiaovienDangky;
import com.example.demo.entity.Monhoc;
import com.example.demo.entity.Sinhvien;
import com.example.demo.repository.BangDiemRepository;
//import com.example.demo.repository.CTBangdiemRepository;
import com.example.demo.repository.BodeRepository;
import com.example.demo.repository.ChiTietBaiThiRepository;
import com.example.demo.repository.GiaovienDangkyRepository;
import com.example.demo.repository.MonhocRepository;
import com.example.demo.repository.SinhvienRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamService {

    @Autowired
    private GiaovienDangkyRepository giaovienDangkyRepository;

    @Autowired
    private BodeRepository bodeRepository;

    @Autowired
    private SinhvienRepository sinhvienRepository;

    @Autowired
    private BangDiemRepository bangdiemRepository;

    @Autowired
    private MonhocRepository monhocRepository;

    @Autowired
    private ChiTietBaiThiRepository chiTietBaiThiRepository;
    //@Autowired
    // private CTBangdiemRepository ctBangdiemRepository;

    /**
     * Lấy thông tin về bài thi
     */
    public ExamInfoDTO getExamInfo(String maSV, String maLop, String maMH, int lan) {
        // Kiểm tra xem sinh viên có thuộc lớp không
        Sinhvien sinhvien = sinhvienRepository.findById(maSV)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên với mã " + maSV));

        if (!sinhvien.getLop().getMaLop().equals(maLop)) {
            throw new RuntimeException("Sinh viên không thuộc lớp này");
        }

        // Kiểm tra xem có lịch thi cho môn học và lớp này không
        GiaovienDangky dangky = giaovienDangkyRepository.findByMalopAndMamhAndLan(maLop, maMH, (short) lan)
                .orElseThrow(() -> new RuntimeException(
                        "Không có lịch thi cho lớp " + maLop + ", môn " + maMH + ", lần " + lan));

        // Kiểm tra xem sinh viên đã thi lần này chưa
        boolean daThiRoi = bangdiemRepository.findBySinhvienAndMamhAndLan(sinhvien, maMH, lan).isPresent();

        ExamInfoDTO examInfo = new ExamInfoDTO();
        examInfo.setMaLop(maLop);
        examInfo.setMaMH(maMH);
        examInfo.setLan(lan);
        examInfo.setTrinhDo(dangky.getTrinhDo());
        examInfo.setSoCauThi(dangky.getSoCauThi());
        examInfo.setThoiGianThi(dangky.getThoiGian());
        examInfo.setNgayThi(java.sql.Timestamp.valueOf(dangky.getNgayThi()));
        examInfo.setDaThiRoi(daThiRoi);

        return examInfo;
    }

    /**
     * Lấy câu hỏi cho bài thi
     * Yêu cầu:
     * - Các câu hỏi không được trùng nhau
     * - 70% số câu hỏi là trình độ đã chọn, 30% số câu hỏi có thể là trình độ thấp
     * hơn
     */
    public List<QuestionDTO> getQuestionsForExam(String maLop, String maMH, int lan, String maSV) {
        // Kiểm tra xem sinh viên đã thi lần này chưa
        Sinhvien sinhvien = sinhvienRepository.findById(maSV)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên với mã " + maSV));

        boolean daThiRoi = bangdiemRepository.findBySinhvienAndMamhAndLan(sinhvien, maMH, lan).isPresent();
        if (daThiRoi) {
            throw new RuntimeException("Sinh viên đã thi lần này rồi");
        }

        // Lấy thông tin lịch thi
        GiaovienDangky dangky = giaovienDangkyRepository.findByMalopAndMamhAndLan(maLop, maMH, (short)lan)
                .orElseThrow(() -> new RuntimeException(
                        "Không có lịch thi cho lớp " + maLop + ", môn " + maMH + ", lần " + lan));

        String trinhDo = dangky.getTrinhDo();
        int soCauThi = dangky.getSoCauThi();
        System.out.println("Trình độ: " + trinhDo);
        System.out.println("Số câu hỏi: " + soCauThi);
        int availableMainQuestions = bodeRepository.countQuestionsBySubjectAndLevel(maMH, trinhDo);

        List<Bode> questions = new ArrayList<>();

        // Nếu trình độ chính đủ câu hỏi
        if (availableMainQuestions >= soCauThi) {
            questions = bodeRepository.findRandomQuestionsForExam(maMH, trinhDo, soCauThi);
        } else {
            // Nếu không đủ, cần lấy thêm từ trình độ thấp hơn
            String lowerLevel = getLowerLevel(trinhDo);

            // Nếu không có trình độ thấp hơn hoặc là trình độ C
            if (lowerLevel == null || "C".equals(trinhDo)) {
                questions = bodeRepository.findRandomQuestionsForExam(maMH, trinhDo, soCauThi);
            } else {
                // Tính toán số câu hỏi cần từ mỗi trình độ
                int mainLevelQuestions = availableMainQuestions; // Lấy tất cả câu hỏi có sẵn ở trình độ chính
                int lowerLevelQuestions = soCauThi - mainLevelQuestions; // Số câu hỏi cần lấy thêm từ trình độ thấp hơn

                // Lấy câu hỏi từ trình độ chính
                List<Bode> mainLevelQs = bodeRepository.findRandomQuestionsForExam(maMH, trinhDo, mainLevelQuestions);
                questions.addAll(mainLevelQs);

                // Lấy câu hỏi từ trình độ thấp hơn
                List<Bode> lowerLevelQs = bodeRepository.findRandomQuestionsForExam(maMH, lowerLevel,
                        lowerLevelQuestions);
                questions.addAll(lowerLevelQs);
            }
        }

        // Kiểm tra số lượng và chuyển đổi sang DTO
        if (questions.size() < soCauThi) {
            throw new RuntimeException("Không đủ câu hỏi cho bài thi. Yêu cầu: " +
                    soCauThi + ", Hiện có: " + questions.size());
        }

        // Đảm bảo số lượng đúng
        if (questions.size() > soCauThi) {
            questions = questions.subList(0, soCauThi);
        }

        return questions.stream().map(this::convertToQuestionDTO).collect(Collectors.toList());
    }
   
    
    /**
     * Chuyển từ entity Bode sang DTO
     */
    private QuestionDTO convertToQuestionDTO(Bode bode) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(bode.getId().getCauHoi().toString());
        dto.setContent(bode.getNoiDung());

        Map<String, String> answers = new HashMap<>();
        answers.put("A", bode.getA());
        answers.put("B", bode.getB());
        answers.put("C", bode.getC());
        answers.put("D", bode.getD());

        dto.setAnswers(answers);
        dto.setCorrectAnswer(bode.getDapAn());

        return dto;
    }
    
    /**
     * Lấy câu hỏi cho bài thi (dùng cho giáo viên test đề)
     * Tương tự getQuestionsForExam nhưng không kiểm tra sinh viên đã thi chưa
     */
    public List<QuestionDTO> getQuestionsForTesting(String maLop, String maMH, int lan) {
        // Lấy thông tin lịch thi
        GiaovienDangky dangky = giaovienDangkyRepository.findByMalopAndMamhAndLan(maLop, maMH, (short) lan)
                .orElseThrow(() -> new RuntimeException(
                        "Không có lịch thi cho lớp " + maLop + ", môn " + maMH + ", lần " + lan));

        String trinhDo = dangky.getTrinhDo();
        int soCauThi = dangky.getSoCauThi();

        System.out.println("Trình độ: " + trinhDo);
        System.out.println("Số câu hỏi: " + soCauThi);

        // Lấy câu hỏi ngẫu nhiên theo trình độ đã đăng ký
        List<Bode> questions = bodeRepository.findRandomQuestionsForExam(maMH, trinhDo, soCauThi);

        // Nếu không đủ câu hỏi ở trình độ chính, kiểm tra xem có thể lấy thêm từ trình
        // độ thấp hơn không
        if (questions.size() < soCauThi) {
            String lowerLevel = getLowerLevel(trinhDo);
            if (lowerLevel != null) {
                // Lấy thêm câu hỏi từ trình độ thấp hơn
                int remainingQuestions = soCauThi - questions.size();
                List<Bode> additionalQuestions = bodeRepository.findRandomQuestionsForExam(
                        maMH, lowerLevel, remainingQuestions);
                questions.addAll(additionalQuestions);
            }
        }

        // Kiểm tra lại tổng số câu hỏi
        if (questions.size() < soCauThi) {
            throw new RuntimeException("Không đủ câu hỏi cho bài thi. Yêu cầu: " +
                    soCauThi + ", Hiện có: " + questions.size());
        }

        // Đảm bảo chỉ lấy đúng số lượng câu hỏi cần thiết
        if (questions.size() > soCauThi) {
            questions = questions.subList(0, soCauThi);
        }

        // Chuyển các câu hỏi sang DTO và trả về
        return questions.stream().map(this::convertToQuestionDTO).collect(Collectors.toList());
    }

    /**
     * Xác định trình độ thấp hơn dựa trên trình độ hiện tại
     */
    private String getLowerLevel(String currentLevel) {
        switch (currentLevel) {
            case "A":
                return "B"; // A (cao nhất) có thể lấy thêm từ B
            case "B":
                return "C"; // B có thể lấy thêm từ C
            case "C":
                return null; // C là thấp nhất, không có trình độ nào thấp hơn
            default:
                return null;
        }
    }

    /**
     * Nộp bài thi và lưu kết quả
     * Sử dụng giao tác để đảm bảo tính toàn vẹn dữ liệu
     */
    // @Transactional
    // public ExamResultDTO submitExam(ExamSubmissionDTO submission) {
    //     //kiểm tra thông tin đầu vào
    //     System.out.println("mã sinh viên: " + submission.getMaSV());
    //     System.out.println("mã lớp: " + submission.getMaLop());
    //     System.out.println("mã môn học: " + submission.getMaMH());
    //     System.out.println("lần thi: " + submission.getLan());
    //     // Map<String, String> answers = submission.getAnswers();
    //     // for (Map.Entry<String, String> entry : answers.entrySet()) {
    //     //     System.out.println("Câu hỏi: " + entry.getKey() + ", Đáp án: " + entry.getValue());
    //     // }

    //     // Kiểm tra thông tin bài thi
    //     Sinhvien sinhvien = sinhvienRepository.findById(submission.getMaSV())
    //             .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên với mã " + submission.getMaSV()));

    //     // Kiểm tra xem đã thi chưa
    //     boolean daThiRoi = bangdiemRepository
    //             .findBySinhvienAndMamhAndLan(sinhvien, submission.getMaMH(), submission.getLan()).isPresent();
    //     if (daThiRoi) {
    //         throw new RuntimeException("Sinh viên đã thi lần này rồi");
    //     }

    //     // Tính điểm
    //     double diemMoiCau = 10.0 / submission.getAnswers().size();
    //     int soCauDung = 0;

    //     // Thay vì cố gắng tạo BodeId từ key trong answers, sử dụng API để lấy câu hỏi theo mã môn học
    //     List<Bode> allQuestions = bodeRepository.findByIdMaMH(submission.getMaMH());

    //     // Lọc ra những câu hỏi mà sinh viên đã trả lời
    //     List<Bode> questions = allQuestions.stream()
    //             .filter(bode -> submission.getAnswers().containsKey(bode.getId().getCauHoi().toString()))
    //             .collect(Collectors.toList());

    //     if (questions.isEmpty()) {
    //         throw new RuntimeException("Không tìm thấy câu hỏi để chấm điểm");
    //     }

    //     // Tạo map để tra cứu đáp án đúng
    //     Map<String, String> questionIdToCorrectAnswer = questions.stream()
    //             .collect(Collectors.toMap(
    //                     bode -> bode.getId().getCauHoi().toString(),
    //                     Bode::getDapAn));

    //     // Tạo bảng điểm mới
    //     Monhoc monhoc = monhocRepository.findById(submission.getMaMH())
    //             .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học với mã " + submission.getMaMH()));
    //     BangDiem bangdiem = new BangDiem();
    //     bangdiem.setSinhvien(sinhvien);
    //     bangdiem.setMonhoc(monhoc);
    //     System.out.println(
    //             "Thông tin bảng điểm: " + bangdiem.getSinhvien().getMaSV() + ", " + bangdiem.getMonhoc().getMaMH());
    //     // Kiểm tra xem đã có bảng điểm cho sinh viên này chưa

    //     // Thiết lập thông tin bằng điểm
    //     // Tạo ID cho bảng điểm nếu cần
    //     BangDiemId bangDiemId = new BangDiemId();

    //     bangDiemId.setMaSV(submission.getMaSV());

    //     bangDiemId.setMaMH(submission.getMaMH());
    //     bangDiemId.setLan((short) submission.getLan());
    //     bangdiem.setId(bangDiemId);
    //     bangdiem.setNgayThi(java.time.LocalDate.now());
    //     System.out.println("Ngày thi: " + bangdiem.getNgayThi());
    //     for (Map.Entry<String, String> answer : submission.getAnswers().entrySet()) {
    //         String questionId = answer.getKey();
    //         String userAnswer = answer.getValue();
    //         String correctAnswer = questionIdToCorrectAnswer.get(questionId);

    //         // Nếu không tìm thấy đáp án đúng, bỏ qua câu hỏi này
    //         if (correctAnswer == null) {
    //             continue;
    //         }

    //         boolean isCorrect = userAnswer.equals(correctAnswer);
    //         if (isCorrect) {
    //             soCauDung++;
    //         }
    //     }

    //     // Tính và lưu điểm
    //     double diem = (questions.isEmpty()) ? 0 : (soCauDung * diemMoiCau);
    //     bangdiem.setDiem((float) diem);

    //     // Lưu bảng điểm vào database sử dụng giao tác
    //     bangdiemRepository.save(bangdiem);

    //     // Lưu chi tiết bảng điểm

    //     // Trả về kết quả
    //     ExamResultDTO result = new ExamResultDTO();
    //     result.setScore(diem);
    //     result.setTotalQuestions(submission.getAnswers().size());
    //     result.setCorrectAnswers(soCauDung);
    //     saveExamDetails(bangdiem, submission.getAnswers(), questionIdToCorrectAnswer);

    //     return result;
    // }
    @Transactional
    public ExamResultDTO submitExam(ExamSubmissionDTO submission) {
        // Kiểm tra thông tin đầu vào
        System.out.println("mã sinh viên: " + submission.getMaSV());
        System.out.println("mã lớp: " + submission.getMaLop());
        System.out.println("mã môn học: " + submission.getMaMH());
        System.out.println("lần thi: " + submission.getLan());
        System.out.println("Tổng số câu hỏi: " + submission.getAnswers().size());

        // Kiểm tra thông tin bài thi
        Sinhvien sinhvien = sinhvienRepository.findById(submission.getMaSV())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên với mã " + submission.getMaSV()));

        // Kiểm tra xem đã thi chưa
        boolean daThiRoi = bangdiemRepository
                .findBySinhvienAndMamhAndLan(sinhvien, submission.getMaMH(), submission.getLan()).isPresent();
        if (daThiRoi) {
            throw new RuntimeException("Sinh viên đã thi lần này rồi");
        }

        // Lấy tất cả câu hỏi của môn học
        List<Bode> allQuestions = bodeRepository.findByIdMaMH(submission.getMaMH());
        if (allQuestions.isEmpty()) {
            throw new RuntimeException("Không tìm thấy câu hỏi cho môn học này");
        }
        System.out.println("Tổng số câu hỏi trong bộ đề: " + allQuestions.size());

        // Tạo map để tra cứu đáp án đúng cho tất cả câu hỏi
        Map<String, String> questionIdToCorrectAnswer = allQuestions.stream()
                .collect(Collectors.toMap(
                        bode -> bode.getId().getCauHoi().toString(),
                        Bode::getDapAn));
                        System.out.println("Đã tạo map tra cứu đáp án đúng cho " + questionIdToCorrectAnswer.size() + " câu hỏi");

        // Tính điểm
        int totalAnsweredQuestions = 0; // Số câu đã trả lời (không tính câu để trống)
        int soCauDung = 0;

        // Kiểm tra từng câu trả lời
        for (Map.Entry<String, String> answer : submission.getAnswers().entrySet()) {
            String questionId = answer.getKey();
            String userAnswer = answer.getValue();
            String correctAnswer = questionIdToCorrectAnswer.get(questionId);
            System.out.println("Kiểm tra câu hỏi ID: " + questionId + ", Trả lời: " + userAnswer + ", Đáp án đúng: " + correctAnswer);

            // Nếu không tìm thấy câu hỏi trong bộ đề, bỏ qua
            if (correctAnswer == null) {
                System.out.println("Không tìm thấy câu hỏi với ID: " + questionId);
                continue;
            }

            // Chỉ tính câu có trả lời (không phải chuỗi rỗng)
            if (userAnswer != null && !userAnswer.trim().isEmpty()) {
                totalAnsweredQuestions++;
                // Kiểm tra đáp án
                System.out.println("Kiểm tra đáp án: " + userAnswer + " so với " + correctAnswer);
                boolean isCorrect = userAnswer.equals(correctAnswer);
                if (isCorrect) {
                    // Nếu đúng, tăng số câu đúng
                    soCauDung++;
                    System.out.println("Câu hỏi ID: " + questionId + " đúng");

                }
            }
        }

        // Lấy tổng số câu hỏi thực tế trong bài thi
        int totalQuestions = submission.getAnswers().size();

        // Tính điểm: Chỉ tính trên số câu đúng so với tổng số câu
        double diemMoiCau = 10.0 / totalQuestions;
        double diem = soCauDung * diemMoiCau;
        System.out.println("Điểm tính được: " + diem + " (Số câu đúng: " + soCauDung + ", Tổng câu: " + totalQuestions + ")");

        // Tạo bảng điểm mới
        Monhoc monhoc = monhocRepository.findById(submission.getMaMH())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học với mã " + submission.getMaMH()));

        BangDiem bangdiem = new BangDiem();
        bangdiem.setSinhvien(sinhvien);
        bangdiem.setMonhoc(monhoc);

        // Thiết lập thông tin bảng điểm
        BangDiemId bangDiemId = new BangDiemId();
        bangDiemId.setMaSV(submission.getMaSV());
        bangDiemId.setMaMH(submission.getMaMH());
        bangDiemId.setLan((short) submission.getLan());
        bangdiem.setId(bangDiemId);
        bangdiem.setNgayThi(java.time.LocalDate.now());
        bangdiem.setDiem((float) diem);

        // Lưu bảng điểm vào database
        bangdiemRepository.save(bangdiem);

        // Lưu chi tiết bài thi
        saveExamDetails(bangdiem, submission.getAnswers(), questionIdToCorrectAnswer);

        // Tạo và trả về kết quả
        ExamResultDTO result = new ExamResultDTO();
        result.setScore(diem);
        result.setTotalQuestions(totalQuestions);
        result.setCorrectAnswers(soCauDung);
        result.setAnsweredQuestions(totalAnsweredQuestions); // Thêm thông tin về số câu đã trả lời

        System.out.println("Kết quả: Điểm = " + diem + ", Số câu đúng = " + soCauDung +
                ", Tổng số câu = " + totalQuestions + ", Số câu đã trả lời = " + totalAnsweredQuestions);

        return result;
    }
    // Thêm phương thức mới để lưu chi tiết bài thi
    private void saveExamDetails(BangDiem bangdiem, Map<String, String> answers,
            Map<String, String> questionIdToCorrectAnswer) {
        for (Map.Entry<String, String> answer : answers.entrySet()) {
            String questionId = answer.getKey();
            String userAnswer = answer.getValue();

            try {
                // Tạo ChiTietBaiThiId
                ChiTietBaiThiId id = new ChiTietBaiThiId();
                id.setMaSV(bangdiem.getSinhvien().getMaSV());
                id.setMaMH(bangdiem.getMonhoc().getMaMH());
                id.setLan(bangdiem.getId().getLan().intValue());
                id.setCauHoi(Integer.parseInt(questionId));

                // Tạo ChiTietBaiThi
                ChiTietBaiThi chiTiet = new ChiTietBaiThi();
                chiTiet.setId(id);
                chiTiet.setBangDiem(bangdiem);

                // Tìm bode tương ứng
                BodeId bodeId = new BodeId(bangdiem.getMonhoc().getMaMH(), Integer.parseInt(questionId));
                Bode bode = bodeRepository.findById(bodeId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi: " + questionId));

                chiTiet.setBode(bode);
                chiTiet.setTraLoi(userAnswer);

                // Lưu chi tiết bài thi
                chiTietBaiThiRepository.save(chiTiet);

            } catch (NumberFormatException e) {
                // Bỏ qua nếu questionId không phải số
                System.err.println("Invalid question ID: " + questionId);
            }
        }
    }
}