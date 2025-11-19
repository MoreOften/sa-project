package ku.cs.models.report;

/**
 * Enum นี้ใช้สำหรับจัดการสถานะของ Report
 * - DRAFT: (ร่าง) Instructor กำลังแก้ไข
 * - PENDING_APPROVAL: (รออนุมัติ) Instructor ส่งให้ Supervisor แล้ว
 * - APPROVED: (อนุมัติ) Supervisor อนุมัติแล้ว
 */
public enum ReportStatus {
    DRAFT,
    PENDING_REVIEW,
    APPROVED,
    REJECTED
}
