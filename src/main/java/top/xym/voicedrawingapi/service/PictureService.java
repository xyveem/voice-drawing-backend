package top.xym.voicedrawingapi.service;

import org.springframework.web.multipart.MultipartFile;
import top.xym.voicedrawingapi.model.vo.PictureDetailVO;
import top.xym.voicedrawingapi.model.vo.PictureVO;

import java.util.List;

public interface PictureService {
    /**
     * 保存画作
     */
    void savePic(Integer userId, String title, MultipartFile file, String voiceCommand, String operationList);

    /**
     * 获取我的画作列表
     */
    List<PictureVO> getMyList(Integer userId);

    /**
     * 获取画作详情，校验归属权
     */
    PictureDetailVO getDetail(Integer picId, Integer loginUserId);

    /**
     * 删除画作（逻辑删除），校验归属权
     */
    void delPic(Integer picId, Integer loginUserId);
}