package top.xym.voicedrawingapi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.xym.voicedrawingapi.model.entity.User;
import top.xym.voicedrawingapi.model.vo.UserInfoVO;

public interface UserService extends IService<User> {
    /**
     * ⽤户信息
     *
     * @return {@link UserInfoVO}
     */
    UserInfoVO userInfo();
}
