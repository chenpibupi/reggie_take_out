package com.chenwut.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chenwut.reggie.common.Result;
import com.chenwut.reggie.entity.User;
import com.chenwut.reggie.service.UserService;
import com.chenwut.reggie.utils.MailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/sendMsg")
    public Result<String> sendMsg(@RequestBody User user, HttpSession session) throws MessagingException {
        String email = user.getPhone();
        if (!email.isEmpty()) {
            //随机生成一个验证码
            String code = MailUtils.achieveCode();
            log.info(code);
            //这里的phone其实就是邮箱，code是我们生成的验证码
//            MailUtils.sendTestMail(email, code);

            //验证码存到session，方便后面拿出来比对
//            session.setAttribute(email, code);

            //将验证码存入Redis,设置过期时间为5分钟
            redisTemplate.opsForValue().set(email,code,5, TimeUnit.MINUTES);

            //存入的格式=>  88888.qq.com ：ABCD
            return Result.success("验证码发送成功");
        }
        return Result.error("验证码发送失败");
    }

    @PostMapping("/login")
    public Result<User> login(@RequestBody Map map, HttpSession session) {
        log.info(map.toString());
        //获取邮箱
        String email = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //从session中获取验证码
//        String verificationCode = session.getAttribute(email).toString();

        //从Redis中获取验证码
        String verificationCode = redisTemplate.opsForValue().get(email).toString();
        //比较这用户输入的验证码和session中存的验证码是否一致
        if (code != null && code.equals(verificationCode)) {
            //如果输入正确，判断一下当前用户是否存在
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            //判断依据是从数据库中查询是否有其邮箱
            queryWrapper.eq(User::getPhone, email);
            User user = userService.getOne(queryWrapper);
            //如果不存在，则创建一个，存入数据库
            if (user == null) {
                user = new User();
                user.setPhone(email);
                userService.save(user);
                user.setName("用户" + verificationCode);
            }
            //存个session，表示登录状态
            session.setAttribute("user", user.getId());
            log.info("UserId为:{}", user.getId());

            //登录成功，删除Redis中缓存的验证码数据
            redisTemplate.delete(email);
            //并将其作为结果返回
            return Result.success(user);
        }
        return Result.error("登录失败");
    }

    @PostMapping("/loginout")
    public Result<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("user");
        return Result.success("退出成功");
    }
}
