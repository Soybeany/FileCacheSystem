package com.soybeany.system.cache.demo.app.controller;

import com.soybeany.download.DataSupplier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * @author Soybeany
 * @date 2020/12/11
 */
@RestController
class UserApiController {

    // ********************标准API********************

    @GetMapping("/getContentByFileId/{token}")
    public void getList(@PathVariable String token, HttpServletRequest request, HttpServletResponse response) {
        File file = new File("C:\\Users\\soybeany\\Desktop\\windows-win32-direct3dhlsl.pdf");
        DataSupplier.builder().file(file, true).enableRandomAccess(request, true).start(response);
    }
}
