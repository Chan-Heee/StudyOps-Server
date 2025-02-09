package com.StudyOps.domain.member.controller;

import com.StudyOps.domain.group.dto.StudyGroupResDto;
import com.StudyOps.domain.member.dto.InvitedMemberReqDto;
import com.StudyOps.domain.member.dto.InvitedMemberStatus;
import com.StudyOps.domain.member.service.InvitedMemberService;
import com.StudyOps.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.StudyOps.global.common.ApiResponseStatus.*;
import static org.springframework.http.HttpStatus.*;


@RestController
@RequiredArgsConstructor
public class InvitedMemberController {
    private final InvitedMemberService invitedMemberService;

    // 스터디 초대
    @PostMapping("/asks/{groupId}")
    public ResponseEntity<ApiResponse<Object>>  createInvitedMember(@PathVariable(value = "groupId") Long groupId, @RequestBody InvitedMemberReqDto invitedMemberReqDto){

        invitedMemberService.createInvitedMember(groupId,invitedMemberReqDto.getInvitees());

        ApiResponse<Object> successResponse = new ApiResponse<>(INVITED_MEMBER_CREATE_SUCCESS);

        return ResponseEntity.status(CREATED).body(successResponse);
    }
    //스터디 초대 수락
    @PostMapping("/asks/responses/{groupId}/{userId}")
    public ResponseEntity<ApiResponse<Object>> acceptInvitedStudyGroup(@PathVariable(value = "groupId") Long groupId,@PathVariable(value = "userId") Long userId){

        invitedMemberService.acceptInvitedStudyGroup(groupId,userId);
        ApiResponse<Object> successResponse = new ApiResponse<>(INVITED_MEMBER_ACCEPT_SUCCESS);

        return ResponseEntity.status(CREATED).body(successResponse);
    }

    //스터디 초대 거절
    @PatchMapping("/asks/responses/{groupId}/{userId}")
    public ResponseEntity<ApiResponse<Object>> rejectInvitedStudyGroup(@PathVariable(value = "groupId") Long groupId,@PathVariable(value = "userId") Long userId){

        invitedMemberService.rejectInvitedStudyGroup(groupId,userId);
        ApiResponse<Object> successResponse = new ApiResponse<>(INVITED_MEMBER_REJECT_SUCCESS);

        return ResponseEntity.status(OK).body(successResponse);
    }

    @GetMapping("/asks/{userId}")
    public ResponseEntity<ApiResponse<List<StudyGroupResDto>>> getAllInvitedStudyGroups(@PathVariable(value = "userId") Long userId){

        ApiResponse<List<StudyGroupResDto>> successResponse = new ApiResponse<>(ALL_INVITED_STUDY_GROUPS_GET_SUCCESS,invitedMemberService.getAllInvitedStudyGroups(userId));

        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }

    @GetMapping("/asks/responses/{groupId}")
    public ResponseEntity<ApiResponse<List<InvitedMemberStatus>>> getInvitedMemberStatus(@PathVariable(value = "groupId") Long groupId){

        ApiResponse<List<InvitedMemberStatus>> successResponse = new ApiResponse<>(INVITED_MEMBER_STATUS_GET_SUCCESS,invitedMemberService.getInvitedMemberStatus(groupId));

        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }
}
