package com.example.crawling.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    UNKNOWN("000_UNKNOWN", "알 수 없는 에러가 발생했습니다."),

    FAIL_TO_CRAWLING_DATA("FAIL_TO_CRAWLING_DATA", "데이터 크롤링에 실패하였습니다."),

    FAIL_TO_SETTING_DRIVER("FAIL_TO_SETTING_DRIVER", "드라이버 세팅을 실패하였습니다."),

    FAIL_TO_LOAD_DRIVER("FAIL_TO_LOAD_DRIVER", "드라이버 세팅을 실패하였습니다."),

    FAIL_TO_DELETE_SCHEDULE_DATA("FAIL_TO_DELETE_SCHEDULE_DATA", "일정 데이터 삭제에 실패하였습니다."),

    FAIL_TO_STORE_SCHEDULE_DATA("FAIL_TO_STORE_RANKING_DATA", "일정 데이터 저장에 실패하였습니다."),

    FAIL_TO_DELETE_RANKING_DATA("FAIL_TO_DELETE_RANKING_DATA", "순위 데이터 삭제에 실패하였습니다."),

    FAIL_TO_STORE_RANKING_DATA("FAIL_TO_STORE_RANKING_DATA", "순위 데이터 저장에 실패하였습니다."),

    EXPIRED_TOKEN("EXPIRED_TOKEN", "토큰이 만료되었습니다."),

    INVALID_REFRESH_TOKEN("INVALID_REFRESH_TOKEN", "refresh 토큰이 아닙니다."),

    INVALID_ACCESS_TOKEN("INVALID_ACCESS_TOKEN", "access 토큰이 아닙니다.");


    private final String errorCode;

    private final String message;
}
