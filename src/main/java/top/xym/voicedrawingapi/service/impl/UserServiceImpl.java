package top.xym.voicedrawingapi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.xym.voicedrawingapi.common.cache.RequestContext;
import top.xym.voicedrawingapi.common.exception.ErrorCode;
import top.xym.voicedrawingapi.common.exception.ServerException;
import top.xym.voicedrawingapi.convert.UserConvert;
import top.xym.voicedrawingapi.mapper.UserMapper;
import top.xym.voicedrawingapi.model.entity.User;
import top.xym.voicedrawingapi.model.vo.UserInfoVO;
import top.xym.voicedrawingapi.service.UserService;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public UserInfoVO userInfo() {
        Integer userId = RequestContext.getUserId();
        // 查询数据库
        User user = baseMapper.selectById(userId);
        if (user == null) {
            log.error("⽤户不存在, userId: {}", userId);
            throw new ServerException(ErrorCode.USER_NOT_EXIST);
        }
        UserInfoVO userInfoVO = UserConvert.INSTANCE.convert(user);

        return userInfoVO;
    }
}