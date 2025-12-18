package com.example.yin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.yin.common.R;
import com.example.yin.mapper.ConsumerMapper;
import com.example.yin.model.domain.Consumer;
import com.example.yin.model.request.ConsumerRequest;
import com.example.yin.service.UserActionLogService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ConsumerServiceImpl 单元测试
 * 重点测试用户行为日志埋点功能
 */
@RunWith(org.junit.runners.JUnit4.class)
@SuppressWarnings("unchecked")
public class ConsumerServiceImplTest {

    private ConsumerServiceImpl consumerService;
    private ConsumerMapper consumerMapper;
    private UserActionLogService userActionLogService;
    private HttpSession httpSession;

    @Before
    public void setUp() {
        consumerMapper = Mockito.mock(ConsumerMapper.class);
        userActionLogService = Mockito.mock(UserActionLogService.class);
        httpSession = Mockito.mock(HttpSession.class);

        consumerService = new ConsumerServiceImpl();
        ReflectionTestUtils.setField(consumerService, "consumerMapper", consumerMapper);
        ReflectionTestUtils.setField(consumerService, "userActionLogService", userActionLogService);
    }

    // ==================== 注册功能测试 ====================

    @Test
    public void addUser_shouldRecordActionWhenRegistrationSucceeds() {
        // 准备测试数据
        ConsumerRequest request = new ConsumerRequest();
        request.setUsername("testuser");
        request.setPassword("123456");
        request.setEmail("test@example.com");

        Consumer savedConsumer = new Consumer();
        savedConsumer.setId(1);
        savedConsumer.setUsername("testuser");

        // Mock 行为：用户名不存在，邮箱不存在，插入成功
        when(consumerMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        when(consumerMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        when(consumerMapper.insert(any(Consumer.class))).thenAnswer(invocation -> {
            Consumer c = invocation.getArgument(0);
            c.setId(1);
            return 1;
        });

        // 执行
        R result = consumerService.addUser(request);

        // 验证
        assertTrue(result.getSuccess());
        assertEquals("注册成功", result.getMessage());

        // 验证埋点调用
        ArgumentCaptor<Integer> userIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> detailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);

        verify(userActionLogService, times(1)).recordAction(
                userIdCaptor.capture(),
                actionCaptor.capture(),
                detailCaptor.capture(),
                ipCaptor.capture()
        );

        assertEquals(Integer.valueOf(1), userIdCaptor.getValue());
        assertEquals("REGISTER", actionCaptor.getValue());
        assertEquals("用户注册成功", detailCaptor.getValue());
    }

    @Test
    public void addUser_shouldNotRecordActionWhenUsernameExists() {
        ConsumerRequest request = new ConsumerRequest();
        request.setUsername("existinguser");
        request.setPassword("123456");

        when(consumerMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);

        R result = consumerService.addUser(request);

        assertFalse(result.getSuccess());
        assertEquals("用户名已注册", result.getMessage());
        verify(userActionLogService, never()).recordAction(any(), any(), any(), any());
    }

    @Test
    public void addUser_shouldNotRecordActionWhenEmailExists() {
        ConsumerRequest request = new ConsumerRequest();
        request.setUsername("newuser");
        request.setPassword("123456");
        request.setEmail("existing@example.com");

        Consumer existingUser = new Consumer();
        existingUser.setId(2);

        when(consumerMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        when(consumerMapper.selectOne(any(QueryWrapper.class))).thenReturn(existingUser);

        R result = consumerService.addUser(request);

        assertFalse(result.getSuccess());
        assertEquals("邮箱不允许重复", result.getMessage());
        verify(userActionLogService, never()).recordAction(any(), any(), any(), any());
    }

    // ==================== 登录功能测试 ====================

    @Test
    public void loginStatus_shouldRecordActionWhenLoginSucceeds() {
        ConsumerRequest loginRequest = new ConsumerRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("123456");

        Consumer consumer = new Consumer();
        consumer.setId(5);
        consumer.setUsername("testuser");
        List<Consumer> consumerList = new ArrayList<>();
        consumerList.add(consumer);

        // Mock 验证密码成功
        when(consumerMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);
        when(consumerMapper.selectList(any(QueryWrapper.class))).thenReturn(consumerList);

        R result = consumerService.loginStatus(loginRequest, httpSession);

        assertTrue(result.getSuccess());
        assertEquals("登录成功", result.getMessage());
        verify(httpSession).setAttribute("username", "testuser");

        // 验证埋点
        ArgumentCaptor<Integer> userIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> detailCaptor = ArgumentCaptor.forClass(String.class);

        verify(userActionLogService).recordAction(
                userIdCaptor.capture(),
                actionCaptor.capture(),
                detailCaptor.capture(),
                any()
        );

        assertEquals(Integer.valueOf(5), userIdCaptor.getValue());
        assertEquals("LOGIN", actionCaptor.getValue());
        assertEquals("用户名登录成功", detailCaptor.getValue());
    }

    @Test
    public void loginStatus_shouldNotRecordActionWhenPasswordWrong() {
        ConsumerRequest loginRequest = new ConsumerRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpass");

        when(consumerMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);

        R result = consumerService.loginStatus(loginRequest, httpSession);

        assertFalse(result.getSuccess());
        assertEquals("用户名或密码错误", result.getMessage());
        verify(userActionLogService, never()).recordAction(any(), any(), any(), any());
    }

    @Test
    public void loginEmailStatus_shouldRecordActionWhenLoginSucceeds() {
        ConsumerRequest loginRequest = new ConsumerRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("123456");

        Consumer consumerByEmail = new Consumer();
        consumerByEmail.setId(5);
        consumerByEmail.setUsername("testuser");
        consumerByEmail.setEmail("test@example.com");

        Consumer consumer = new Consumer();
        consumer.setId(5);
        consumer.setUsername("testuser");
        List<Consumer> consumerList = new ArrayList<>();
        consumerList.add(consumer);

        when(consumerMapper.selectOne(any(QueryWrapper.class))).thenReturn(consumerByEmail);
        when(consumerMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);
        when(consumerMapper.selectList(any(QueryWrapper.class))).thenReturn(consumerList);

        R result = consumerService.loginEmailStatus(loginRequest, httpSession);

        assertTrue(result.getSuccess());
        assertEquals("登录成功", result.getMessage());
        verify(httpSession).setAttribute("username", "testuser");

        // 验证埋点
        ArgumentCaptor<String> detailCaptor = ArgumentCaptor.forClass(String.class);
        verify(userActionLogService).recordAction(eq(5), eq("LOGIN"), detailCaptor.capture(), any());
        assertEquals("邮箱登录成功", detailCaptor.getValue());
    }

    @Test
    public void loginEmailStatus_shouldNotRecordActionWhenPasswordWrong() {
        ConsumerRequest loginRequest = new ConsumerRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrongpass");

        Consumer consumerByEmail = new Consumer();
        consumerByEmail.setUsername("testuser");

        when(consumerMapper.selectOne(any(QueryWrapper.class))).thenReturn(consumerByEmail);
        when(consumerMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);

        R result = consumerService.loginEmailStatus(loginRequest, httpSession);

        assertFalse(result.getSuccess());
        assertEquals("用户名或密码错误", result.getMessage());
        verify(userActionLogService, never()).recordAction(any(), any(), any(), any());
    }

    // ==================== 更新用户信息测试 ====================

    @Test
    public void updateUserMsg_shouldRecordActionWhenUpdateSucceeds() {
        ConsumerRequest updateRequest = new ConsumerRequest();
        updateRequest.setId(5);
        updateRequest.setUsername("testuser");
        updateRequest.setIntroduction("新的简介");

        when(consumerMapper.updateById(any(Consumer.class))).thenReturn(1);

        R result = consumerService.updateUserMsg(updateRequest);

        assertTrue(result.getSuccess());
        assertEquals("修改成功", result.getMessage());

        // 验证埋点
        ArgumentCaptor<Integer> userIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> detailCaptor = ArgumentCaptor.forClass(String.class);

        verify(userActionLogService).recordAction(
                userIdCaptor.capture(),
                actionCaptor.capture(),
                detailCaptor.capture(),
                any()
        );

        assertEquals(Integer.valueOf(5), userIdCaptor.getValue());
        assertEquals("UPDATE_PROFILE", actionCaptor.getValue());
        assertEquals("更新用户基本信息", detailCaptor.getValue());
    }

    @Test
    public void updateUserMsg_shouldNotRecordActionWhenUpdateFails() {
        ConsumerRequest updateRequest = new ConsumerRequest();
        updateRequest.setId(5);

        when(consumerMapper.updateById(any(Consumer.class))).thenReturn(0);

        R result = consumerService.updateUserMsg(updateRequest);

        assertFalse(result.getSuccess());
        assertEquals("修改失败", result.getMessage());
        verify(userActionLogService, never()).recordAction(any(), any(), any(), any());
    }

    // ==================== 修改密码测试 ====================

    @Test
    public void updatePassword_shouldRecordActionWhenPasswordUpdateSucceeds() {
        ConsumerRequest updateRequest = new ConsumerRequest();
        updateRequest.setId(5);
        updateRequest.setUsername("testuser");
        updateRequest.setOldPassword("oldpass");
        updateRequest.setPassword("newpass");

        // Mock 旧密码验证成功
        when(consumerMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);
        when(consumerMapper.updateById(any(Consumer.class))).thenReturn(1);

        R result = consumerService.updatePassword(updateRequest);

        assertTrue(result.getSuccess());
        assertEquals("密码修改成功", result.getMessage());

        // 验证埋点
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> detailCaptor = ArgumentCaptor.forClass(String.class);

        verify(userActionLogService).recordAction(eq(5), actionCaptor.capture(), detailCaptor.capture(), any());
        assertEquals("UPDATE_PASSWORD", actionCaptor.getValue());
        assertEquals("用户修改密码", detailCaptor.getValue());
    }

    @Test
    public void updatePassword_shouldNotRecordActionWhenOldPasswordWrong() {
        ConsumerRequest updateRequest = new ConsumerRequest();
        updateRequest.setId(5);
        updateRequest.setUsername("testuser");
        updateRequest.setOldPassword("wrongoldpass");
        updateRequest.setPassword("newpass");

        when(consumerMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);

        R result = consumerService.updatePassword(updateRequest);

        assertFalse(result.getSuccess());
        assertEquals("密码输入错误", result.getMessage());
        verify(userActionLogService, never()).recordAction(any(), any(), any(), any());
    }

    @Test
    public void updatePassword01_shouldRecordActionWhenResetPasswordSucceeds() {
        ConsumerRequest updateRequest = new ConsumerRequest();
        updateRequest.setId(5);
        updateRequest.setPassword("newpass");

        when(consumerMapper.updateById(any(Consumer.class))).thenReturn(1);

        R result = consumerService.updatePassword01(updateRequest);

        assertTrue(result.getSuccess());
        assertEquals("密码修改成功", result.getMessage());

        // 验证埋点
        ArgumentCaptor<String> detailCaptor = ArgumentCaptor.forClass(String.class);
        verify(userActionLogService).recordAction(eq(5), eq("UPDATE_PASSWORD"), detailCaptor.capture(), any());
        assertEquals("用户通过邮件重置密码", detailCaptor.getValue());
    }

    // ==================== 更新头像测试 ====================
    // 注意：updateUserAvator 方法调用了静态方法 MinioUploadController.uploadAtorImgFile
    // 完整测试需要使用 PowerMock 或重构代码使静态方法可测试
    // 这里暂时跳过，实际项目中建议将 Minio 上传逻辑抽取为可注入的服务
}

