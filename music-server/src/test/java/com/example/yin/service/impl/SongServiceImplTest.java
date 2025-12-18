package com.example.yin.service.impl;

import com.example.yin.common.R;
import com.example.yin.mapper.ConsumerMapper;
import com.example.yin.mapper.SongMapper;
import com.example.yin.model.domain.Consumer;
import com.example.yin.model.domain.Song;
import com.example.yin.service.UserActionLogService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 针对新增的播放记录与用户行为记录埋点的单元测试
 */
public class SongServiceImplTest {

    private SongServiceImpl songService;

    private SongMapper songMapper;
    private ConsumerMapper consumerMapper;
    private UserActionLogService userActionLogService;

    @Before
    public void setUp() {
        songMapper = Mockito.mock(SongMapper.class);
        consumerMapper = Mockito.mock(ConsumerMapper.class);
        userActionLogService = Mockito.mock(UserActionLogService.class);

        songService = new SongServiceImpl();
        // 通过反射注入依赖
        ReflectionTestUtils.setField(songService, "songMapper", songMapper);
        ReflectionTestUtils.setField(songService, "consumerMapper", consumerMapper);
        ReflectionTestUtils.setField(songService, "userActionLogService", userActionLogService);
    }

    @Test
    public void recordSongPlay_shouldReturnErrorWhenSongIdIsNull() {
        R r = songService.recordSongPlay(null, 1, "user");
        assertFalse(r.getSuccess());
        assertEquals("歌曲ID不能为空", r.getMessage());
        verify(userActionLogService, never()).recordAction(anyInt(), anyString(), anyString(), anyString());
    }

    @Test
    public void recordSongPlay_shouldReturnErrorWhenSongNotExist() {
        when(songMapper.selectById(100)).thenReturn(null);

        R r = songService.recordSongPlay(100, 1, "user");

        assertFalse(r.getSuccess());
        assertEquals("歌曲不存在", r.getMessage());
        verify(userActionLogService, never()).recordAction(anyInt(), anyString(), anyString(), anyString());
    }

    @Test
    public void recordSongPlay_shouldRecordActionWhenUserIdProvided() {
        Song song = new Song();
        song.setId(38);
        song.setName("测试歌曲");
        when(songMapper.selectById(38)).thenReturn(song);

        R r = songService.recordSongPlay(38, 5, null);

        assertTrue(r.getSuccess());
        assertEquals("播放记录成功", r.getMessage());

        ArgumentCaptor<Integer> userIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> detailCaptor = ArgumentCaptor.forClass(String.class);

        verify(userActionLogService).recordAction(
                userIdCaptor.capture(),
                actionCaptor.capture(),
                detailCaptor.capture(),
                anyString());

        assertEquals(Integer.valueOf(5), userIdCaptor.getValue());
        assertEquals("PLAY_SONG", actionCaptor.getValue());
        // detail 中包含歌曲名称和 ID 即可
        String detail = detailCaptor.getValue();
        assertTrue(detail.contains("测试歌曲"));
        assertTrue(detail.contains("38"));
    }

    @Test
    public void recordSongPlay_shouldResolveUserIdFromSessionWhenUserIdMissing() {
        Song song = new Song();
        song.setId(38);
        song.setName("测试歌曲");
        when(songMapper.selectById(38)).thenReturn(song);

        Consumer consumer = new Consumer();
        consumer.setId(9);
        when(consumerMapper.selectOne(any())).thenReturn(consumer);

        R r = songService.recordSongPlay(38, null, "sessionUser");

        assertTrue(r.getSuccess());
        assertEquals("播放记录成功", r.getMessage());

        ArgumentCaptor<Integer> userIdCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(userActionLogService).recordAction(
                userIdCaptor.capture(),
                anyString(),
                anyString(),
                anyString());
        assertEquals(Integer.valueOf(9), userIdCaptor.getValue());
    }

    @Test
    public void recordSongPlay_shouldSkipLogWhenUserCannotBeResolved() {
        Song song = new Song();
        song.setId(38);
        song.setName("测试歌曲");
        when(songMapper.selectById(38)).thenReturn(song);
        // consumerMapper 返回 null，表示找不到用户
        when(consumerMapper.selectOne(any())).thenReturn(null);

        R r = songService.recordSongPlay(38, null, "unknownUser");

        assertTrue(r.getSuccess());
        assertEquals("播放记录成功", r.getMessage());
        verify(userActionLogService, never()).recordAction(anyInt(), anyString(), anyString(), anyString());
    }
}


