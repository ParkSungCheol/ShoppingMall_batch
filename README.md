# 11번가 쇼핑데이터 활용서비스
------------
### 왜 이런 프로젝트를 기획하였나요?
> **[ 대량의 데이터 ]** 를 가지고<br/>
> **[ 초기 데이터 적재부터 단순조회 및 활용까지 전 과정 ]** 을 다뤄보고자<br/>
> **[ 대중들에게 가장 익숙하고 의미있는 쇼핑데이터 ]** 를 이용한 프로젝트<br/>
------------
### 어떤 기술을 사용하였나요?
> **[ 크롤링 및 API ]**
+ [ Selenium ] 
  + 브라우저를 통해 직접 해당 페이지로 접근하는 방식으로 가장 인간의 활동과 유사
  + AWS 프리티어 메모리로는 급격한 성능저하 발생으로 **사용불가**
+ [ Jsoup ] 
  + 적은 메모리로도 사용가능
  + **이미지 로딩 불가** & 지속적인 호출 시 IP차단되거나 500번 503번 서버에러 발생 등으로 데이터 적재 로직의 **안정성 저하 우려**
+ **[ API ]** 
  + 적은 메모리로도 사용가능
  + 이미지를 포함한 상품의 모든 상세정보를 가져올 수 있으면서 최소한의 서버부하로 해결가능<br/> **=> PICK**
> **[ 스프링 배치 ]**
+ **[ Chunk ]** 
  + Tasklet 방식은 tasklet 전체가 트랜잭션으로 다뤄져서 완료되지 않는 이상 DB에 적재하지 않음
  + Chunk방식은 유연한 트랜잭션 관리를 제공해서 페이지 1단위씩 트랜잭션을 구성하여 <br/>
  언제라도 interrupt가 발생하면 이전 페이지까지는 DB에 적재
+ **[ 멀티쓰레드 ]** 
  + API방식을 채택하여 메모리가 부족하지 않은 환경에서 멀티쓰레드를 적용하여 대량의 데이터를 단시간내에 적재
+ **[ 재시도 및 알림 ]**
  + API 호출 시 제대로 된 데이터를 받지 못했을 경우 3번 재시도 <br/>
  => chunk단위가 실패하면 Decider를 설정하여 해당 chunk 3번 재시도 <br/>
  => 모두 실패 시 Slack 알림 띄우도록 설계하여 안정성 보장
> **[ ES, Logstash, RDB ]**
+ **[ ES ]** 
  + 최소 몇 백만 건 이상의 대규모 데이터를 가지고 실시간 검색 및 통계작업에 적합
  + 한국어 검색이 용이하도록 한국어형태소 분석기 Nori와 문자열을 잘라서 찾아내는 nGram 활용
  + 최소한의 검색연관도를 보장하기 위해 min_score : 20을 부여하여 질의
  + 메모리 부족으로 인한 성능저하 방지를 위해 최대 result 반환수를 800개로 한정
+ **[ RDB(MySQL) ]** 
  + 데이터의 일관성 및 무결성 보장, 정형화된 관계파악에 적합
  + MySQL은 무료이고 메모리 사용량이 적어 적합
+ **[ Logstash ]** 
  + RDB(MySQL) => ES 데이터 적재(동기화)
  + schedule에 cron 표현식으로 5초마다 SELECT 문 실행하게 설정
  + statement에 SELECT 할 때 MySQL의 timeStamp를 tracking_column으로 설정하고 <br/>WHERE 조건에 이전에 적재한 timeStamp 값을 :sql_last_value 파라미터로 받아 활용하여 <br/>데이터가 누락되거나 중복되지 않게 적재
+ **[ 데이터 흐름 ]** 
  + 스프링 배치 => RDB(MySQL) 데이터 적재 => Logstash => ES 데이터 적재
> **[ Jenkins ]**
+ **[ CI/CD ]** 
  + gitHub 커밋 => Webhooks를 이용해 Jenkins로 보냄 => Jenkins에서 빌드 전 JUnit5 단위테스트 / 통합테스트 실행 자동화
  + 빌드 후 AWS 인스턴스에 접근해서 배포 자동화
