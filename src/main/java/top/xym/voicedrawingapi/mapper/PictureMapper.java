package top.xym.voicedrawingapi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.xym.voicedrawingapi.model.entity.Picture;
import top.xym.voicedrawingapi.model.vo.PictureDetailVO;
import top.xym.voicedrawingapi.model.vo.PictureVO;

import java.util.List;

public interface PictureMapper extends BaseMapper<Picture> {

    @Select("SELECT id, title, TO_BASE64(content) contentBase64, voice_command voiceCommand, create_time createTime " +
            "FROM drawing WHERE user_id = #{userId} AND delete_flag = 0 ORDER BY create_time DESC")
    List<PictureVO> selectMyPicList(@Param("userId") Integer userId);

    @Select("SELECT id, user_id userId, title, TO_BASE64(content) contentBase64, voice_command voiceCommand, create_time createTime " +
            "FROM drawing WHERE id = #{id} AND delete_flag = 0")
    PictureDetailVO selectDetailById(@Param("id") Integer id);
}