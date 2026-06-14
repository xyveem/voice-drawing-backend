package top.xym.voicedrawingapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.xym.voicedrawingapi.common.cache.RequestContext;
import top.xym.voicedrawingapi.common.result.Result;
import top.xym.voicedrawingapi.model.vo.PictureDetailVO;
import top.xym.voicedrawingapi.model.vo.PictureVO;
import top.xym.voicedrawingapi.service.PictureService;

import java.util.List;

@RestController
@RequestMapping("/pictures")
@Tag(name = "绘图作品接口")
@AllArgsConstructor
public class PictureController {
    private final PictureService pictureService;

    @PostMapping
    @Operation(summary = "保存手绘作品")
    public Result<?> savePicture(@RequestParam("file") MultipartFile file,
                                 @RequestParam("title") String title,
                                 @RequestParam(value = "voiceCommand", required = false) String voiceCommand) {
        Integer userId = RequestContext.getUserId();
        System.out.println("userId" + userId);
        pictureService.savePic(userId, title, file, voiceCommand);
        return Result.ok();
    }

    @GetMapping("/my")
    @Operation(summary = "获取当前用户全部画作")
    public Result<List<PictureVO>> myPicList() {
        Integer userId = RequestContext.getUserId();
        return Result.ok(pictureService.getMyList(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "画作详情（归属校验）")
    public Result<PictureDetailVO> detail(@PathVariable Integer id) {
        Integer userId = RequestContext.getUserId();
        return Result.ok(pictureService.getDetail(id, userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除自己的画作（逻辑删除）")
    public Result<?> delete(@PathVariable Integer id) {
        Integer userId = RequestContext.getUserId();
        pictureService.delPic(id, userId);
        return Result.ok();
    }
}