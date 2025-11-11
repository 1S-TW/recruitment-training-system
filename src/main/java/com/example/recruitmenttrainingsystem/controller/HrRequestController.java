package com.example.recruitmenttrainingsystem.controller;


import com.example.recruitmenttrainingsystem.dto.HrRequestResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam; // <-- IMPORT MỚI CẦN THIẾT
import com.example.recruitmenttrainingsystem.service.HrRequestService;
// import com.example.recruitmenttrainingsystem.entity.HrRequest; // (Import này không được sử dụng, có thể xóa)
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hr-request")
public class HrRequestController {

    private final HrRequestService hrRequestService;

    @GetMapping
    public List<HrRequestResponse> getAllHrRequests() {
        return hrRequestService.getAllHrRequests();
    }
    /**
     * Endpoint MỚI: Tìm kiếm động theo tiêu đề và/hoặc trạng thái
     * URL: GET /api/hr-request/search?title=...&status=...
     * * @param title  (Tùy chọn) Tên tiêu đề để tìm kiếm (LIKE)
     * @param status (Tùy chọn) Trạng thái để lọc (EQUAL)
     * @return Danh sách các request phù hợp
     */
    @GetMapping("/search")
    public List<HrRequestResponse> searchHrRequests(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String status
    ) {
        return hrRequestService.searchHrRequests(title, status);
    }
}
