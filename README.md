# lck_crawling
리그오브레전드 게임 Lck 경기 크롤링 프로젝트

# 목적 
네이버 e스포츠, op.gg 등등 Lck 경기 일정을 제공하는 사이트에 일일히 접속하여 내가 선호하는 팀의 경기 일정과 순위를 파악하는 것이
불편하여 사이트 제작

# 사용 기술
- BackEnd : SpringBoot, Spring Data Jpa
- FrontEnd : React.js
- DataBase : MySQL , Redis
- ETC : Firebase

# 기능
- Oauth를 통한 구글 로그인
- 월 별 경기 일정 파악 ( 네이버 e스포츠 크롤링 )
- Lck 팀들의 순위 파악 ( op.gg Lck 크롤링 )
- 선호하는 팀 선택 
- Fcm 알림 서비스를 통한 선호하는 팀 경기 알림 기능 ( 자정에 한 번, 경기 일정 3시간 전부터 1시간마다 )

# 개선 여부
- 일정, 순위 데이터 크롤링 시간 단축을 위해 redis를 활용한 캐싱으로 시간 단축 
- 일정 데이터, 순위 데이터를 동기적으로 크롤링 함으로써 시간 소요가 심하기에 CompletableFuture 통한 비동기 크롤링 실행
=> ( 크롤링 10번 평균 ) 동기 : 11,190ms , 비동기 : 7,408ms -> 시간 소요 약 33.8% 감소
- React.js interceptor를 통한 jwt 토큰 재발급

# 페이지 
- 홈 페이지
  <img width="1153" alt="Image" src="https://github.com/user-attachments/assets/a099cc24-ad1a-42d5-99d5-1271b063bd55" />
- 순위 페이지
![Image](https://github.com/user-attachments/assets/5f10ab26-a258-48a0-9b32-3ea15cd7d72a)
- 알림 기능
  <img width="1288" alt="Image" src="https://github.com/user-attachments/assets/1d524cdc-6856-4490-9a73-3c35889e459a" />

# DB 
![Image](https://github.com/user-attachments/assets/48657990-8fff-42e9-9c1f-dd46c848f3e6)
