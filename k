CREATE DATABASE hospitaldb;
CREATE DATABASE lmsdb;
use lmsdb;
drop database lmsdb;
show tables;

INSERT INTO role (id, role_name, description) VALUES
(1, 'SUPER_ADMIN', 'Quản trị hệ thống'),
(2, 'TRUONG_BO_PHAN', 'Trưởng bộ phận'),
(3, 'HR', 'Nhân sự'),
(4, 'USER', 'Người dùng thông thường');

INSERT INTO user (
    full_name,
    phone_number,
    email,
    password,
    role_id,
    status,
    created_at
) VALUES (
    'Phuc Le',
    '0909123456',
    'phuc.le@example.com',
    '$2b$12$9wAg.Bh3Q/L2Juj5u9ohsemqpM6e4b2ns04WmWUN02XfreCovR236', -- mật khẩu đã mã hóa bằng BCrypt
    1, -- SUPER_ADMIN
    true,
    NOW()
);


INSERT INTO technology (technology_name) VALUES
('Java'),
('Spring Boot'),
('ReactJS'),
('Angular');

INSERT INTO hr_request (
    request_title,
    status,
    expected_delivery_date,
    created_at,
    note,
    created_by
) VALUES
(
    'Tuyển dụng Java Developer',
    'PENDING',
    '2025-12-15',
    NOW(),
    'Cần tuyển gấp 2 Java Dev có kinh nghiệm Spring Boot',
    1
),
(
    'Tuyển dụng Frontend Developer',
    'APPROVED',
    '2025-12-20',
    NOW(),
    'Cần 1 ReactJS developer cho dự án mới',
    1
);

select * from quantity_candidate;
