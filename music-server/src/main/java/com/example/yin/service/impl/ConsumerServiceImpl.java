package com.example.yin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yin.common.R;
import com.example.yin.controller.MinioUploadController;
import com.example.yin.mapper.ConsumerMapper;
import com.example.yin.model.domain.Consumer;
import com.example.yin.model.request.ConsumerRequest;
import com.example.yin.service.ConsumerService;
import com.example.yin.service.UserActionLogService;
import com.example.yin.utils.IpUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.example.yin.constant.Constants.SALT;

@Service
public class ConsumerServiceImpl extends ServiceImpl<ConsumerMapper, Consumer>
        implements ConsumerService {

    @Autowired
    private ConsumerMapper consumerMapper;

    @Autowired
    private UserActionLogService userActionLogService;

    private static final String ACTION_REGISTER = "REGISTER";
    private static final String ACTION_LOGIN = "LOGIN";
    private static final String ACTION_UPDATE_PROFILE = "UPDATE_PROFILE";
    private static final String ACTION_UPDATE_PASSWORD = "UPDATE_PASSWORD";
    private static final String ACTION_UPDATE_AVATAR = "UPDATE_AVATAR";


    /**
     * 新增用户
     */
    @Override
    public R addUser(ConsumerRequest registryRequest) {
        if (this.existUser(registryRequest.getUsername())) {
            return R.warning("用户名已注册");
        }
        Consumer consumer = new Consumer();
        BeanUtils.copyProperties(registryRequest, consumer);
        //MD5加密
        String password = DigestUtils.md5DigestAsHex((SALT + registryRequest.getPassword()).getBytes(StandardCharsets.UTF_8));
        consumer.setPassword(password);

        if (StringUtils.isBlank(consumer.getPhoneNum())) {
            consumer.setPhoneNum(null);
        }
        if ("".equals(consumer.getEmail())) {
            consumer.setEmail(null);
        }
        consumer.setAvator("img/avatorImages/user.jpg");
        try {
            QueryWrapper<Consumer> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("email",consumer.getEmail());
            Consumer one = consumerMapper.selectOne(queryWrapper);
            if (one!=null){
                return R.fatal("邮箱不允许重复");
            }
            if (consumerMapper.insert(consumer) > 0) {
                recordUserAction(consumer.getId(), ACTION_REGISTER, "用户注册成功");
                return R.success("注册成功");
            } else {
                return R.error("注册失败");
            }
        } catch (DuplicateKeyException e) {
            return R.fatal(e.getMessage());
        }
    }

    @Override
    public R updateUserMsg(ConsumerRequest updateRequest) {
        Consumer consumer = new Consumer();
        BeanUtils.copyProperties(updateRequest, consumer);
        if (consumerMapper.updateById(consumer) > 0) {
            recordUserAction(consumer.getId(), ACTION_UPDATE_PROFILE, "更新用户基本信息");
            return R.success("修改成功");
        } else {
            return R.error("修改失败");
        }
    }

    @Override
    public R updatePassword(ConsumerRequest updatePasswordRequest) {

       if (!this.verityPasswd(updatePasswordRequest.getUsername(),updatePasswordRequest.getOldPassword())) {
            return R.error("密码输入错误");
        }

        Consumer consumer = new Consumer();
        consumer.setId(updatePasswordRequest.getId());
        String secretPassword = DigestUtils.md5DigestAsHex((SALT + updatePasswordRequest.getPassword()).getBytes(StandardCharsets.UTF_8));
        consumer.setPassword(secretPassword);

        if (consumerMapper.updateById(consumer) > 0) {
            recordUserAction(consumer.getId(), ACTION_UPDATE_PASSWORD, "用户修改密码");
            return R.success("密码修改成功");
        } else {
            return R.error("密码修改失败");
        }
    }

    /**
     * 缩减验证
     * @param updatePasswordRequest
     * @return
     */
    @Override
    public R updatePassword01(ConsumerRequest updatePasswordRequest) {
        Consumer consumer = new Consumer();
        consumer.setId(updatePasswordRequest.getId());
        String secretPassword = DigestUtils.md5DigestAsHex((SALT + updatePasswordRequest.getPassword()).getBytes(StandardCharsets.UTF_8));
        consumer.setPassword(secretPassword);

        if (consumerMapper.updateById(consumer) > 0) {
            recordUserAction(consumer.getId(), ACTION_UPDATE_PASSWORD, "用户通过邮件重置密码");
            return R.success("密码修改成功");
        } else {
            return R.error("密码修改失败");
        }
    }


    @Override
    public R updateUserAvator(MultipartFile avatorFile, int id) {
        String fileName = avatorFile.getOriginalFilename();
        String imgPath = "/img/avatorImages/" + fileName;
        Consumer consumer = new Consumer();
        consumer.setId(id);
        consumer.setAvator(imgPath);
        String s = MinioUploadController.uploadAtorImgFile(avatorFile);
        if (s.equals("File uploaded successfully!")&&consumerMapper.updateById(consumer) > 0) {
            recordUserAction(consumer.getId(), ACTION_UPDATE_AVATAR, "用户更新头像");
            return R.success("上传成功", imgPath);
        } else {
            return R.error("上传失败");
        }
    }

    @Override
    public boolean existUser(String username) {
        QueryWrapper<Consumer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",username);
        return consumerMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean verityPasswd(String username, String password) {
        QueryWrapper<Consumer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",username);
        String secretPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes(StandardCharsets.UTF_8));

        queryWrapper.eq("password",secretPassword);
        return consumerMapper.selectCount(queryWrapper) > 0;
    }


    // 删除用户
    @Override
    public R deleteUser(Integer id) {
        if (consumerMapper.deleteById(id) > 0) {
            return R.success("删除成功");
        } else {
            return R.error("删除失败");
        }
    }

    @Override
    public R allUser() {
        return R.success(null, consumerMapper.selectList(null));
    }

    @Override
    public R userOfId(Integer id) {
        QueryWrapper<Consumer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",id);
        return R.success(null, consumerMapper.selectList(queryWrapper));
    }

    @Override
    public R loginStatus(ConsumerRequest loginRequest, HttpSession session) {

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        if (this.verityPasswd(username, password)) {
            session.setAttribute("username", username);
            QueryWrapper<Consumer> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);
            List<Consumer> consumerList = consumerMapper.selectList(queryWrapper);
            if (!consumerList.isEmpty()) {
                recordUserAction(consumerList.get(0).getId(), ACTION_LOGIN, "用户名登录成功");
            }
            return R.success("登录成功", consumerList);
        } else {
            return R.error("用户名或密码错误");
        }
    }

    @Override
    public R loginEmailStatus(ConsumerRequest loginRequest, HttpSession session) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        Consumer consumer1 = findByEmail(email);
        if (this.verityPasswd(consumer1.getUsername(), password)) {
            session.setAttribute("username", consumer1.getUsername());
            Consumer consumer = new Consumer();
            consumer.setUsername(consumer1.getUsername());
            List<Consumer> consumerList = consumerMapper.selectList(new QueryWrapper<>(consumer));
            if (!consumerList.isEmpty()) {
                recordUserAction(consumerList.get(0).getId(), ACTION_LOGIN, "邮箱登录成功");
            }
            return R.success("登录成功", consumerList);
        } else {
            return R.error("用户名或密码错误");
        }
    }

    @Override
    public Consumer findByEmail(String email) {
        QueryWrapper<Consumer> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("email",email);
        Consumer consumer = consumerMapper.selectOne(queryWrapper);
        return consumer;
    }

    private void recordUserAction(Integer userId, String action, String detail) {
        if (userId == null) {
            return;
        }
        String ip = IpUtils.getClientIp();
        userActionLogService.recordAction(userId, action, detail, ip);
    }
}
