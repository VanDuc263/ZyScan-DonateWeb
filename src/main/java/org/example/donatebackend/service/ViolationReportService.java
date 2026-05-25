package org.example.donatebackend.service;

import org.example.donatebackend.dto.request.AdminUpdateReportStatusRequest;
import org.example.donatebackend.dto.request.CreateViolationReportRequest;
import org.example.donatebackend.dto.response.ReportPenaltyResponse;
import org.example.donatebackend.dto.response.ViolationReportResponse;
import org.example.donatebackend.entity.Donation;
import org.example.donatebackend.entity.NotificationEntity;
import org.example.donatebackend.entity.ReportPenaltyEntity;
import org.example.donatebackend.entity.StreamerEntity;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.entity.ViolationReportEntity;
import org.example.donatebackend.exception.AppException;
import org.example.donatebackend.exception.ErrorCode;
import org.example.donatebackend.repository.DonationRepository;
import org.example.donatebackend.repository.ReportPenaltyRepository;
import org.example.donatebackend.repository.StreamerRepository;
import org.example.donatebackend.repository.UserRepository;
import org.example.donatebackend.repository.ViolationReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ViolationReportService {

    @Autowired private ViolationReportRepository violationReportRepository;
    @Autowired private ReportPenaltyRepository reportPenaltyRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private StreamerRepository streamerRepository;
    @Autowired private DonationRepository donationRepository;
    @Autowired(required = false) private NotificationService notificationService;

    @Transactional
    public ViolationReportResponse createReport(CreateViolationReportRequest req) {
        UserEntity reporter = getCurrentUser();
        ensureReporterIsNotBlocked(reporter);

        ViolationReportEntity.TargetType targetType = parseTargetType(req.getTargetType());
        Long targetId = req.getTargetId();
        String reason = trimOrNull(req.getReason());

        if (targetId == null || targetId <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "targetId không hợp lệ");
        }
        if (reason == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Lý do báo cáo không được để trống");
        }

        TargetInfo targetInfo = validateAndBuildTargetInfo(targetType, targetId, reporter);

        boolean duplicatedPendingReport = violationReportRepository.existsByReporter_IdAndTargetTypeAndTargetIdAndStatusIn(
                reporter.getId(),
                targetType,
                targetId,
                List.of(ViolationReportEntity.ReportStatus.PENDING, ViolationReportEntity.ReportStatus.REVIEWING)
        );

        if (duplicatedPendingReport) {
            throw new AppException(
                    ErrorCode.REPORT_DUPLICATE_PENDING,
                    "Bạn đã gửi báo cáo cho đối tượng này rồi. Vui lòng chờ admin xử lý."
            );
        }

        ViolationReportEntity report = new ViolationReportEntity();
        report.setReporter(reporter);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReason(reason);
        report.setDescription(trimOrNull(req.getDescription()));
        report.setEvidenceUrl(trimOrNull(req.getEvidenceUrl()));
        report.setTargetSnapshot(targetInfo.snapshot());
        report.setStatus(ViolationReportEntity.ReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        ViolationReportEntity saved = violationReportRepository.save(report);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ViolationReportResponse> getMyReports() {
        UserEntity currentUser = getCurrentUser();
        return violationReportRepository.findByReporter_IdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ViolationReportResponse> adminFindAll(String status, String targetType) {
        ViolationReportEntity.ReportStatus parsedStatus = parseStatusOrNull(status);
        ViolationReportEntity.TargetType parsedTargetType = parseTargetTypeOrNull(targetType);

        List<ViolationReportEntity> reports;
        if (parsedStatus != null && parsedTargetType != null) {
            reports = violationReportRepository.findByStatusAndTargetTypeOrderByCreatedAtDesc(parsedStatus, parsedTargetType);
        } else if (parsedStatus != null) {
            reports = violationReportRepository.findByStatusOrderByCreatedAtDesc(parsedStatus);
        } else if (parsedTargetType != null) {
            reports = violationReportRepository.findByTargetTypeOrderByCreatedAtDesc(parsedTargetType);
        } else {
            reports = violationReportRepository.findAllByOrderByCreatedAtDesc();
        }

        return reports.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ViolationReportResponse adminFindById(Long id) {
        return toResponse(findReportOrThrow(id));
    }

    @Transactional
    public ViolationReportResponse adminUpdateStatus(Long id, AdminUpdateReportStatusRequest req) {
        UserEntity admin = getCurrentUser();
        ViolationReportEntity report = findReportOrThrow(id);
        ViolationReportEntity.ReportStatus newStatus = parseStatus(req.getStatus());

        if (newStatus == ViolationReportEntity.ReportStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_REPORT_STATUS, "Admin không thể cập nhật lại về PENDING");
        }

        report.setStatus(newStatus);
        report.setAdminNote(trimOrNull(req.getAdminNote()));
        report.setReviewedBy(admin);
        report.setReviewedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        ViolationReportEntity saved = violationReportRepository.save(report);

        if (newStatus == ViolationReportEntity.ReportStatus.FALSE_REPORT) {
            increaseFalseReportPenalty(report.getReporter());
        }

        notifyReporterAfterReview(saved);

        return toResponse(saved);
    }

    @Transactional
    public void adminDelete(Long id) {
        ViolationReportEntity report = findReportOrThrow(id);
        violationReportRepository.delete(report);
    }

    @Transactional(readOnly = true)
    public ReportPenaltyResponse adminGetPenalty(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy user"));

        ReportPenaltyEntity penalty = reportPenaltyRepository.findByUser_Id(userId).orElse(null);
        return toPenaltyResponse(user, penalty);
    }

    private void ensureReporterIsNotBlocked(UserEntity reporter) {
        ReportPenaltyEntity penalty = reportPenaltyRepository.findByUser_Id(reporter.getId()).orElse(null);
        if (penalty == null || penalty.getBlockedUntil() == null) {
            return;
        }

        if (penalty.getBlockedUntil().isAfter(LocalDateTime.now())) {
            String until = penalty.getBlockedUntil().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            throw new AppException(
                    ErrorCode.REPORT_BLOCKED,
                    "Bạn đang bị khóa chức năng báo cáo đến " + until
            );
        }
    }

    private TargetInfo validateAndBuildTargetInfo(
            ViolationReportEntity.TargetType targetType,
            Long targetId,
            UserEntity reporter
    ) {
        if (targetType == ViolationReportEntity.TargetType.STREAMER) {
            StreamerEntity streamer = streamerRepository.findById(targetId)
                    .orElseThrow(() -> new AppException(ErrorCode.TARGET_NOT_FOUND, "Không tìm thấy streamer bị báo cáo"));

            if (streamer.getUserId() != null && streamer.getUserId().equals(reporter.getId())) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Bạn không thể tự báo cáo trang streamer của mình");
            }

            String name = streamer.getDisplayName();
            String snapshot = "STREAMER{id=" + streamer.getId()
                    + ", displayName='" + safe(streamer.getDisplayName()) + "'"
                    + ", token='" + safe(streamer.getToken()) + "'}";
            return new TargetInfo(name, snapshot);
        }

        if (targetType == ViolationReportEntity.TargetType.USER) {
            UserEntity targetUser = userRepository.findById(targetId)
                    .orElseThrow(() -> new AppException(ErrorCode.TARGET_NOT_FOUND, "Không tìm thấy user bị báo cáo"));

            if (targetUser.getId() == reporter.getId()) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Bạn không thể tự báo cáo chính mình");
            }

            String name = targetUser.getUsername();
            String snapshot = "USER{id=" + targetUser.getId()
                    + ", username='" + safe(targetUser.getUsername()) + "'"
                    + ", email='" + safe(targetUser.getEmail()) + "'"
                    + ", role='" + (targetUser.getRole() != null ? targetUser.getRole().name() : "") + "'}";
            return new TargetInfo(name, snapshot);
        }

        Donation donation = donationRepository.findById(targetId)
                .orElseThrow(() -> new AppException(ErrorCode.TARGET_NOT_FOUND, "Không tìm thấy donate bị báo cáo"));

        String streamerName = donation.getStreamer() != null ? donation.getStreamer().getDisplayName() : null;
        String name = "Donate #" + donation.getId();
        String snapshot = "DONATION{id=" + donation.getId()
                + ", donorName='" + safe(donation.getDonorName()) + "'"
                + ", streamer='" + safe(streamerName) + "'"
                + ", amount=" + donation.getAmount()
                + ", status='" + safe(donation.getStatus()) + "'}";
        return new TargetInfo(name, snapshot);
    }

    private void increaseFalseReportPenalty(UserEntity reporter) {
        if (reporter == null) return;

        ReportPenaltyEntity penalty = reportPenaltyRepository.findByUser_Id(reporter.getId())
                .orElseGet(() -> {
                    ReportPenaltyEntity created = new ReportPenaltyEntity();
                    created.setUser(reporter);
                    created.setFalseReportCount(0);
                    created.setCreatedAt(LocalDateTime.now());
                    return created;
                });

        int newCount = penalty.getFalseReportCount() == null ? 1 : penalty.getFalseReportCount() + 1;
        penalty.setFalseReportCount(newCount);
        penalty.setUpdatedAt(LocalDateTime.now());

        if (newCount >= 5) {
            penalty.setBlockedUntil(LocalDateTime.now().plusDays(30));
        } else if (newCount >= 3) {
            penalty.setBlockedUntil(LocalDateTime.now().plusDays(7));
        }

        reportPenaltyRepository.save(penalty);
    }

    private void notifyReporterAfterReview(ViolationReportEntity report) {
        if (notificationService == null || report.getReporter() == null) return;

        try {
            String title = "Báo cáo vi phạm đã được xử lý";
            String content = switch (report.getStatus()) {
                case RESOLVED -> "Admin đã xác nhận báo cáo của bạn là hợp lệ và đã xử lý.";
                case REJECTED -> "Admin đã từ chối báo cáo vì không đủ căn cứ hoặc không phát hiện vi phạm.";
                case FALSE_REPORT -> "Báo cáo bị đánh dấu là không đúng sự thật. Nếu lặp lại nhiều lần, bạn sẽ bị khóa chức năng báo cáo.";
                case REVIEWING -> "Admin đang xem xét báo cáo của bạn.";
                default -> "Báo cáo của bạn đã được cập nhật.";
            };

            notificationService.createNotification(
                    report.getReporter().getId(),
                    NotificationEntity.NotificationType.SYSTEM,
                    title,
                    content,
                    "/account/reports",
                    "reportId=" + report.getId()
            );
        } catch (Exception ignored) {
        }
    }

    private ViolationReportEntity findReportOrThrow(Long id) {
        return violationReportRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.REPORT_NOT_FOUND, "Không tìm thấy báo cáo"));
    }

    private UserEntity getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy user hiện tại"));
    }

    private ViolationReportEntity.TargetType parseTargetType(String value) {
        try {
            return ViolationReportEntity.TargetType.valueOf(String.valueOf(value).trim().toUpperCase());
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "targetType chỉ được là STREAMER, USER hoặc DONATION");
        }
    }

    private ViolationReportEntity.TargetType parseTargetTypeOrNull(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return parseTargetType(value);
    }

    private ViolationReportEntity.ReportStatus parseStatus(String value) {
        try {
            return ViolationReportEntity.ReportStatus.valueOf(String.valueOf(value).trim().toUpperCase());
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_REPORT_STATUS, "status chỉ được là REVIEWING, RESOLVED, REJECTED hoặc FALSE_REPORT");
        }
    }

    private ViolationReportEntity.ReportStatus parseStatusOrNull(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return parseStatus(value);
    }

    private ViolationReportResponse toResponse(ViolationReportEntity report) {
        ViolationReportResponse response = new ViolationReportResponse();
        response.setId(report.getId());

        if (report.getReporter() != null) {
            response.setReporterId(report.getReporter().getId());
            response.setReporterUsername(report.getReporter().getUsername());
            response.setReporterEmail(report.getReporter().getEmail());
        }

        response.setTargetType(report.getTargetType() != null ? report.getTargetType().name() : null);
        response.setTargetId(report.getTargetId());
        response.setTargetName(resolveTargetName(report));
        response.setTargetSnapshot(report.getTargetSnapshot());

        response.setReason(report.getReason());
        response.setDescription(report.getDescription());
        response.setEvidenceUrl(report.getEvidenceUrl());
        response.setStatus(report.getStatus() != null ? report.getStatus().name() : null);
        response.setAdminNote(report.getAdminNote());

        if (report.getReviewedBy() != null) {
            response.setReviewedBy(report.getReviewedBy().getId());
            response.setReviewedByUsername(report.getReviewedBy().getUsername());
        }
        response.setReviewedAt(report.getReviewedAt());
        response.setCreatedAt(report.getCreatedAt());
        response.setUpdatedAt(report.getUpdatedAt());
        return response;
    }

    private ReportPenaltyResponse toPenaltyResponse(UserEntity user, ReportPenaltyEntity penalty) {
        ReportPenaltyResponse response = new ReportPenaltyResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setFalseReportCount(penalty != null && penalty.getFalseReportCount() != null ? penalty.getFalseReportCount() : 0);
        response.setBlockedUntil(penalty != null ? penalty.getBlockedUntil() : null);
        response.setBlocked(response.getBlockedUntil() != null && response.getBlockedUntil().isAfter(LocalDateTime.now()));
        return response;
    }

    private String resolveTargetName(ViolationReportEntity report) {
        if (report.getTargetType() == null || report.getTargetId() == null) return null;

        try {
            if (report.getTargetType() == ViolationReportEntity.TargetType.STREAMER) {
                return streamerRepository.findById(report.getTargetId())
                        .map(StreamerEntity::getDisplayName)
                        .orElse(null);
            }
            if (report.getTargetType() == ViolationReportEntity.TargetType.USER) {
                return userRepository.findById(report.getTargetId())
                        .map(UserEntity::getUsername)
                        .orElse(null);
            }
            return donationRepository.findById(report.getTargetId())
                    .map(d -> "Donate #" + d.getId())
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private String trimOrNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("'", "\\'");
    }

    private record TargetInfo(String targetName, String snapshot) {
    }
}
