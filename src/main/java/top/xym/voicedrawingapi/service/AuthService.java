package top.xym.voicedrawingapi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.xym.voicedrawingapi.model.entity.User;
import top.xym.voicedrawingapi.model.vo.UserLoginVO;

public interface AuthService extends IService<User> {

    /**
     * 登录
     *
     * @param phone 电话
     * @param code 验证码
     * @return {@link UserLoginVO}
     */
    UserLoginVO loginByPhone(String phone, String code);
}