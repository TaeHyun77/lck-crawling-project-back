### 프로젝트 설명
---
리그 오브 레전드 LCK 경기 정보를 크롤링하여, 사용자가 선호하는 팀의 경기 일정과 순위를 한눈에 확인할 수 있도록 만든 서비스입니다. 

정보 확인을 위해 여러 사이트를 일일이 방문해야 하는 불편함을 해소하고 경기 시작 알림 서비스를 제공하기 위해 개발하였습니다.<br><br>

### 기술 스택
---
- BackEnd : SpringBoot, Spring Data Jpa<br><br>
- FrontEnd : React.js, JavaScript<br><br>
- DataBase : MySQL , Redis<br><br>
- ETC : Firebase<br><br>

### 기능
---
- Oauth를 통한 소셜 로그인<br><br>
- 월 별 경기 일정 파악 ( 네이버 e 스포츠 크롤링 )<br><br>
- Lck 팀들의 순위 파악 ( OP.GG LCK 크롤링 )<br><br>
- 선호하는 팀 선택 <br><br>
- Fcm 알림 서비스를 통해 선호하는 팀의 경기 알림 기능 제공 ( 자정에 한 번, 경기 일정 3시간 전부터 1시간마다 )<br><br>

### 개선 여부
---
- 일정, 순위 데이터 크롤링 시간 단축을 위해 redis를 활용한 캐싱으로 시간 단축 <br><br>
- 일정 데이터, 순위 데이터를 동기적으로 크롤링 함으로써 시간 소요가 심하기에 CompletableFuture 통한 비동기 크롤링 실행<br><br>
  ( 크롤링 10번 평균 ) 동기 : 11,190ms , 비동기 : 7,408ms -> 시간 소요 약 33.8% 감소<br><br>

### 페이지
---
- 홈 페이지<br>
  <img width="1383" alt="Image" src="https://github.com/user-attachments/assets/4ea4f242-e241-41b2-8622-672adf2a3612" width=600/><br><br>

- 순위 페이지<br>
  <img src="https://github.com/user-attachments/assets/20da48ee-5ca3-4e8f-91c3-1b44c31eff7f"><br><br>

- 로그인 시 화면<br>
  선호하는 팀을 선택 가능하며, 선호하는 팀의 경기를 필터링 해서 볼 수 있습니다.<br>
  <img width="1382" alt="Image" src="https://github.com/user-attachments/assets/f535ae0d-63af-4044-bfbc-3438b012860c" /><br><br>

- 알림 기능<br>
  알림 허용 기능을 구현하여 사용자 임의대로 알림 허용 여부 선택이 가능합니다.<br>
  알림은 FCM을 통해 크롬 알림으로 구현했습니다.<br>
  <br>
  <img width="1288" alt="Image" src="https://github.com/user-attachments/assets/1d524cdc-6856-4490-9a73-3c35889e459a" /><br><br>

### DB 
---
![Image](https://github.com/user-attachments/assets/48657990-8fff-42e9-9c1f-dd46c848f3e6)



