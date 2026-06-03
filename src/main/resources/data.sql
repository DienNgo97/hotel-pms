-- Seed data for hotel-pms mock. INSERT IGNORE => idempotent on restart.
INSERT IGNORE INTO hotels (id, name, city, address, description, star_rating, active) VALUES
  (1,'Saigon Riverside Hotel','TP.HCM','12 Ton Duc Thang, Q1','Khach san ven song Sai Gon, gan pho di bo.',4,true),
  (2,'Hanoi Old Quarter Hotel','Ha Noi','45 Hang Bac, Hoan Kiem','Khach san pho co, di bo ra Ho Guom.',3,true),
  (3,'Da Nang Beach Resort','Da Nang','88 Vo Nguyen Giap','Resort sat bai bien My Khe.',5,true),
  (4,'Hue Imperial Hotel','Hue','9 Le Loi','Gan Dai Noi va song Huong.',4,true),
  (5,'Nha Trang Bay Hotel','Nha Trang','30 Tran Phu','View vinh Nha Trang.',4,true);

INSERT IGNORE INTO room_types (id, hotel_id, name, description, capacity, base_price, currency, total_rooms) VALUES
  (1,1,'Standard','Phong tieu chuan',2,800000,'VND',18),
  (2,1,'Deluxe','Phong cao cap, view dep',2,1400000,'VND',12),
  (3,1,'Suite','Phong suite rong rai',4,2600000,'VND',6),
  (4,2,'Standard','Phong tieu chuan',2,640000,'VND',18),
  (5,2,'Deluxe','Phong cao cap, view dep',2,1120000,'VND',12),
  (6,2,'Suite','Phong suite rong rai',4,2080000,'VND',6),
  (7,3,'Standard','Phong tieu chuan',2,1280000,'VND',18),
  (8,3,'Deluxe','Phong cao cap, view dep',2,2240000,'VND',12),
  (9,3,'Suite','Phong suite rong rai',4,4160000,'VND',6),
  (10,4,'Standard','Phong tieu chuan',2,880000,'VND',18),
  (11,4,'Deluxe','Phong cao cap, view dep',2,1540000,'VND',12),
  (12,4,'Suite','Phong suite rong rai',4,2860000,'VND',6),
  (13,5,'Standard','Phong tieu chuan',2,960000,'VND',18),
  (14,5,'Deluxe','Phong cao cap, view dep',2,1680000,'VND',12),
  (15,5,'Suite','Phong suite rong rai',4,3120000,'VND',6);
