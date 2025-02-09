package com.StudyOps.domain.attendance.controller;

import com.StudyOps.domain.attendance.service.StudyAttendanceVoteService;
import com.StudyOps.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.StudyOps.global.common.ApiResponseStatus.STUDY_DATE_ABSENT_SUCCESS;


@RestController
@RequiredArgsConstructor
public class StudyAttendanceVoteController {
    private final StudyAttendanceVoteService studyAttendanceVoteService;

    @PatchMapping("/schedules/attendances/{groupId}/{userId}")
    public ResponseEntity<ApiResponse<Object>> absentOrAttendStudyDate(@PathVariable(value = "groupId") Long groupId, @PathVariable(value = "userId") Long userId, @RequestParam String date, @RequestParam Boolean attendance) {

        studyAttendanceVoteService.absentOrAttendStudyDate(groupId, userId, date, attendance);

        ApiResponse<Object> successResponse = new ApiResponse<>(STUDY_DATE_ABSENT_SUCCESS);

        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }
}
