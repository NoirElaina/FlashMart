use flashmart;

-- 秒杀活动表（库存与普通库存分开管理）
create table if not exists seckill_activity (
    id            bigint primary key auto_increment,
    product_id    int          not null,
    seckill_price decimal(10,2) not null,
    stock         int          not null,
    limit_per_user int         not null default 1,
    start_time    datetime     not null,
    end_time      datetime     not null,
    status        tinyint      not null default 1,  -- 1上线 0下线
    create_time   datetime     not null default current_timestamp,
    update_time   datetime     not null default current_timestamp on update current_timestamp,
    key idx_seckill_time (start_time, end_time),
    key idx_seckill_product (product_id)
) engine=InnoDB default charset=utf8mb4;

-- 秒杀订单表：用唯一索引(activity_id+user_id)从 DB 层兜底"一人一单"
create table if not exists seckill_order (
    id           bigint primary key auto_increment,
    activity_id  bigint   not null,
    user_id      bigint   not null,
    order_id     bigint   null,        -- 关联正式订单
    status       tinyint  not null default 0,  -- 0排队 1成功 2失败
    create_time  datetime not null default current_timestamp,
    unique key uk_seckill_user (activity_id, user_id)
) engine=InnoDB default charset=utf8mb4;
