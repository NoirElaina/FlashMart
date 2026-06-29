use flashmart;

-- 独立分类表（替代商品表里散落的 category 字符串）
create table if not exists category (
    id          int primary key auto_increment,
    name        varchar(64) not null,
    sort        int         not null default 0,
    status      tinyint     not null default 1,  -- 1启用 0停用
    create_time datetime    not null default current_timestamp,
    update_time datetime    not null default current_timestamp on update current_timestamp,
    unique key uk_category_name (name)
) engine=InnoDB default charset=utf8mb4;

-- 商品表增加分类外键列（保留原 category 名称做冗余展示字段）
alter table products add column category_id int null after category;

-- 用现有商品里的分类名初始化分类表
insert ignore into category(name)
select distinct category from products where category is not null and category <> '';

-- 回填商品的 category_id
update products p
join category c on p.category = c.name
set p.category_id = c.id;

create index idx_products_category_id on products(category_id);
