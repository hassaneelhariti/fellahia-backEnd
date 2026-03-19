-- V2__seed_data.sql
-- Development seed data — do NOT run in production

-- Demo Fellah user (password: password123)
INSERT INTO users (id, full_name, phone, email, password, role, verified)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'أحمد الفلاح',
    '0600000001',
    'fellah@demo.ma',
    '$2a$12$LqB0N5VUXQRP.8.cqM5EZ.X8LpHfIvxoEh6qo3DY3Nip0UjSbVGmy',
    'FELLAH',
    TRUE
) ON CONFLICT DO NOTHING;

INSERT INTO fellah_profiles (user_id, balance, rib)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    1000.00,
    'MA64 0112 0003 2001 0000 1234 5678'
) ON CONFLICT DO NOTHING;

-- Demo Lawyer user (password: password123)
INSERT INTO users (id, full_name, phone, email, password, role, verified)
VALUES (
    'b0000000-0000-0000-0000-000000000002',
    'الأستاذ العمراني',
    '0600000002',
    'avocat@demo.ma',
    '$2a$12$LqB0N5VUXQRP.8.cqM5EZ.X8LpHfIvxoEh6qo3DY3Nip0UjSbVGmy',
    'AVOCAT',
    TRUE
) ON CONFLICT DO NOTHING;

INSERT INTO lawyer_profiles (user_id, bar_number, specialization, region, rating)
VALUES (
    'b0000000-0000-0000-0000-000000000002',
    'BAR-AGA-2021-042',
    'القانون الفلاحي وحقوق المياه',
    'سوس ماسة',
    4.5
) ON CONFLICT DO NOTHING;