+ **[ Build Periodically ]** 
  + 매일 00:00 모든 배치서버 STOP <br/>=> 00:10 Logstash, ES 메모리 부족으로 인한 성능저하 방지를 위해 재시작 <br/>=> 01:00 모든 배치서버 자동시작
+ **[ 복호화 ]** 
  + git-secret을 이용해 GPG 알고리즘(RSA 기반)으로 암호화된 파일을 PrivateKey를 credential로 저장 후 application.yml을 복호화
> **[ 보안 ]**
+ **[ 스프링 ]** 
  + 보안측면에서 유리한 세션 방식으로 로그인 정보 저장
  + 백엔드 서버에 요청 시 인터셉터 이용해서 세션을 통한 로그인 검증
  + 비밀번호 저장 시 랜덤한 4자리의 Salt 값 생성 후 (비밀번호 + Salt)를 단방향암호화 진행
  + SQL Injection 방지를 위해 myBatis #{} 바인딩 방식 사용
+ **[ AWS ]**
  + 보안그룹과 OpenVPN을 활용하여 허용되지 않은 IP접근 차단
------------
### 프로젝트는 어떤 서비스를 제공하나요?
+ 하루 최대 **100만건** 적재 가능한 서비스 구현(중복데이터 없고 20쓰레드 12시간 작동 및 API 호출 간 0.75초 Thread.sleep 기준)
+ **문자메시지 전송서비스**를 통해 특정 검색어에 해당하는 제품이 설정한 가격 이하라면 알림 제공
+ 어떠한 검색어든 가격 및 판매상품 개수 추이를 **시계열로 검색** 가능
------------
### 추후 수정하거나 개발하고 싶은 부분이 있나요?
+ 서버 성능저하 방지를 위해 권한검증방식을 기존의 세션방식에서 **JWT 방식**으로 변경
+ 적재된 데이터를 제공할 수 있는 **OPEN API 및 API 명세서** 제공 **(Rest API 적용)**
+ 현재 적용중인 nginx를 이용해 **무중단 배포** 구현
+ 쿠버네티스와 도커를 활용하여 **로드밸런싱** 적용
+ 서버 성능저하 방지를 위해 캐싱 목적의 **인메모리 DB** 구축
+ **크롤링IP 감지&차단**
+ 적재된 데이터를 활용한 **빅데이터분석 및 문서화** 구현
------------
### 프로젝트 설치 및 실행방법은 어떻게 되나요?
> 서버 설정 및 개인정보를 application.yml에 포함하고 있어서 현재 GIT에 올린 버전은 모두 **git-secret을 이용해 암호화**한 상태입니다.<br/>그러므로 로컬에서 실행하는 것이 아닌 **하단의 주소를 통해 서버에 접속**하여 확인해주세요.
------------
### 프로젝트 주소 및 스크린샷을 첨부해주세요.
+ **주소 : https://www.juroSpring.o-r.kr**
+ 블로그
  + https://believeme.tistory.com/entry/11%EB%B2%88%EA%B0%80-%EC%87%BC%ED%95%91%EB%8D%B0%EC%9D%B4%ED%84%B0-%ED%99%9C%EC%9A%A9%EC%84%9C%EB%B9%84%EC%8A%A4
+ 스크린샷
  + 검색화면
    <img width="901" alt="화면 캡처 2023-07-12 172721" src="https://github.com/ParkSungCheol/ShoppingMall_vue/assets/93702296/6beb4687-8c34-4db0-85fd-f66d3fbf69a0">
  + 통계화면(시계열검색)
    <img width="904" alt="화면 캡처 2023-07-12 172846" src="https://github.com/ParkSungCheol/ShoppingMall_vue/assets/93702296/dbfea2eb-c336-478f-9aa9-8701d9830349">
  + 문자메시지 전송시스템
    <img width="914" alt="화면 캡처 2023-07-12 172920" src="https://github.com/ParkSungCheol/ShoppingMall_vue/assets/93702296/f31d9f9f-78bd-4085-908a-0bf091a76054">
------------
