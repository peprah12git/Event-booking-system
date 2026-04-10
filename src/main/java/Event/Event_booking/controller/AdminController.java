package Event.Event_booking.controller;
import Event.Event_booking.dto.PendingOrganizerDTO;
import Event.Event_booking.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    /**
     * GET /api/v1/admin/organizer-requests
     *
     * Lists all users whose role = ORGANIZER and is_organizer_approved = FALSE.
     * Protected: only users with ROLE_ADMIN may call this endpoint (enforced in SecurityConfig).
     *
     * Query params:
     *   page  - zero-based page index (default 0)
     *   size  - page size (default 20)
     *   sort  - field,direction e.g. createdAt,asc (default createdAt,desc)
     */
    @GetMapping("/organizer-requests")
    public ResponseEntity<Page<PendingOrganizerDTO>> getPendingOrganizerRequests(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PendingOrganizerDTO> result = adminService.getPendingOrganizerRequests(pageable);
        return ResponseEntity.ok(result);
    }
}
