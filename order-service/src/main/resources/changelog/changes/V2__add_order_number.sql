alter table orders add column order_number varchar(255);

alter table orders rename column creat_at to create_at;