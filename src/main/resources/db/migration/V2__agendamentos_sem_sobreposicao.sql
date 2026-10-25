create extension if not exists btree_gist;

alter table appointment add constraint appointment_no_overlap exclude using gist (
    professional_id with =,
    tsrange(scheduled_date + scheduled_time, scheduled_date + scheduled_time + duration_minutes * interval '1 minute') with &&
) where (status <> 'CANCELLED');
