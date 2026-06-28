use flashmart;

insert into user_info (id, username, email, password, create_time, update_time) values
    (1, 'demo', 'demo@flashmart.local', '123456', now(), now()),
    (2, 'alice', 'alice@flashmart.local', '123456', now(), now())
on duplicate key update
    username = values(username),
    email = values(email),
    password = values(password),
    update_time = now();

insert into products (
    id, name, image, category, original_price, sale_price, stock, sold,
    is_hot, is_seckill, limit_per_user, status, seckill_start_time, seckill_end_time,
    create_time, update_time
) values
    (1, 'iPhone 16 Pro 256GB', 'https://picsum.photos/seed/iphone16/600/600', '手机数码', 9999.00, 7999.00, 100, 320, 1, 1, 1, 1, '2026-04-24 10:00:00', '2026-04-24 22:00:00', now(), now()),
    (2, 'Xiaomi TV 65 Inch 4K', 'https://picsum.photos/seed/xiaomitv/600/600', '家用电器', 3999.00, 2499.00, 50, 180, 0, 0, 2, 1, null, null, now(), now()),
    (3, 'Nike Air Max Sneakers', 'https://picsum.photos/seed/nikeshoe/600/600', '服饰鞋包', 899.00, 499.00, 199, 561, 1, 0, 1, 1, null, null, now(), now()),
    (4, 'Dyson V15 Vacuum Cleaner', 'https://picsum.photos/seed/dysonv15/600/600', '家用电器', 4999.00, 3299.00, 30, 210, 1, 0, 1, 1, null, null, now(), now()),
    (5, 'Sony WH-1000XM5 Headphones', 'https://picsum.photos/seed/sonyxm5/600/600', '手机数码', 2999.00, 1999.00, 80, 430, 0, 0, 2, 1, null, null, now(), now()),
    (6, 'Essence Mascara Lash Princess', 'https://picsum.photos/seed/flashmart-essence-mascara-lash-princess-1/900/900', '美妆护肤', 11.16, 9.99, 99, 932, 0, 0, 5, 1, null, null, now(), now()),
    (7, 'Eyeshadow Palette with Mirror', 'https://picsum.photos/seed/flashmart-eyeshadow-palette-with-mirror-1/900/900', '美妆护肤', 24.43, 19.99, 34, 110, 0, 0, 1, 1, null, null, now(), now()),
    (8, 'Powder Canister', 'https://picsum.photos/seed/flashmart-powder-canister-1/900/900', '美妆护肤', 16.63, 14.99, 89, 1235, 1, 0, 5, 1, null, null, now(), now())
on duplicate key update
    name = values(name),
    image = values(image),
    category = values(category),
    original_price = values(original_price),
    sale_price = values(sale_price),
    stock = values(stock),
    sold = values(sold),
    is_hot = values(is_hot),
    is_seckill = values(is_seckill),
    limit_per_user = values(limit_per_user),
    status = values(status),
    seckill_start_time = values(seckill_start_time),
    seckill_end_time = values(seckill_end_time),
    update_time = now();

insert into product_detail (id, product_id, description, specs, create_time, update_time) values
    (1, 1, 'Flagship phone with high performance and pro camera system.', '{"brand":"Apple","storage":"256GB","color":"Natural Titanium"}', now(), now()),
    (2, 2, 'Large 4K smart TV for home entertainment.', '{"brand":"Xiaomi","size":"65 inch","resolution":"4K"}', now(), now()),
    (3, 3, 'Comfortable everyday running sneakers.', '{"brand":"Nike","series":"Air Max","scene":"Running"}', now(), now()),
    (4, 4, 'Cordless vacuum cleaner for deep home cleaning.', '{"brand":"Dyson","model":"V15","type":"Cordless"}', now(), now()),
    (5, 5, 'Noise cancelling wireless headphones.', '{"brand":"Sony","model":"WH-1000XM5","connection":"Bluetooth"}', now(), now()),
    (6, 6, 'Popular mascara for daily makeup.', '{"brand":"Essence","type":"Mascara"}', now(), now()),
    (7, 7, 'Eyeshadow palette with mirror.', '{"type":"Eyeshadow","feature":"Mirror"}', now(), now()),
    (8, 8, 'Lightweight powder canister for makeup.', '{"type":"Powder","scene":"Makeup"}', now(), now())
on duplicate key update
    description = values(description),
    specs = values(specs),
    update_time = now();

insert into product_images (id, product_id, url, sort, is_main, create_time) values
    (1, 1, 'https://picsum.photos/seed/iphone16-main/900/900', 1, 1, now()),
    (2, 1, 'https://picsum.photos/seed/iphone16-side/900/900', 2, 0, now()),
    (3, 2, 'https://picsum.photos/seed/xiaomitv-main/900/900', 1, 1, now()),
    (4, 2, 'https://picsum.photos/seed/xiaomitv-room/900/900', 2, 0, now()),
    (5, 3, 'https://picsum.photos/seed/nikeshoe-main/900/900', 1, 1, now()),
    (6, 3, 'https://picsum.photos/seed/nikeshoe-side/900/900', 2, 0, now()),
    (7, 4, 'https://picsum.photos/seed/dyson-main/900/900', 1, 1, now()),
    (8, 4, 'https://picsum.photos/seed/dyson-detail/900/900', 2, 0, now()),
    (9, 5, 'https://picsum.photos/seed/sony-main/900/900', 1, 1, now()),
    (10, 5, 'https://picsum.photos/seed/sony-case/900/900', 2, 0, now()),
    (11, 6, 'https://picsum.photos/seed/mascara-main/900/900', 1, 1, now()),
    (12, 6, 'https://picsum.photos/seed/mascara-detail/900/900', 2, 0, now()),
    (13, 7, 'https://picsum.photos/seed/eyeshadow-main/900/900', 1, 1, now()),
    (14, 7, 'https://picsum.photos/seed/eyeshadow-open/900/900', 2, 0, now()),
    (15, 8, 'https://picsum.photos/seed/powder-main/900/900', 1, 1, now()),
    (16, 8, 'https://picsum.photos/seed/powder-detail/900/900', 2, 0, now())
on duplicate key update
    product_id = values(product_id),
    url = values(url),
    sort = values(sort),
    is_main = values(is_main);

insert into product_reviews (id, product_id, user_id, order_id, rating, content, images, is_visible, create_time, update_time) values
    (1, 1, 1, null, 5, 'Fast and smooth. The camera is impressive.', '[]', 1, now(), now()),
    (2, 1, 2, null, 4, 'Good phone, shipping was quick.', '[]', 1, now(), now()),
    (3, 3, 1, null, 5, 'Comfortable shoes for daily walking.', '[]', 1, now(), now()),
    (4, 5, 2, null, 5, 'Noise cancellation works well.', '[]', 1, now(), now())
on duplicate key update
    product_id = values(product_id),
    user_id = values(user_id),
    rating = values(rating),
    content = values(content),
    images = values(images),
    is_visible = values(is_visible),
    update_time = now();

insert into shopping_cart (id, user_id, product_id, quantity, selected, create_time, update_time) values
    (1, 1, 2, 1, 1, now(), now()),
    (2, 1, 3, 2, 1, now(), now())
on duplicate key update
    product_id = values(product_id),
    quantity = values(quantity),
    selected = values(selected),
    update_time = now();
