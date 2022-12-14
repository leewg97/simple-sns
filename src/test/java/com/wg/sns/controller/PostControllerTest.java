package com.wg.sns.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wg.sns.controller.request.PostCommentRequest;
import com.wg.sns.controller.request.PostCreateRequest;
import com.wg.sns.controller.request.PostModifyRequest;
import com.wg.sns.exception.ErrorCode;
import com.wg.sns.exception.SnsApplicationException;
import com.wg.sns.fixture.PostEntityFixture;
import com.wg.sns.model.Post;
import com.wg.sns.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private PostService postService;

    public PostControllerTest(@Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @WithMockUser
    @DisplayName("포스트 작성이 성공한 경우")
    @Test
    void postCreationIsSuccessful() throws Exception {
        String title = "title";
        String body = "body";

        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostCreateRequest(title, body))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithAnonymousUser
    @DisplayName("포스트 작성 전 로그인 하지 않은 경우")
    @Test
    void ifYouAreNotLoggedInBeforeCreatingAPost() throws Exception {
        String title = "title";
        String body = "body";

        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostCreateRequest(title, body))))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser
    @DisplayName("포스트 수정이 성공한 경우")
    @Test
    void postModificationIsSuccessful() throws Exception {
        String title = "title";
        String body = "body";

        when(postService.modify(eq(title), eq(body), any(), any()))
                .thenReturn(Post.fromEntity(PostEntityFixture.get("username", 1L, 1L)));

        mockMvc.perform(put("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostCreateRequest(title, body))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithAnonymousUser
    @DisplayName("포스트 수정 전 로그인 하지 않은 경우")
    @Test
    void ifYouAreNotLoggedInBeforeModifyingThePost() throws Exception {
        String title = "title";
        String body = "body";

        mockMvc.perform(put("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostCreateRequest(title, body))))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser
    @DisplayName("본인이 작성하지 않은 포스트를 수정하려는 경우 에러 발생")
    @Test
    void ifThePostIsNotWrittenByYouWhenModifyingIt() throws Exception {
        String title = "title";
        String body = "body";

        doThrow(new SnsApplicationException(ErrorCode.INVALID_PERMISSION)).when(postService).modify(eq(title), eq(body), any(), eq(1L));

        mockMvc.perform(put("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostCreateRequest(title, body))))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser
    @DisplayName("수정하려는 포스트 아이디가 존재하지 않는 경우 에러 발생")
    @Test
    void thePostIdYouWantToModifyDoesNotExist() throws Exception {
        String title = "title";
        String body = "body";

        doThrow(new SnsApplicationException(ErrorCode.POST_NOT_FOUND)).when(postService).modify(eq(title), eq(body), any(), eq(1L));

        mockMvc.perform(put("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostCreateRequest(title, body))))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @WithMockUser
    @DisplayName("포스트 삭제 성공한 경우")
    @Test
    void postDeletionIsSuccessful() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithAnonymousUser
    @DisplayName("포스트 삭제 전 로그인 하지 않은 경우")
    @Test
    void ifYouAreNotLoggedInBeforeDeletingThePost() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser
    @DisplayName("본인이 작성하지 않은 포스트를 삭제하려는 경우 에러 발생")
    @Test
    void ifThePostIsNotWrittenByYouWhenDeletingIt() throws Exception {
        doThrow(new SnsApplicationException(ErrorCode.INVALID_PERMISSION)).when(postService).delete(any(), any());

        mockMvc.perform(delete("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser
    @DisplayName("삭제하려는 포스트 아이디가 존재하지 않는 경우 에러 발생")
    @Test
    void thePostIdYouWantToDeleteDoesNotExist() throws Exception {
        doThrow(new SnsApplicationException(ErrorCode.POST_NOT_FOUND)).when(postService).delete(any(), any());

        mockMvc.perform(delete("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @WithMockUser
    @DisplayName("포스트 목록 요청")
    @Test
    void requestPostList() throws Exception {
        when(postService.list(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithAnonymousUser
    @DisplayName(" 포스트 목록 요청시 로그인하지 않은 경우")
    @Test
    void ifYouAreNotLoggedInBeforeRequestingPostList() throws Exception {
        when(postService.list(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser
    @DisplayName("내 포스트 목록 요청")
    @Test
    void requestMyPostList() throws Exception {
        when(postService.myList(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/posts/my")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithAnonymousUser
    @DisplayName("내 포스트 목록 요청시 로그인하지 않은 경우")
    @Test
    void ifYouAreNotLoggedInBeforeRequestingMyPostList() throws Exception {
        when(postService.myList(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/posts/my")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser
    @DisplayName("댓글 등록")
    @Test
    void postCommentSuccessful() throws Exception {
        mockMvc.perform(post("/api/v1/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostCommentRequest("comment"))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithAnonymousUser
    @DisplayName("댓글 작성시 로그인하지 않은 경우")
    @Test
    void ifYouAreNotLoggedInBeforeRequestingComment() throws Exception {
        mockMvc.perform(post("/api/v1/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostCommentRequest("comment"))))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser
    @DisplayName("댓글 작성시 포스트 없는 경우")
    @Test
    void thePostYouWantToCommentDoesNotExist() throws Exception {
        doThrow(new SnsApplicationException(ErrorCode.POST_NOT_FOUND)).when(postService).comment(any(), any(), any());

        mockMvc.perform(post("/api/v1/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostCommentRequest("comment"))))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

}
