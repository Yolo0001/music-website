package com.example.yin.controller;

import com.example.yin.common.R;
import com.example.yin.model.request.SongPlayRequest;
import com.example.yin.service.SongService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;

/**
 * SongController 集成测试（Web层），验证 /song/play 接口的请求映射与参数传递
 */
@RunWith(SpringRunner.class)
@WebMvcTest(SongController.class)
public class SongControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SongService songService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void recordSongPlay_shouldDelegateToService() throws Exception {
        SongPlayRequest req = new SongPlayRequest();
        req.setSongId(38);
        req.setUserId(5);

        Mockito.when(songService.recordSongPlay(Mockito.eq(38), Mockito.eq(5), Mockito.anyString()))
                .thenReturn(R.success("播放记录成功"));

        mockMvc.perform(post("/song/play")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("播放记录成功")))
                .andExpect(jsonPath("$.success", is(true)));
    }
}






