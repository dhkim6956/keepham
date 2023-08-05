package com.ssafy.keepham.domain.chatroom.controller;

import com.ssafy.keepham.common.api.Api;
import com.ssafy.keepham.common.error.ChatRoomError;
import com.ssafy.keepham.common.error.ErrorCode;
import com.ssafy.keepham.common.exception.ApiException;
import com.ssafy.keepham.domain.chatroom.dto.ChatRoomRequest;
import com.ssafy.keepham.domain.chatroom.dto.ChatRoomResponse;
import com.ssafy.keepham.domain.chatroom.dto.RoomPassword;
import com.ssafy.keepham.domain.chatroom.entity.enums.ChatRoomStatus;
import com.ssafy.keepham.domain.chatroom.service.ChatRoomManager;
import com.ssafy.keepham.domain.chatroom.service.ChatRoomService;
import com.ssafy.keepham.security.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
@RequestMapping("/api")
@Slf4j
public class ChatRoomApiController {

    private final ChatRoomService chatRoomService;
    private final ChatRoomManager chatRoomManager;
    private Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String tempNickName = UUID.randomUUID().toString();

    @Operation(summary = "방생성")
    @PostMapping("/rooms")
    private Api<ChatRoomResponse> createRoom(
            @RequestBody ChatRoomRequest chatRoomRequest
    ){
        chatRoomRequest.setSuperUserId(tempNickName);
        var res = chatRoomService.createRoom(chatRoomRequest);
        return Api.OK(res);
    }

    @Operation(summary = "모든 채팅방 조회")
    @GetMapping("/rooms")
    private Api<List<ChatRoomResponse>> findAllOpenedRoom(
            @RequestParam ChatRoomStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int pageSize
    ){
        return Api.OK(chatRoomService.openedRoom(status, page, pageSize));
    }

    @Operation(summary = "boxId로 채팅방 조회")
    @GetMapping("/rooms/{boxId}")
    private Api<List<ChatRoomResponse>> findOpenedRoomByBoxId(
            @PathVariable Long boxId,
            @RequestParam ChatRoomStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int pageSize
    ){

        return Api.OK(chatRoomService.findOpenedRoomByBoxId(status, page, pageSize, boxId));
    }

    @Operation(summary = "채팅방 입장")
    @GetMapping("/{roomId}")
    public Api<Object> enterRoom(@PathVariable Long roomId){
        if (chatRoomManager.isSecretRoom(roomId)){
           throw new ApiException(ChatRoomError.SECRET_ROOM);
        }

        chatRoomManager.userJoin(roomId, tempNickName);
        return Api.OK(tempNickName);
    }

    @Operation(summary = "비밀 방 입장")
    @PostMapping("/{roomId}")
    public Api<Object> enterSecretRoom(@PathVariable Long roomId, @RequestBody RoomPassword password){
        if (chatRoomManager.isPasswordCorrect(roomId, password.getPassword())){
            chatRoomManager.userJoin(roomId, tempNickName);
            return Api.OK(tempNickName);
        } else {
            return Api.ERROR(ErrorCode.BAD_REQUEST, "방 비밀번호가 일치하지 않습니다.");
        }
    }

    @Operation(summary = "해당 채팅방에 현재 유저 전부 삭제")
    @GetMapping("/{roomId}/clear")
    public Api<String> clearRoom(@PathVariable Long roomId){
        chatRoomManager.allUserClear(roomId);
        return Api.OK("전체 삭제 성공");
    }

    @Operation(summary = "해당 채팅방의 모든 유저 닉네임 조회")
    @GetMapping("/{roomId}/allUser")
    private Api<Set<String>> getAllUser(@PathVariable Long roomId){
        return Api.OK(chatRoomManager.getAllUser(roomId));
    }



}
