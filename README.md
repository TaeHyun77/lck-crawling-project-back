### 프로젝트 설명
---
리그 오브 레전드 LCK 경기 정보를 크롤링하여, 사용자가 선호하는 팀의 경기 일정과 순위를 한눈에 확인할 수 있도록 만든 서비스입니다.

사용자가 다른 일을 하더라도 경기 시작을 놓치지 않도록, 크롬 알림을 통해 경기 시작 전 알림을 받을 수 있도록 개발하였습니다<br><br>

### 기술 스택
---
- BackEnd : SpringBoot, Spring Data Jpa
  
- FrontEnd : React.js, JavaScript
  
- DataBase : MySQL
  
- ETC : Firebase<br><br>

### 기능
---
- Oauth를 통한 소셜 로그인
  
- 월 별 LCK 경기 일정 제공 ( 네이버 e 스포츠 크롤링 )
  
- Lck 팀들의 순위 제공 ( 네이버 e 스포츠 크롤링 )
  
- 선호하는 팀 선택
  
- Fcm 알림 서비스를 사용하여 선호하는 팀의 경기를 크롬 알림을 통해 제공 ( 자정에 한 번, 경기 시작 3시간 전부터 1시간마다, 10분 전 )<br><br>


### 개선 여부
---  
- LCK 일정, LCK 순위 데이터를 동기적으로 크롤링하면 불필요한 시간이 소요되므로 CompletableFuture을 사용하여 비동기적으로 크롤링 실행하여 문제를 해결하였습니다.<br>
  ( 크롤링 10번 평균 ) 동기 : 11,190ms , 비동기 : 7,408ms -> 시간 소요 약 33.8% 감소<br><br>

### 페이지
---
**[ 홈 페이지 ]**<br><br>
<img width="1710" height="922" alt="Image" src="https://github.com/user-attachments/assets/02cce9de-e574-4154-9193-f67b5936071f" /><br><br>

**[ 순위 페이지 ]**<br><br>
<img width="1710" height="804" alt="Image" src="https://github.com/user-attachments/assets/68beafa7-081d-4938-b0d2-6f7ab88eeb7a" /><br><br>

**[ 로그인 시 화면 ]**<br>
선호하는 팀을 선택 가능하며, 선호하는 팀의 경기를 필터링 해서 볼 수 있습니다.<br><br>
<img width="1710" height="804" alt="Image" src="https://github.com/user-attachments/assets/29a6e814-a42b-4a31-b390-1e28ffd6c0aa" /><br><br>

**[ 알림 기능 ]**<br>
알림 허용 기능을 구현하여 사용자 임의대로 알림 허용 여부 선택이 가능합니다.<br>
알림은 FCM을 사용하여 크롬 알림으로 구현했습니다.<br><br>
<img width="1710" height="804" alt="Image" src="https://github.com/user-attachments/assets/e9f7b38a-1a22-41c3-bf58-7bf58f5cc905" /><br><br>
