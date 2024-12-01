package com.yupi.springbootinit.mapper;

import com.yupi.springbootinit.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Vizzini
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2024-11-07 12:40:14
* @Entity com.yupi.springbootinit.model.entity.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




