use flashmart;

-- 密码改为 BCrypt 存储，哈希长度 60，放宽到 100 留余量
alter table user_info
    modify column password varchar(100) not null;

-- 新增角色列，默认普通用户
alter table user_info
    add column role varchar(32) not null default 'USER' after password;

-- 指定管理员账号（按需修改 username）
update user_info set role = 'ADMIN' where username = 'admin';

-- 说明：升级到 BCrypt 后，存量明文密码无法通过校验（matches 返回 false），
-- 受影响的老用户需要重新注册，或走找回密码流程重置密码。
-- 本地开发若想快速可用，最简单的方式是清空老数据后重新注册：
-- delete from user_info;
