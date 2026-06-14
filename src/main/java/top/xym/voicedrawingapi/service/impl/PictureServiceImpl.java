package top.xym.voicedrawingapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.xym.voicedrawingapi.common.exception.ErrorCode;
import top.xym.voicedrawingapi.common.exception.ServerException;
import top.xym.voicedrawingapi.mapper.PictureMapper;
import top.xym.voicedrawingapi.model.entity.Picture;
import top.xym.voicedrawingapi.model.vo.PictureDetailVO;
import top.xym.voicedrawingapi.model.vo.PictureVO;
import top.xym.voicedrawingapi.service.PictureService;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    private final PictureMapper pictureMapper;

    @Override
    public void savePic(Integer userId, String title, MultipartFile file, String voiceCommand) {
        try {
            Picture picture = new Picture();
            picture.setUserId(userId);
            picture.setTitle(title);
            picture.setContent(file.getBytes());
            picture.setVoiceCommand(voiceCommand);
            picture.setDeleteFlag(0);
            baseMapper.insert(picture);
        } catch (IOException e) {
            log.error("图片读取二进制失败", e);
            throw new ServerException("图片上传异常");
        }
    }

    @Override
    public List<PictureVO> getMyList(Integer userId) {
        return pictureMapper.selectMyPicList(userId);
    }

    @Override
    public PictureDetailVO getDetail(Integer picId, Integer loginUserId) {
        PictureDetailVO detail = pictureMapper.selectDetailById(picId);
        if (detail == null) {
            throw new ServerException(ErrorCode.DATA_NOT_EXIST);
        }
        // 归属校验：只能查看自己的画作
        if (!detail.getUserId().equals(loginUserId)) {
            throw new ServerException(ErrorCode.NO_PERMISSION);
        }
        return detail;
    }

    @Override
    public void delPic(Integer picId, Integer loginUserId) {
        PictureDetailVO detail = pictureMapper.selectDetailById(picId);
        if (detail == null) {
            throw new ServerException(ErrorCode.DATA_NOT_EXIST);
        }
        if (!detail.getUserId().equals(loginUserId)) {
            throw new ServerException(ErrorCode.NO_PERMISSION);
        }
        // 手动构造更新条件，强制设置delete_flag=1，绕开MP逻辑删除拦截
        LambdaUpdateWrapper<Picture> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Picture::getId, picId);
        wrapper.set(Picture::getDeleteFlag, 1);
        baseMapper.update(null, wrapper);
    }
}