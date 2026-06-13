package top.xym.voicedrawingapi.convert;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import top.xym.voicedrawingapi.model.entity.User;
import top.xym.voicedrawingapi.model.vo.UserInfoVO;

@Mapper
public interface UserConvert {
    //获取 UserConvert 实例，由 MapStruct ⾃动⽣成实现类并提供实例
    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);
    //将 User 对象转换为 UserInfoVO 对象
    UserInfoVO convert(User user);
}