-- SmartDesk CRM Initial Data Script
-- Bu script ilk kurulumda örnek verileri yükler

-- İlk önce mevcut verileri temizle (sadece development için)
-- DELETE FROM ticket_comments WHERE id > 0;
-- DELETE FROM ticket_attachments WHERE id > 0;
-- DELETE FROM ticket_history WHERE id > 0;
-- DELETE FROM tickets WHERE id > 0;
-- DELETE FROM users WHERE id > 0;
-- DELETE FROM departments WHERE id > 0;
-- DELETE FROM companies WHERE id > 0;

-- 1. Örnek Şirketler
INSERT IGNORE INTO companies (id, name, domain, phone, address, website, timezone, plan_type, max_users, max_tickets_per_month, is_active, created_at, updated_at) VALUES
(1, 'Erdemir Bilgi Teknolojileri', 'erdemir.com.tr', '+90 372 316 1000', 'Kozlu, Zonguldak, Türkiye', 'https://erdemir.com.tr', 'Europe/Istanbul', 'ENTERPRISE', 100, 1000, TRUE, NOW(), NOW()),
(2, 'TechCorp Solutions', 'techcorp.com', '+1 555 0123', '123 Tech Street, San Francisco, CA', 'https://techcorp.com', 'America/Los_Angeles', 'PREMIUM', 50, 500, TRUE, NOW(), NOW()),
(3, 'StartupHub Ltd', 'startuphub.io', '+44 20 7946 0958', '10 Downing Street, London, UK', 'https://startuphub.io', 'Europe/London', 'BASIC', 25, 200, TRUE, NOW(), NOW());

-- 2. Departmanlar
INSERT IGNORE INTO departments (id, company_id, name, description, email, is_active, created_at, updated_at) VALUES
(1, 1, 'Teknik Destek', 'Yazılım ve donanım teknik destek departmanı', 'teknik@erdemir.com.tr', TRUE, NOW(), NOW()),
(2, 1, 'Müşteri Hizmetleri', 'Genel müşteri soruları ve şikayetler', 'musteri@erdemir.com.tr', TRUE, NOW(), NOW()),
(3, 1, 'Satış', 'Satış öncesi danışmanlık ve teklifler', 'satis@erdemir.com.tr', TRUE, NOW(), NOW()),
(4, 2, 'Technical Support', 'Software technical support department', 'support@techcorp.com', TRUE, NOW(), NOW()),
(5, 2, 'Customer Success', 'Customer onboarding and success', 'success@techcorp.com', TRUE, NOW(), NOW()),
(6, 3, 'General Support', 'All customer inquiries', 'support@startuphub.io', TRUE, NOW(), NOW());

