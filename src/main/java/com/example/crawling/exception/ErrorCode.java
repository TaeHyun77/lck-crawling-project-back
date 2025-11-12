package com.example.crawling.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    UNKNOWN("000_UNKNOWN", "알 수 없는 에러가 발생했습니다."),

    FAIL_TO_CRAWLING_LCK_DATA("FAIL_TO_CRAWLING_LCK_DATA", "LCK 일정 데이터 크롤링에 실패하였습니다."),

    FAIL_TO_SETTING_DRIVER("FAIL_TO_SETTING_DRIVER", "드라이버 세팅을 실패하였습니다."),

    FAIL_TO_LOAD_DRIVER("FAIL_TO_LOAD_DRIVER", "드라이버 세팅을 실패하였습니다."),

    FAIL_TO_STORE_RANKING_DATA("FAIL_TO_STORE_RANKING_DATA", "순위 데이터 저장에 실패하였습니다."),

    EXPIRED_TOKEN("EXPIRED_TOKEN", "토큰이 만료되었습니다."),

    INVALID_REFRESH_TOKEN("INVALID_REFRESH_TOKEN", "refresh 토큰이 아닙니다."),

    NOT_FOUND_USER("NOT_FOUND_USER", "유저를 찾을 수 없습니다."),

    NOT_FOUND_MATCHSCHEDULE("NOT_FOUND_MATCHSCHEDULE", "경기 일정 정보를 찾을 수 없습니다."),

    NOT_FOUND_RANKING_DATA("NOT_FOUND_RANKING_DATA", "해당 순위 데이터를 찾을 수 없습니다."),

    NOT_FOUND_FCMTOKEN("NOT_FOUND_FCMTOKEN", "FCM 토큰을 찾을 수 없습니다."),

    ERROR_TO_PARSING_MONTH("ERROR_TO_PARSING_MONTH", "월 데이터 파싱 중 오류 발생"),

    FAILED_TO_SEND_NOTIFICATION_3_HOURS_BEFORE("FAILED_TO SEND_NOTIFICATION_3_HOURS_BEFORE", "경기 3시간 전 알림 전송 실패"),

    FAILED_TO_SEND_NOTIFICATION_24_HOURS_BEFORE("FAILED_TO_SEND_NOTIFICATION_24_HOURS_BEFORE", "경기 24시간 전 알림 전송 실패"),

    OPTIMISTICLOCKING("OPTIMISTICLOCKING", "낙관적 락 발생"),

    FAIL_TO_CRAWLING_DATA("FAIL_TO_CRAWLING_DATA", "크롤링 실패");

    private final String errorCode;

    private final String message;
}
