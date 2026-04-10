package Event.Event_booking.service;
import Event.Event_booking.dto.PendingOrganizerDTO;
import Event.Event_booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    /**
     * Returns a paginated list of users who have requested organizer access
     * but have not yet been approved (role = ORGANIZER, is_organizer_approved = FALSE).
     *
     * @param pageable page number, size, and sort supplied by the caller
     * @return page of PendingOrganizerDTO
     */
    public Page<PendingOrganizerDTO> getPendingOrganizerRequests(Pageable pageable) {
        return userRepository
                .findByRole_NameAndIsOrganizerApprovedFalse("ORGANIZER", pageable)
                .map(user -> PendingOrganizerDTO.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .createdAt(user.getCreatedAt())
                        .build());
    }
}
