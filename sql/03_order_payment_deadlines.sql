use flashmart;

alter table orders
    add column pay_expire_time datetime null after receiver_address,
    add column close_deadline_time datetime null after pay_expire_time;

update orders
set pay_expire_time = date_add(create_time, interval 15 minute),
    close_deadline_time = date_add(create_time, interval 16 minute)
where pay_expire_time is null
   or close_deadline_time is null;

alter table orders
    modify pay_expire_time datetime not null,
    modify close_deadline_time datetime not null;

create index idx_orders_status_close_deadline
    on orders (status, close_deadline_time);
