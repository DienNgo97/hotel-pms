package com.hotelpms.domain;

/**
 * Trang thai cua mot reservation. Thay cho cac magic string "CONFIRMED"/"CANCELLED"
 * de tranh typo lam hong state machine (PROV-X3).
 *
 * <p>Status duoc luu trong DB duoi dang String (cot {@code reservations.status}),
 * nen cung cap san cac hang so String de so sanh/gan ma khong phai goi {@code .name()}
 * khap noi.
 */
public enum ReservationStatus {
    CONFIRMED,
    CANCELLED;

    /** Gia tri String luu trong DB (== ten enum). */
    public static final String CONFIRMED_VALUE = "CONFIRMED";
    public static final String CANCELLED_VALUE = "CANCELLED";

    public String value() {
        return name();
    }
}
