package top.xym.voicedrawingapi.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.xym.voicedrawingapi.model.entity.User;

public interface UserMapper extends BaseMapper<User> {

    default User getByPhone(String phone){
        return this.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }
}
