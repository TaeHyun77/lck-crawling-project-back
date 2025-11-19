### 프로젝트 설명
---
리그 오브 레전드 LCK 경기 정보를 크롤링하여, 사용자가 선호하는 팀의 경기 일정과 순위를 한눈에 확인할 수 있도록 만든 서비스입니다.

사용자가 다른 일을 하더라도 경기 시작을 놓치지 않도록, 크롬 알림을 통해 경기 시작 전 알림을 받을 수 있도록 개발하였습니다.<br><br>

개발 과정 블로그<br>
https://velog.io/@ayeah77/series/Lck-%EA%B2%BD%EA%B8%B0-%EC%A0%95%EB%B3%B4-%EC%A0%9C%EA%B3%B5-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%ED%81%AC%EB%A1%A4%EB%A7%81

<br>

### 기술 스택
---
- BackEnd : SpringBoot, Spring Data Jpa
  
- FrontEnd : React.js, JavaScript
  
- DataBase : MySQL
  
- ETC : Google Firebase<br><br>

### 기능
---
- JWT와 Oauth를 사용한 로그인
  
- 2025 월 별 LCK 경기 일정 제공 ( 네이버 e 스포츠 크롤링 )
  
- 2025 LCK 팀 순위 제공 ( 네이버 e 스포츠 크롤링 )
  
- 사용자가 선호하는 팀 선택 후, FCM을 사용하여 선호하는 팀의 경기를 크롬 브라우저 알림을 통해 제공<br>
  ( 자정에 한 번, 경기 시작 3시간 전부터 1시간마다, 10분 전 )<br><br>


### 개선 여부
---  
- LCK 일정, 순위 데이터를 순차적으로 크롤링하면 불필요한 시간이 소요되므로 CompletableFuture를 사용하여 비동기적 크롤링 통해 시간을 단축하였습니다.<br>
  ( 크롤링 10번 평균 ) 동기 : 2,590ms , 비동기 : 1,984ms → 시간 소요 약 23% 감소<br><br>

### 페이지
---
**[ 홈 페이지 ]**<br><br>
<img width="1710" height="922" alt="Image" src="https://github.com/user-attachments/assets/02cce9de-e574-4154-9193-f67b5936071f" /><br><br><br>

**[ 순위 페이지 ]**<br><br>
<img width="1710" height="804" alt="Image" src="https://github.com/user-attachments/assets/68beafa7-081d-4938-b0d2-6f7ab88eeb7a" /><br><br><br>

**[ 로그인 시 화면 ]**<br><br>
선호하는 팀을 선택 가능하며, 선호하는 팀의 경기를 필터링 해서 볼 수 있습니다.<br><br>
<img width="1710" height="804" alt="Image" src="https://github.com/user-attachments/assets/29a6e814-a42b-4a31-b390-1e28ffd6c0aa" /><br><br><br>

**[ 알림 기능 ]**<br><br>
알림 허용 기능을 구현하여 사용자 임의대로 알림 허용 여부 선택이 가능합니다.<br>
알림은 FCM을 사용하여 크롬 알림으로 구현했습니다.<br><br>
<img width="1710" height="804" alt="Image" src="https://github.com/user-attachments/assets/e9f7b38a-1a22-41c3-bf58-7bf58f5cc905" /><br><br><br>
