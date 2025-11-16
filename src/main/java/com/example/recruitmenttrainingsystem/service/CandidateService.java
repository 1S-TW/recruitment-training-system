package com.example.recruitmenttrainingsystem.service;
import com.example.recruitmenttrainingsystem.dto.CreateCandidateDto;
import com.example.recruitmenttrainingsystem.entity.RecruitmentPlan;
import com.example.recruitmenttrainingsystem.repository.RecruitmentPlanRepository;
import com.example.recruitmenttrainingsystem.dto.CandidateListDto;
import com.example.recruitmenttrainingsystem.entity.Candidate;
import com.example.recruitmenttrainingsystem.entity.CandidateResult;
import com.example.recruitmenttrainingsystem.entity.CandidateReview;
import com.example.recruitmenttrainingsystem.repository.CandidateRepository;
import com.example.recruitmenttrainingsystem.repository.CandidateReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import com.example.recruitmenttrainingsystem.dto.AddCandidateResultDto;
import com.example.recruitmenttrainingsystem.dto.CreateCandidateDto;
import com.example.recruitmenttrainingsystem.entity.User;
import com.example.recruitmenttrainingsystem.exception.CustomException;
import com.example.recruitmenttrainingsystem.repository.CandidateResultRepository;
import com.example.recruitmenttrainingsystem.repository.RecruitmentPlanRepository;
import com.example.recruitmenttrainingsystem.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import com.example.recruitmenttrainingsystem.entity.RecruitmentPlan;
import com.example.recruitmenttrainingsystem.entity.HrRequest;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final CandidateReviewRepository candidateReviewRepository;
    private final RecruitmentPlanRepository recruitmentPlanRepository;
    private final CandidateResultRepository candidateResultRepository;
    private final UserRepository userRepository;
    // GET candidates (all hoặc theo planId)
    @Transactional(readOnly = true)
    public List<CandidateListDto> getCandidates(Long planId) {
        List<Candidate> candidates =
                (planId != null)
                        ? candidateRepository.findByRecruitmentPlan_RecruitmentPlanId(planId)
                        : candidateRepository.findAll();

        return candidates.stream()
                .map(this::toListDto)
                .toList();
    }

    // GET review theo ứng viên
    @Transactional(readOnly = true)
    public List<CandidateReview> getReviewsByCandidate(Long candidateId) {
        if (candidateId == null) return Collections.emptyList();
        return candidateReviewRepository.findByCandidate_CandidateId(candidateId);
    }
    // ham them ung vien
    @Transactional
    public CandidateListDto createCandidate(CreateCandidateDto dto) {
        // 1. Tìm Kế hoạch tuyển dụng
        RecruitmentPlan plan = recruitmentPlanRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kế hoạch tuyển dụng ID: " + dto.getPlanId()));

        // 2. Kiểm tra email trùng trong plan
        boolean exists = candidateRepository.existsByEmailAndRecruitmentPlan_RecruitmentPlanId(dto.getEmail(), dto.getPlanId());
        if (exists) {
            // Có thể dùng CustomException nếu bạn đã định nghĩa
            throw new RuntimeException("Email ứng viên đã tồn tại trong kế hoạch này.");
        }

        // 3. Tạo Entity
        Candidate candidate = new Candidate();
        candidate.setFullName(dto.getFullName());
        candidate.setEmail(dto.getEmail());
        candidate.setPhoneNumber(dto.getPhoneNumber());
        candidate.setCvLink(dto.getCvLink());
        candidate.setInterviewDate(dto.getInterviewDate());
        candidate.setRecruitmentPlan(plan);
        // 'reviews' và 'results' sẽ là list rỗng (theo @Builder.Default trong Entity)

        // 4. Lưu vào DB
        Candidate savedCandidate = candidateRepository.save(candidate);

        // 5. Map sang DTO để trả về cho FE (giống hàm GET)
        return toListDto(savedCandidate);
    }
    // cap nhat
    @Transactional
    public CandidateListDto addCandidateResult(Long candidateId, AddCandidateResultDto dto, String reviewerEmail) {

        // 1. Tìm ứng viên (SỬA: Dùng hàm query mới để lấy EAGER)
        Candidate candidate = candidateRepository.findByIdWithPlanAndRequestDetails(candidateId)
                .orElseThrow(() -> new CustomException("Không tìm thấy ứng viên với ID: " + candidateId));

        // 2. Tìm người chấm điểm
        User reviewer = userRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new CustomException("Không tìm thấy người dùng (reviewer) với email: " + reviewerEmail));

        // ✅ 3. LOGIC MỚI: Kiểm tra giới hạn "PASS"
        if (dto.getFinalResult().equalsIgnoreCase("PASS")) {
            RecruitmentPlan plan = candidate.getRecruitmentPlan();
            HrRequest request = plan.getRequest();

            // Tính tổng giới hạn "Đầu vào" (soLuong * 2)
            int totalLimit = request.getQuantityCandidates().stream()
                    .mapToInt(qc -> qc.getSoLuong() * 2)
                    .sum();

            // Đếm số lượng đã PASS hiện tại
            long currentPassCount = candidateResultRepository
                    .countByCandidate_RecruitmentPlan_RecruitmentPlanIdAndFinalResultIgnoreCase(
                            plan.getRecruitmentPlanId(),
                            "PASS"
                    );

            // So sánh
            if (currentPassCount >= totalLimit) {
                throw new CustomException(
                        "Kế hoạch này đã đạt đủ số lượng 'PASS' (" + currentPassCount + "/" + totalLimit + "). " +
                                "Không thể chấm 'PASS' cho ứng viên này."
                );
            }
        }
        // ✅ KẾT THÚC LOGIC MỚI

        // 4. Tạo và lưu CandidateReview (Trạng Thái)
        CandidateReview review = new CandidateReview();
        review.setCandidate(candidate);
        review.setUser(reviewer);
        review.setCandidateStatus(dto.getCandidateStatus());
        review.setNote(dto.getNote());

        CandidateReview savedReview = candidateReviewRepository.save(review);

        // 5. Tạo và lưu CandidateResult (Kết quả phỏng vấn)
        CandidateResult result = new CandidateResult();
        result.setCandidate(candidate);
        result.setReview(savedReview);
        result.setAttendedInterview(dto.getAttendedInterview());
        result.setTestScore(dto.getTestScore());
        result.setInterviewScore(dto.getInterviewScore());
        result.setComment(dto.getComment());
        result.setFinalResult(dto.getFinalResult());

        candidateResultRepository.save(result);

        // 6. Cập nhật object 'candidate' (để toListDto dùng)
        candidate.getReviews().add(savedReview);
        candidate.getResults().add(result);

        // 7. Trả về DTO đã cập nhật
        return toListDto(candidate);
    }

    private CandidateListDto toListDto(Candidate c) {

        // status lấy từ CandidateResult cuối cùng
        String status = "Chưa có kết quả";
        BigDecimal testScore = null;
        BigDecimal interviewScore = null;

        if (c.getResults() != null && !c.getResults().isEmpty()) {
            // Sắp xếp để lấy mới nhất (hoặc dùng query)
            // Giả sử list đã được sắp xếp hoặc ta lấy cái cuối cùng
            CandidateResult latest = c.getResults().get(c.getResults().size() - 1);

            // map BE → FE status
            if (latest.getFinalResult() != null) {
                String r = latest.getFinalResult().toUpperCase();

                // Cập nhật logic status
                if (r.equals("PASS")) status = "Đã có kết quả";
                else if (r.equals("FAIL")) status = "Không nhận việc";
                else status = latest.getFinalResult(); // Giữ nguyên nếu là status khác
            }

            testScore = latest.getTestScore();
            interviewScore = latest.getInterviewScore();
        }

        return CandidateListDto.builder()
                .candidateId(c.getCandidateId())
                .fullName(c.getFullName())
                .email(c.getEmail())
                .phoneNumber(c.getPhoneNumber())
                .cvLink(c.getCvLink())
                .interviewDate(c.getInterviewDate())
                .testScore(testScore)
                .interviewScore(interviewScore)
                .status(status)
                .recruitmentPlanId(
                        c.getRecruitmentPlan() != null ?
                                c.getRecruitmentPlan().getRecruitmentPlanId() : null
                )
                .recruitmentPlanName(
                        c.getRecruitmentPlan() != null ?
                                c.getRecruitmentPlan().getPlanName() : null
                )
                .build();
    }
}