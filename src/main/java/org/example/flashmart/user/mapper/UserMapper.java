package org.example.flashmart.user.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.flashmart.user.model.dataobject.UserDO;

@Mapper
public interface UserMapper {
    /**
     * 登录支持用户名或邮箱，因此 account 同时匹配 username / email。
     */
    @Select("""
            SELECT id, username, email, password, role,
                   create_time AS createdTime,
                   update_time AS updatedTime
            FROM user_info
            WHERE username = #{account} OR email = #{account}
            """)
    UserDO selectByAccount(@Param("account") String account);

    /**
     * 注册前检查用户名是否已经存在。
     */
    @Select("""
            SELECT id, username, email, password, role,
                   create_time AS createdTime,
                   update_time AS updatedTime
            FROM user_info
            WHERE username = #{username}
            """)
    UserDO selectByUsername(String username);

    /**
     * 根据登录态中的用户 ID 查询用户基础信息。
     */
    @Select("""
            SELECT id, username, email, password, role,
                   create_time AS createdTime,
                   update_time AS updatedTime
            FROM user_info
            WHERE id = #{id}
            """)
    UserDO selectById(@Param("id") Integer id);

    /**
     * 注册唯一性校验：用户名不能重复。
     */
    @Select("SELECT COUNT(1) FROM user_info WHERE username = #{username}")
    int countByUsername(@Param("username") String username);

    /**
     * 注册唯一性校验：邮箱不能重复。
     */
    @Select("SELECT COUNT(1) FROM user_info WHERE email = #{email}")
    int countByEmail(@Param("email") String email);

    /**
     * 登录前快速判断账号是否存在，避免后续空对象处理分散到业务代码里。
     */
    @Select("SELECT COUNT(1) FROM user_info WHERE username = #{account} OR email = #{account}")
    int countByAccount(@Param("account") String account);

    /**
     * 密码须在 Service 层完成 BCrypt 加密后再写入数据库。
     */
    @Insert("INSERT INTO user_info (username, email, password, role, create_time, update_time) "
            + "VALUES (#{username}, #{email}, #{password}, #{role}, NOW(), NOW())")
    int insertUser(@Param("username") String username,
                   @Param("email") String email,
                   @Param("password") String password,
                   @Param("role") String role);
}
