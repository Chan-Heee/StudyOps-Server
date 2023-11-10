package com.StudyOps.domain.penalty.service;

import com.StudyOps.domain.attendance.repository.StudyAttendanceRepository;
import com.StudyOps.domain.group.entity.StudyGroup;
import com.StudyOps.domain.group.repository.StudyGroupRepository;
import com.StudyOps.domain.member.entity.StudyMember;
import com.StudyOps.domain.member.repository.StudyMemberRepository;
import com.StudyOps.domain.penalty.dto.StudyGroupMemberPenaltyDto;
import com.StudyOps.domain.penalty.dto.StudyGroupPenaltyInfoResDto;
import com.StudyOps.domain.penalty.entity.StudyAbsentPenalty;
import com.StudyOps.domain.penalty.entity.StudyLatePenalty;
import com.StudyOps.domain.penalty.repository.PenaltyRepository;
import com.StudyOps.domain.schedule.entity.StudySchedule;
import com.StudyOps.domain.schedule.repository.StudyScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StudyPenaltyService {
    private final PenaltyRepository penaltyRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyScheduleRepository studyScheduleRepository;
    private final StudyAttendanceRepository studyAttendanceRepository;

    //매 정각마다 불참자 정보 업데이트
    /**
        1. 스터디 그룹 시작날짜가 현재 시간 이후인것만 전체 조회
        2. 전체 조회한 스터디그룹에서 for문 돌면서 오늘 요일 및 끝나는 시간 현재 정각이하 1시간 전 초과인것만 뽑기
        3. 해당 스터디 그룹 스터디 멤버 전체 조회
        4. 멤버별로 오늘 해당 되는 날짜 출결 테이블에 없으면 불참 처리
     **/
    @Scheduled(cron = "0 * * * * ?") //"0 0 0/1 * * *"
    public void updateAbsentStudyMember() {
        LocalTime now = LocalTime.now();
        if(now.compareTo(LocalTime.of(0,0)) == 0) // 00시 일경우
        {
            //스터디 그룹 시작날짜가 현재 시간 이후인것만 전체 조회한다.
            LocalDate target = LocalDate.now().minusDays(1);
            List<StudyGroup> allGroups = studyGroupRepository.findAllByStartDateIsLessThanEqual(target);

            // 반복문을 돌면서 하루전날이 스터디 요일인것과 끝나는시간이 23시 초과 00시이하인 스터디를 찾는다.
            for (int i = 0; i < allGroups.size(); i++) {
                StudyGroup studyGroup = allGroups.get(i);
                String dayOfWeek = target.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN);

                Optional<StudySchedule> studySchedule = studyScheduleRepository.findByStudyGroupAndDayWeekAndFinishTimeGreaterThanAndFinishTimeLessThanEqual(studyGroup, dayOfWeek, now.minusHours(1),now);
                if(studySchedule.isEmpty())
                    continue;
                List<StudyMember> members = studyMemberRepository.findAllByStudyGroup(studyGroup);
                for(int j=0; i<members.size(); j++){
                    StudyMember studyMember = members.get(j);
                    if(studyAttendanceRepository.findByStudyMemberAndDate(studyMember,target).isEmpty());
                    {

                    }
                }




            }
        }
        else{


        }


    }

    public void deleteLateStudyMember(StudyMember studyMember){
        List<StudyLatePenalty> penalties = penaltyRepository.findLatePenaltiesByStudyMember(studyMember);
        penalties.stream()
                .forEach(penalty -> penaltyRepository.delete(penalty));
    }

    public void deleteAbsentStudyMember(StudyMember studyMember){
        List<StudyAbsentPenalty> penalties = penaltyRepository.findAbsentPenaltiesByStudyMember(studyMember);
        penalties.stream()
                .forEach(penalty -> penaltyRepository.delete(penalty));
    }

    public StudyGroupPenaltyInfoResDto getStudyPenaltyInfo(Long groupId) {
        StudyGroup studyGroup = studyGroupRepository.findById(groupId).get();

        //스터디 그룹 pk로 해당 스터디 전체 멤버 조회한다.
        List<StudyMember> members = studyMemberRepository.findAllByStudyGroup(studyGroup);

        List<StudyGroupMemberPenaltyDto> settledPenaltyDtos = new ArrayList<>();
        List<StudyGroupMemberPenaltyDto> notSettledPenaltyDtos = new ArrayList<>();

        for(int i=0; i<members.size(); i++){
            StudyMember studyMember = members.get(i);

            //조회한 스터디멤버 해당 dto리스트에 삽입한다.
            settledPenaltyDtos.add(StudyGroupMemberPenaltyDto.builder()
                    .name(studyMember.getUser().getNickname())
                    .penalty(studyMember.getTotalPenalty())
                    .build());

            //스터디 멤버와 정산여부 필드 false값으로 studyPenalty조회한다.
            List<StudyLatePenalty> notSettledLatePenalties = penaltyRepository.findLatePenaltiesByStudyMemberAndIsSettled(studyMember,false);
            List<StudyAbsentPenalty> notSettledAbsentPenalties = penaltyRepository.findAbsentPenaltiesByStudyMemberAndIsSettled(studyMember, false);
            int penaltySum=0;
            // 각 penalty정보에 대하여 해당 멤버의 정산되지 않은 누적 벌금을 구한다.
            for(int j=0; j<notSettledLatePenalties.size(); j++){
                StudyLatePenalty notSettledPenalty = notSettledLatePenalties.get(i);
                penaltySum += notSettledPenalty.getFine();
            }
            for(int j=0; j<notSettledAbsentPenalties.size(); j++){
                StudyAbsentPenalty notSettledPenalty = notSettledAbsentPenalties.get(i);
                penaltySum += notSettledPenalty.getFine();
            }
            // 정산되지 않은 금액이 0이 아닐경우에만 dto리스트에 추가한다.
            if(penaltySum != 0)
                notSettledPenaltyDtos.add(StudyGroupMemberPenaltyDto.builder()
                    .name(studyMember.getUser().getNickname())
                    .penalty(penaltySum)
                    .build());
        }

        return StudyGroupPenaltyInfoResDto.builder()
                .totalFine(studyGroup.getTotalCost())
                .account(studyGroup.getAccount())
                .settledPenalties(settledPenaltyDtos)
                .notSettledPenalties(notSettledPenaltyDtos)
                .build();
    }
}
