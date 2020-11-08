package com.jit.webchat.controller;

import com.jit.webchat.bean.UrlType;
import com.jit.webchat.bean.User;
import com.jit.webchat.securityutils.RSAUtils;
import com.jit.webchat.service.UserService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Controller
public class ChatController {
    static {
        try {
            RSAUtils.generateKeyToFile("chat.pub", "chat.pri");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/register")
    public @ResponseBody String register(User user) {
        if (user.getUsername() == "") {
            return "用户名不能为空";
        }
        String msg = "";
        if (userService.existsUsername(user.getUsername())) {
            msg = "用户名已存在";
        } else {
            log.info(user.getPassword());
            userService.saveUser(user);
            msg = "注册成功";
        }
        return msg;
    }

    @PostMapping("/login")
    public @ResponseBody String login(HttpSession session, String username, String password) {
        if (username == "" || password == "") {
            return "用户名或密码不能为空";
        }
        String msg = "";
        if (userService.login(username, password) != null) {
            msg = "登录成功";
            session.setAttribute("username", username);
        } else {
            msg = "用户名或密码错误";
        }
        return msg;
    }

    SimpleDateFormat sdf = new SimpleDateFormat("/yyyy/MM/dd");
    @PostMapping("/upload")
    public @ResponseBody UrlType upload(MultipartFile file, HttpServletRequest req) {
        String format = sdf.format(new Date());
        String realPath = req.getServletContext().getRealPath("/temp") + format;
        File folder = new File(realPath);
        System.out.println(folder);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        // 获取文件原始名后缀,UUID使得上传文件保存不会重名
        String oldName = file.getOriginalFilename();
        String fileSuffix = oldName.substring(oldName.lastIndexOf("."));
        String newName = UUID.randomUUID().toString() + fileSuffix;
        try {
            file.transferTo(new File(folder, newName));
            String url =  req.getScheme()
                    + "://"
                    + req.getServerName()
                    + ":"
                    + req.getServerPort()
                    + "/webchat/temp"
                    + format
                    + "/"
                    + newName;
            if (".jpg".equals(fileSuffix) || ".png".equals(fileSuffix) || ".jpeg".equals(fileSuffix) || ".gif".equals(fileSuffix)){
                return new UrlType(url, "img");
            }
            return new UrlType(url, "other", oldName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new UrlType("error", "error");
    }

    @GetMapping("/logout")
    public @ResponseBody String logout(HttpServletRequest req) {
        req.getSession().invalidate();
        return "logout success";
    }

    @PostMapping("/getPrivateKey")
    public @ResponseBody String returnPrivateKey() throws Exception {
        String privateKeyString = FileUtils.readFileToString(new File("chat.pri"), Charset.defaultCharset());
        return privateKeyString;
    }

    @PostMapping("/getPublicKey")
    public @ResponseBody String returnPublicKey() throws Exception {
        String publicKeyString = FileUtils.readFileToString(new File("chat.pub"), Charset.defaultCharset());
        return publicKeyString;
    }
}
