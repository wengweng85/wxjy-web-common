package com.insigma.mvc.controller.common.gallery;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author: liuds
 * @date: 2018/3/15
 */
@Controller
public class GalleryController {
    @RequestMapping("/common/gallery")
    public String gallery(){
        return "common/gallery/gallery";
    }
}
