# hotel-pms (mock Cloudbeds-style PMS)

Mock PMS cho **Dididi Booking Platform** (Phase 1.5). Spring Boot 3.5.14 · Java 17 · port **8082** · DB `hotel_pms`.

Có **admin UI** (mô phỏng Cloudbeds: CRUD hotel/room type + inventory calendar + danh sách reservation)
và **REST API** để Booking Platform pull dữ liệu, auth bằng header `X-API-KEY`.

## Prerequisites
JDK 17, MySQL 8 (localhost:3306). Không Redis, không Lombok.

## Setup DB (DBeaver, Alt+X)
```sql
CREATE DATABASE IF NOT EXISTS hotel_pms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT ALL PRIVILEGES ON hotel_pms.* TO 'booking'@'localhost';
FLUSH PRIVILEGES;
```

## Run
```bash
./mvnw spring-boot:run
```
Hibernate tạo bảng (`hotels`, `room_types`, `room_inventory`, `reservations`) rồi `data.sql`
seed **5 khách sạn + 15 loại phòng** (`INSERT IGNORE` nên restart không lỗi). Port 8082 — chạy
song song booking-platform (8080) + flight-provider (8081).

## Admin UI (public trong dev, mở bằng trình duyệt)
- http://localhost:8082/admin — dashboard
- http://localhost:8082/admin/hotels — danh sách + thêm/sửa/xoá khách sạn (**DoD: CRUD**)
- vào 1 khách sạn → thêm/sửa loại phòng, mở **Lịch phòng** để chỉnh availability/giá từng ngày
- http://localhost:8082/admin/reservations — đặt phòng nhận từ Booking Platform

## REST API (cần header `X-API-KEY: dev-pms-key-67890`)
```bash
KEY="dev-pms-key-67890"

curl -s localhost:8082/api/pms/v1/hotels -H "X-API-KEY: $KEY"
curl -s localhost:8082/api/pms/v1/hotels/1 -H "X-API-KEY: $KEY"
curl -s localhost:8082/api/pms/v1/hotels/1/rooms -H "X-API-KEY: $KEY"

# Inventory theo khoảng ngày (DoD)
curl -s "localhost:8082/api/pms/v1/hotels/1/inventory?from=2026-07-01&to=2026-07-31" -H "X-API-KEY: $KEY"

# Đặt phòng
curl -s -X POST localhost:8082/api/pms/v1/hotels/1/reserve -H "X-API-KEY: $KEY" \
  -H "Content-Type: application/json" \
  -d '{"roomTypeId":1,"guestName":"Tran Thi B","checkIn":"2026-07-01","checkOut":"2026-07-03","rooms":2}'

# Huỷ (dùng reservationId trả về ở trên)
curl -s -X POST localhost:8082/api/pms/v1/reservations/1/cancel -H "X-API-KEY: $KEY"
```
Thiếu/sai `X-API-KEY` → **401** `{"error":"Invalid API key"}`.

## Mô hình inventory
- Mỗi ngày không có override → dùng default của `RoomType` (`totalRooms`, `basePrice`).
- Operator chỉnh 1 ngày trong "Lịch phòng" → tạo/sửa row `room_inventory` (override).
- Đặt/huỷ phòng → trừ/cộng lại số phòng theo từng đêm.

## Khác biệt so với tài liệu
- **Không dùng `spring-boot-starter-security`**: API auth bằng `ApiKeyAuthFilter`; admin UI để public
  trong dev cho dễ demo. Cần login cho operator thì thêm sau.

## Tests
`./mvnw test` — `InventoryServiceTest` (Mockito, không cần DB) kiểm tra logic default inventory.
