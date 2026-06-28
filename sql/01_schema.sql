create database if not exists flashmart
    default character set utf8mb4
    collate utf8mb4_unicode_ci;

use flashmart;

create table if not exists user_info (
    id bigint primary key auto_increment,
    username varchar(32) not null,
    email varchar(128) not null,
    password varchar(64) not null,
    create_time datetime not null default current_timestamp,
    update_time datetime not null default current_timestamp on update current_timestamp,
    unique key uk_user_info_username (username),
    unique key uk_user_info_email (email)
) engine = InnoDB default charset = utf8mb4;

create table if not exists products (
    id int primary key auto_increment,
    name varchar(128) not null,
    image varchar(512) null,
    category varchar(64) not null,
    original_price decimal(10, 2) not null default 0.00,
    sale_price decimal(10, 2) not null default 0.00,
    stock int not null default 0,
    sold int not null default 0,
    is_hot tinyint(1) not null default 0,
    is_seckill tinyint(1) not null default 0,
    limit_per_user int not null default 0,
    status tinyint not null default 1,
    seckill_start_time datetime null,
    seckill_end_time datetime null,
    create_time datetime not null default current_timestamp,
    update_time datetime not null default current_timestamp on update current_timestamp,
    key idx_products_category (category),
    key idx_products_status (status),
    key idx_products_seckill (is_seckill, seckill_start_time, seckill_end_time)
) engine = InnoDB default charset = utf8mb4;

create table if not exists product_detail (
    id int primary key auto_increment,
    product_id int not null,
    description text null,
    specs json null,
    create_time datetime not null default current_timestamp,
    update_time datetime not null default current_timestamp on update current_timestamp,
    unique key uk_product_detail_product_id (product_id),
    key idx_product_detail_product_id (product_id)
) engine = InnoDB default charset = utf8mb4;

create table if not exists product_images (
    id int primary key auto_increment,
    product_id int not null,
    url varchar(512) not null,
    sort int not null default 0,
    is_main tinyint(1) not null default 0,
    create_time datetime not null default current_timestamp,
    key idx_product_images_product_id (product_id, sort)
) engine = InnoDB default charset = utf8mb4;

create table if not exists product_reviews (
    id int primary key auto_increment,
    product_id int not null,
    user_id bigint not null,
    order_id bigint null,
    rating tinyint not null,
    content varchar(1000) null,
    images json null,
    is_visible tinyint(1) not null default 1,
    create_time datetime not null default current_timestamp,
    update_time datetime not null default current_timestamp on update current_timestamp,
    key idx_product_reviews_product_id (product_id, is_visible, create_time),
    key idx_product_reviews_user_id (user_id)
) engine = InnoDB default charset = utf8mb4;

create table if not exists shopping_cart (
    id int primary key auto_increment,
    user_id bigint not null,
    product_id int not null,
    quantity int not null default 1,
    selected tinyint(1) not null default 1,
    create_time datetime not null default current_timestamp,
    update_time datetime not null default current_timestamp on update current_timestamp,
    unique key uk_shopping_cart_user_product (user_id, product_id),
    key idx_shopping_cart_user_id (user_id),
    key idx_shopping_cart_product_id (product_id)
) engine = InnoDB default charset = utf8mb4;

create table if not exists orders (
    id bigint primary key auto_increment,
    order_no varchar(64) not null,
    user_id bigint not null,
    status varchar(32) not null,
    product_amount decimal(10, 2) not null default 0.00,
    shipping_fee decimal(10, 2) not null default 0.00,
    discount_amount decimal(10, 2) not null default 0.00,
    payable_amount decimal(10, 2) not null default 0.00,
    receiver_name varchar(64) not null,
    receiver_phone varchar(32) not null,
    receiver_address varchar(512) not null,
    pay_expire_time datetime not null,
    close_deadline_time datetime not null,
    pay_time datetime null,
    cancel_time datetime null,
    create_time datetime not null default current_timestamp,
    update_time datetime not null default current_timestamp on update current_timestamp,
    unique key uk_orders_order_no (order_no),
    key idx_orders_user_create_time (user_id, create_time),
    key idx_orders_status (status),
    key idx_orders_status_close_deadline (status, close_deadline_time)
) engine = InnoDB default charset = utf8mb4;

create table if not exists order_items (
    id bigint primary key auto_increment,
    order_id bigint not null,
    product_id bigint not null,
    product_name varchar(128) not null,
    product_image varchar(512) null,
    product_price decimal(10, 2) not null default 0.00,
    quantity int not null default 1,
    subtotal decimal(10, 2) not null default 0.00,
    create_time datetime not null default current_timestamp,
    key idx_order_items_order_id (order_id),
    key idx_order_items_product_id (product_id)
) engine = InnoDB default charset = utf8mb4;
