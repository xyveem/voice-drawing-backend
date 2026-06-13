package top.xym.voicedrawingapi.common.interceptor;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import top.xym.voicedrawingapi.common.cache.RequestContext;
import top.xym.voicedrawingapi.common.cache.TokenStoreCache;
import top.xym.voicedrawingapi.common.constant.Constant;
import top.xym.voicedrawingapi.common.exception.ErrorCode;
import top.xym.voicedrawingapi.common.exception.ServerException;
import top.xym.voicedrawingapi.model.vo.UserLoginVO;
import top.xym.voicedrawingapi.service.AuthService;
import top.xym.voicedrawingapi.utils.JwtUtil;

@Slf4j
@AllArgsConstructor
@Component
public class TokenInterceptor implements HandlerInterceptor {
    private final TokenStoreCache tokenStoreCache;
    private final AuthService authService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取token
        String accessToken = JwtUtil.getAccessToken(request);
        if (StringUtils.isBlank(accessToken)) {
            throw new ServerException(ErrorCode.UNAUTHORIZED);
        }
        // 校验token
        if (!JwtUtil.validate(accessToken)) {
            throw new ServerException(ErrorCode.UNAUTHORIZED);
        }
        // 验证⽤户登录状态是否正常
        UserLoginVO user = tokenStoreCache.getUser(accessToken);
        if (ObjectUtils.isEmpty(user)) {
            throw new ServerException(ErrorCode.LOGIN_STATUS_EXPIRE);
        }

        // 保存⽤户id到上下⽂
        RequestContext.put(Constant.USER_ID, user.getUserId());
        return true;
    }
}