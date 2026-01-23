[![codecov](https://codecov.io/gh/sb07-monew-team3/sb07-monew-team03/branch/feature%2FCD-hjy/graph/badge.svg?token=6PE3IV96TS)](https://codecov.io/gh/sb07-monew-team3/sb07-monew-team03)

## 📰 모두의 뉴스를 모은팀 
### 📒 팀 협업 문서 링크
https://www.notion.so/monew-2de7885bebca8048a324dcb10c6e8315

## 🤝 팀원 구성
#### 김진우 https://github.com/jinwo-o
#### 장미연 https://github.com/pring7th
#### 조성만 https://github.com/BetterCodings
#### 최태훈 https://github.com/Tae705
#### 황준영 https://github.com/OfficialHwempire

## 📌 프로젝트 소개
> **MoNew**는 사용자의 관심사를 기반으로 뉴스를 수집·제공하고,
기사에 대한 댓글·좋아요·활동 내역을 관리하는 **뉴스 큐레이션 서비스**입니다.
### ⏰ 프로젝트 기간
- **2026.01.05 ~ 2026.01.26**
## 🛠️ 기술 스택
### Backend
**Core**
- Java 17
- Spring Boot 3.5.9
- Spring Data JPA
- Spring Validation
- Spring Actuator
- Lombok
- Spring Batch

**Libs**
- QueryDSL 5.0.0
- ROME (RSS)
- Apache Commons Text
***
### Database
- PostgreSQL 16 
- MongoDB
***
### Infrastructure
**infra**
- Docker
- Docker & Compose
**CI/CD**
  -GitHub Action
***
### AWS
**Backend Infrastructure**
- AWS S3
- Aurora and RDS
- AWS EC2
- Elastic Conatiner Registry
- Elastic Container Service
**Monitoring**
- AWS CloudWatch

## 팀원별 구현 기능
### 🟩 김진우
### 🟩 장미연
**알람관리 API**
- 알림 정보의 CRUD 처리 (Spring Data JPA 사용, 시간 순으로 정렬 및 커서 페이지네이션 구현
**반응형 레이아웃 API**
- 클라이언트에서 요청된 반응형 레이아웃을 위한 RESTful API 엔드포인트 구현
**매일 배치 삭제 API**
- 확인한 알림 중 1주일이 경과된 알림은 자동으로 삭제
### 🟩 조성만
### 🟩 최태훈

**댓글 도메인 전체 설계 및 구현**  
  - 댓글 CRUD (등록 / 수정 / 소프트 삭제 / 하드 삭제)
  - 좋아요 기능 구현 (멱등성 보장)
    
**커서 기반 페이지네이션 구현**
  - createdAt + id 기반 커서
   - 좋아요 순 정렬 시 보조 커서(after) 적용
     
**성능 최적화**
  - QueryDSL 전용 조회 Repository 분리
  - 좋아요 수 / 사용자 좋아요 여부 N+1 문제 제거

**예외 처리 및 응답 구조 설계**

**단위 테스트 / 통합 테스트 작성**

**Swagger API 문서 정리**

### 🟩 황준영

## ✨ 주요 기능
### 📰 뉴스 기사
- 정기 배치 작업을 통한 뉴스 기사 수집

- 기사 백업 및 복구 기능

- 기사 조회 및 목록 제공
### 💬 댓글 & 좋아요
- 기사별 댓글 등록 / 수정 / 삭제

- 좋아요 등록 및 취소 (중복 요청에도 안전)

- 등록순 / 좋아요순 정렬 지원

- 커서 기반 페이지네이션으로 중복·누락 없는 조회
### 💡 관심사
### 🙍 유저 & 유저 활동 내역
- 유저 등록
- 로그인 유효성 검사 
- 댓글 작성 / 좋아요 / 기사 조회 활동 기록
### 🚨 알림

## 파일 구조 
< 나중에 넣을 예정>

## ERD 다이어 그램
<img width="1590" height="1093" alt="image" src="https://github.com/user-attachments/assets/b109c82f-b481-4d24-8e85-d58ea309cb67" />

## 클래스 다이어그램
<img width="1660" height="821" alt="image" src="https://github.com/user-attachments/assets/b6b21331-193b-4e46-a42e-495f2edf2d11" />

## 배포 다이어그램
<img width="1751" height="810" alt="image" src="https://github.com/user-attachments/assets/9e4b1265-454e-4ca3-9588-42a5d151141f" />

## 구현 홈페이지
**http://ec2-13-125-235-192.ap-northeast-2.compute.amazonaws.com/#/interests?direction=DESC**
## 프로젝트 회고록 
<img width="1716" height="792" alt="image" src="https://github.com/user-attachments/assets/d191c902-de55-4282-9430-fecc5fa7cb3b" />

