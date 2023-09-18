package com.StudyOps.domain.group.service;

import com.StudyOps.domain.attendance.service.StudyAttendanceService;
import com.StudyOps.domain.attendance.service.StudyAttendanceVoteService;
import com.StudyOps.domain.group.dto.StudyGroupCreateReqDto;
import com.StudyOps.domain.group.entity.StudyGroup;
import com.StudyOps.domain.group.repository.StudyGroupRepository;
import com.StudyOps.domain.member.entity.StudyMember;
import com.StudyOps.domain.member.repository.StudyMemberRepository;
import com.StudyOps.domain.member.service.InvitedMemberService;
import com.StudyOps.domain.member.service.StudyMemberService;
import com.StudyOps.domain.penalty.service.StudyPenaltyService;
import com.StudyOps.domain.schedule.service.StudyScheduleService;
import com.StudyOps.domain.user.entity.User;
import com.StudyOps.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StudyGroupService {
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyMemberService studyMemberService;
    private final StudyScheduleService studyScheduleService;
    private final InvitedMemberService invitedMemberService;
    private final StudyAttendanceService studyAttendanceService;
    private final StudyAttendanceVoteService studyAttendanceVoteService;
    private final StudyPenaltyService studyPenaltyService;

    //테스트 유저 먼저 생성
    @PostConstruct
    public void init() {
        User user1 = new User();
        User user2 = new User();
        User user3 = new User();
        User user4 = new User();

        user1.setNickname("이찬희");
        user2.setNickname("장희영");
        user3.setNickname("소예원");
        user4.setNickname("누구지");

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        userRepository.save(user4);
    }

    public void createStudyGroup(Long userId, StudyGroupCreateReqDto studyGroupCreateReqDto) {
        //userId로 유저를 찾는다. Optional로 조회되므로 .get()매서드를 사용해준다.
        User user = userRepository.findById(userId).get();

        //studyGroupCreateReqDto를 엔티티로 변환 후 디비에 정보를 저장한다.
        StudyGroup studyGroup = studyGroupCreateReqDto.toEntity();
        studyGroupRepository.save(studyGroup);

        //StudyMember 생성
        studyMemberService.createStudyMember(user, studyGroup, true);

        //StudySchedule 생성
        studyScheduleService.createStudySchedule(studyGroup, studyGroupCreateReqDto.getSchedules());

        //InvitedMember 생성
        invitedMemberService.createInvitedMember(studyGroup, studyGroupCreateReqDto.getInvitees());
    }

    /***********************************
     1.스터디 멤버테이블에서 삭제(마지막)
     2.스터디 그룹 인원수 한명감소(완료)
     3.스터디 출결테이블에서 삭제(완료)
     4.스터디 투표테이블에서 삭제(완료)
     4.스터디 벌금테이블에서 삭제(완료)
     5.스터디 게시판테이블에서 삭제(나중에 게시판 구현후)
     6.추후 스터디장이 탈퇴시 위임기능 추가
     ***********************************/
    public void quitStudyGroup(Long groupId, Long userId) {

        User user = userRepository.findById(userId).get();
        StudyGroup studyGroup = studyGroupRepository.findById(groupId).get();
        studyGroup.decreaseHeadCount();

        //studyMember조회
        StudyMember studyMember = studyMemberRepository.findByStudyGroupAndUser(studyGroup,user).get();

        //출결 테이블에서 찾은 studyMember 삭제
        studyAttendanceService.deleteStudyMember(studyMember);

        //투표 테이블에서 찾은 studyMember 삭제
        studyAttendanceVoteService.deleteStudyMember(studyMember);

        //벌금 테이블에서 찾은 studyMember 삭제
        studyPenaltyService.deleteStudyMember(studyMember);
    }
}