-- 3. Kullanıcılar
-- Şifreler: "password123" (BCrypt encoded)
INSERT IGNORE INTO users (id, company_id, department_id, email, password_hash, first_name, last_name, phone, role, status, email_verified, login_attempts, created_at, updated_at) VALUES
-- Super Admin
(1, 1, NULL, 'admin@erdemir.com.tr', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfcXVdcXvN4YtyrkHdvEZ2', 'System', 'Administrator', '+90 372 316 1001', 'SUPER_ADMIN', 'ACTIVE', TRUE, 0, NOW(), NOW()),

-- Managers
(2, 1, 1, 'mehmet.yilmaz@erdemir.com.tr', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfcXVdcXvN4YtyrkHdvEZ2', 'Mehmet', 'Yılmaz', '+90 372 316 1002', 'MANAGER', 'ACTIVE', TRUE, 0, NOW(), NOW()),
(3, 1, 2, 'ayse.demir@erdemir.com.tr', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfcXVdcXvN4YtyrkHdvEZ2', 'Ayşe', 'Demir', '+90 372 316 1003', 'MANAGER', 'ACTIVE', TRUE, 0, NOW(), NOW()),
(4, 2, 4, 'john.smith@techcorp.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfcXVdcXvN4YtyrkHdvEZ2', 'John', 'Smith', '+1 555 0124', 'MANAGER', 'ACTIVE', TRUE, 0, NOW(), NOW()),

-- Agents
(5, 1, 1, 'ali.kaya@erdemir.com.tr', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfcXVdcXvN4YtyrkHdvEZ2', 'Ali', 'Kaya', '+90 372 316 1004', 'AGENT', 'ACTIVE', TRUE, 0, NOW(), NOW()),
(6, 1, 1, 'fatma.ozturk@erdemir.com.tr', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfcXVdcXvN4YtyrkHdvEZ2', 'Fatma', 'Öztürk', '+90 372 316 1005', 'AGENT', 'ACTIVE', TRUE, 0, NOW(), NOW()),
(7, 1, 2, 'can.arslan@erdemir.com.tr', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfcXVdcXvN4YtyrkHdvEZ2', 'Can', 'Arslan', '+90 372 316 1006', 'AGENT', 'ACTIVE', TRUE, 0, NOW(), NOW()),
(8, 2, 4, 'sarah.johnson@techcorp.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfcXVdcXvN4YtyrkHdvEZ2', 'Sarah', 'Johnson', '+1 555 0125', 'AGENT', 'ACTIVE', TRUE, 0, NOW(), NOW()),
(9, 2, 5, 'mike.davis@techcorp.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfcXVdcXvN4YtyrkHdvEZ2', 'Mike', 'Davis', '+1 555 0126', 'AGENT', 'ACTIVE', TRUE, 0, NOW(), NOW());

-- 4. Manager atamalarını güncelle
UPDATE departments SET manager_id = 2 WHERE id = 1;
UPDATE departments SET manager_id = 3 WHERE id = 2;
UPDATE departments SET manager_id = 4 WHERE id = 4;

-- 5. Müşteriler
INSERT IGNORE INTO customers (id, company_id, email, phone, first_name, last_name, segment, company_name, address, is_active, created_at, updated_at) VALUES
(1, 1, 'ahmet.celik@gmail.com', '+90 532 123 4567', 'Ahmet', 'Çelik', 'PREMIUM', 'Çelik Holding', 'İstanbul, Türkiye', TRUE, NOW(), NOW()),
(2, 1, 'zeynep.kurt@outlook.com', '+90 533 234 5678', 'Zeynep', 'Kurt', 'VIP', 'Kurt Tekstil', 'Bursa, Türkiye', TRUE, NOW(), NOW()),
(3, 1, 'emre.sahin@yahoo.com', '+90 534 345 6789', 'Emre', 'Şahin', 'STANDARD', 'Bireysel Müşteri', 'Ankara, Türkiye', TRUE, NOW(), NOW()),
(4, 2, 'robert.wilson@gmail.com', '+1 555 0201', 'Robert', 'Wilson', 'PREMIUM', 'Wilson Industries', 'New York, USA', TRUE, NOW(), NOW()),
(5, 2, 'emma.brown@gmail.com', '+1 555 0202', 'Emma', 'Brown', 'STANDARD', 'Brown LLC', 'California, USA', TRUE, NOW(), NOW()),
(6, 3, 'james.taylor@gmail.com', '+44 7700 900123', 'James', 'Taylor', 'BASIC', 'Taylor & Co', 'Manchester, UK', TRUE, NOW(), NOW());

-- 6. Örnek Tickets
INSERT IGNORE INTO tickets (id, company_id, customer_id, creator_user_id, assigned_agent_id, department_id, ticket_number, title, description, priority, status, category, source, sla_deadline, is_internal, escalation_level, created_at, updated_at, last_activity_at) VALUES
(1, 1, 1, NULL, 5, 1, 'TK-20240125-0001', 'Sistem Giriş Sorunu', 'Sisteme giriş yaparken "Geçersiz kullanıcı" hatası alıyorum. Şifremi birkaç kez sıfırladım ama sorun devam ediyor.', 'HIGH', 'OPEN', 'TECHNICAL_SUPPORT', 'EMAIL', DATE_ADD(NOW(), INTERVAL 4 HOUR), FALSE, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW(), NOW()),

(2, 1, 2, NULL, 6, 1, 'TK-20240125-0002', 'Rapor Oluşturma Hatası', 'Aylık satış raporlarını oluştururken sistem donuyor. Excel export işlemi yarım kalıyor.', 'URGENT', 'IN_PROGRESS', 'BUG_REPORT', 'WEB_FORM', DATE_ADD(NOW(), INTERVAL 2 HOUR), FALSE, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), NOW()),

(3, 1, 3, NULL, 7, 2, 'TK-20240125-0003', 'Fatura Sorusu', 'Geçen ay gönderdiğiniz faturada anlayamadığım bir kalem var. Detaylı açıklama alabilir miyim?', 'NORMAL', 'PENDING', 'BILLING', 'PHONE', DATE_ADD(NOW(), INTERVAL 6 HOUR), FALSE, 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE), NOW(), NOW()),

(4, 2, 4, NULL, 8, 4, 'TK-20240125-0004', 'API Integration Issue', 'Having trouble integrating with your REST API. Getting 401 errors despite correct authentication.', 'HIGH', 'OPEN', 'TECHNICAL_SUPPORT', 'API', DATE_ADD(NOW(), INTERVAL 3 HOUR), FALSE, 0, DATE_SUB(NOW(), INTERVAL 45 MINUTE), NOW(), NOW()),

(5, 2, 5, NULL, 9, 5, 'TK-20240125-0005', 'Feature Request: Dark Mode', 'It would be great to have a dark mode option in the dashboard. Many users have requested this feature.', 'LOW', 'NEW', 'FEATURE_REQUEST', 'WEB_FORM', DATE_ADD(NOW(), INTERVAL 24 HOUR), FALSE, 0, DATE_SUB(NOW(), INTERVAL 15 MINUTE), NOW(), NOW()),

(6, 1, 1, NULL, NULL, 1, 'TK-20240125-0006', 'Performans Sorunu', 'Dashboard sayfası çok yavaş yükleniyor. Özellikle grafiklerde problem var.', 'NORMAL', 'NEW', 'TECHNICAL_SUPPORT', 'CHAT', DATE_ADD(NOW(), INTERVAL 8 HOUR), FALSE, 0, NOW(), NOW(), NOW());

-- 7. Ticket Comments
INSERT IGNORE INTO ticket_comments (id, ticket_id, author_id, message, is_internal, is_auto_generated, comment_type, created_at, updated_at) VALUES
(1, 1, 5, 'Merhaba Ahmet Bey, sorununuzu inceliyorum. Hangi tarayıcı kullanıyorsunuz?', FALSE, FALSE, 'COMMENT', DATE_SUB(NOW(), INTERVAL 90 MINUTE), DATE_SUB(NOW(), INTERVAL 90 MINUTE)),

(2, 1, 1, 'Chrome kullanıyorum, sürüm 120. Aynı sorun Firefox\'ta da var.', FALSE, FALSE, 'COMMENT', DATE_SUB(NOW(), INTERVAL 85 MINUTE), DATE_SUB(NOW(), INTERVAL 85 MINUTE)),

(3, 1, 5, 'Anlıyorum. Cache temizlemeyi denediniz mi? Ctrl+Shift+Delete ile browser cache\'ini temizleyin.', FALSE, FALSE, 'COMMENT', DATE_SUB(NOW(), INTERVAL 80 MINUTE), DATE_SUB(NOW(), INTERVAL 80 MINUTE)),

(4, 2, 6, 'Zeynep Hanım, rapor probleminizi test ortamında reproduce etmeye çalışıyorum. Hangi tarih aralığında rapor oluşturmaya çalışıyorsunuz?', FALSE, FALSE, 'COMMENT', DATE_SUB(NOW(), INTERVAL 45 MINUTE), DATE_SUB(NOW(), INTERVAL 45 MINUTE)),

(5, 2, 2, 'Son 3 aylık dönem için rapor almaya çalışıyorum. Yaklaşık 5000 kayıt var.', FALSE, FALSE, 'COMMENT', DATE_SUB(NOW(), INTERVAL 40 MINUTE), DATE_SUB(NOW(), INTERVAL 40 MINUTE)),

(6, 2, 6, 'Büyük veri setleri için optimizasyon gerekiyor. Development team ile koordine ediyorum.', TRUE, FALSE, 'INTERNAL', DATE_SUB(NOW(), INTERVAL 35 MINUTE), DATE_SUB(NOW(), INTERVAL 35 MINUTE)),

(7, 4, 8, 'Hi Robert, I can help you with the API integration. Can you share your API key and the exact request you\'re making?', FALSE, FALSE, 'COMMENT', DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- 8. Ticket History
INSERT IGNORE INTO ticket_history (id, ticket_id, user_id, field_name, old_value, new_value, change_type, description, created_at, updated_at) VALUES
(1, 1, 5, 'status', 'NEW', 'OPEN', 'STATUS_CHANGED', 'Ticket assigned and status changed to OPEN', DATE_SUB(NOW(), INTERVAL 95 MINUTE), DATE_SUB(NOW(), INTERVAL 95 MINUTE)),
(2, 1, 5, 'assignedAgent', NULL, 'Ali Kaya', 'ASSIGNED', 'Ticket assigned to Ali Kaya', DATE_SUB(NOW(), INTERVAL 95 MINUTE), DATE_SUB(NOW(), INTERVAL 95 MINUTE)),

(3, 2, 6, 'status', 'NEW', 'OPEN', 'STATUS_CHANGED', 'Ticket opened by agent', DATE_SUB(NOW(), INTERVAL 50 MINUTE), DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
(4, 2, 6, 'assignedAgent', NULL, 'Fatma Öztürk', 'ASSIGNED', 'Ticket assigned to Fatma Öztürk', DATE_SUB(NOW(), INTERVAL 50 MINUTE), DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
(5, 2, 6, 'status', 'OPEN', 'IN_PROGRESS', 'STATUS_CHANGED', 'Investigation started', DATE_SUB(NOW(), INTERVAL 40 MINUTE), DATE_SUB(NOW(), INTERVAL 40 MINUTE)),

(6, 3, 7, 'assignedAgent', NULL, 'Can Arslan', 'ASSIGNED', 'Ticket assigned to Can Arslan', DATE_SUB(NOW(), INTERVAL 25 MINUTE), DATE_SUB(NOW(), INTERVAL 25 MINUTE)),
(7, 3, 7, 'status', 'NEW', 'PENDING', 'STATUS_CHANGED', 'Waiting for customer information', DATE_SUB(NOW(), INTERVAL 20 MINUTE), DATE_SUB(NOW(), INTERVAL 20 MINUTE)),

(8, 4, 8, 'assignedAgent', NULL, 'Sarah Johnson', 'ASSIGNED', 'Ticket assigned to Sarah Johnson', DATE_SUB(NOW(), INTERVAL 40 MINUTE), DATE_SUB(NOW(), INTERVAL 40 MINUTE)),
(9, 4, 8, 'status', 'NEW', 'OPEN', 'STATUS_CHANGED', 'API integration investigation started', DATE_SUB(NOW(), INTERVAL 40 MINUTE), DATE_SUB(NOW(), INTERVAL 40 MINUTE));

-- 9. SLA Policies (Opsiyonel - eğer SLA tablosu varsa)
-- INSERT IGNORE INTO sla_policies (id, company_id, name, priority_level, response_time_hours, resolution_time_hours, business_hours_only, created_at, updated_at) VALUES
-- (1, 1, 'Standart SLA', 'NORMAL', 4, 24, TRUE, NOW(), NOW()),
-- (2, 1, 'Yüksek Öncelik SLA', 'HIGH', 2, 8, TRUE, NOW(), NOW()),
-- (3, 1, 'Acil SLA', 'URGENT', 1, 4, FALSE, NOW(), NOW());

-- 10. System Settings (Opsiyonel)
-- INSERT IGNORE INTO system_settings (id, setting_key, setting_value, description, created_at, updated_at) VALUES
-- (1, 'SYSTEM_NAME', 'SmartDesk CRM', 'Sistem adı', NOW(), NOW()),
-- (2, 'SYSTEM_VERSION', '1.0.0', 'Sistem versiyonu', NOW(), NOW()),
-- (3, 'DEFAULT_LANGUAGE', 'tr-TR', 'Varsayılan dil', NOW(), NOW()),
-- (4, 'TICKET_AUTO_ASSIGN', 'true', 'Otomatik ticket atama', NOW(), NOW()),
-- (5, 'EMAIL_NOTIFICATIONS', 'true', 'Email bildirimleri', NOW(), NOW());

-- İstatistik tabloları için View'ler (Opsiyonel)
-- CREATE OR REPLACE VIEW ticket_stats AS
-- SELECT
--     c.id as company_id,
--     c.name as company_name,
--     COUNT(t.id) as total_tickets,
--     COUNT(CASE WHEN t.status = 'OPEN' THEN 1 END) as open_tickets,
--     COUNT(CASE WHEN t.status = 'CLOSED' THEN 1 END) as closed_tickets,
--     COUNT(CASE WHEN t.priority = 'HIGH' THEN 1 END) as high_priority_tickets,
--     AVG(CASE WHEN t.resolved_at IS NOT NULL
--         THEN TIMESTAMPDIFF(HOUR, t.created_at, t.resolved_at) END) as avg_resolution_time
-- FROM companies c
-- LEFT JOIN tickets t ON c.id = t.company_id
-- GROUP BY c.id, c.name;

COMMIT;