insert into brand (name, slug, city, state, created_at)
values ('Studio Carga', 'studio-carga', 'João Monlevade', 'MG', now());

insert into professional (brand_id, name, is_active, created_at)
select id, 'Profissional Carga', true, now() from brand where slug = 'studio-carga';

insert into service (professional_id, name, duration_minutes, price, is_active, created_at)
select p.id, 'Corte', 30, 50.00, true, now()
from professional p join brand b on b.id = p.brand_id where b.slug = 'studio-carga';

insert into professional_schedule (professional_id, day_of_week, start_time, end_time, created_at)
select p.id, d.day, '08:00', '20:00', now()
from professional p join brand b on b.id = p.brand_id
cross join (values ('MONDAY'), ('TUESDAY'), ('WEDNESDAY'), ('THURSDAY'), ('FRIDAY'), ('SATURDAY'), ('SUNDAY')) as d(day)
where b.slug = 'studio-carga';

insert into client (phone, name, created_at)
select '3190000' || lpad(i::text, 4, '0'), 'Cliente ' || i, now()
from generate_series(1, 100) as i;

insert into client_session (client_id, otp_code, otp_expires_at, otp_verified_at, session_token, session_expires_at, created_at)
select c.id, '000000', now(), now(),
       ('00000000-0000-4000-8000-' || lpad(substring(c.phone from 8)::int::text, 12, '0'))::uuid,
       now() + interval '30 days', now()
from client c where c.phone like '3190000%';
