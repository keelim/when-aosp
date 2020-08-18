---
layout: post
title: "WindowManager8"
date: 2020-08-19 00:00:01
author: Keelim
categories: AOSP
comments: true
toc: true
toc_sticky: true
---

## 커스텀 빌드 실행 결과 

-  build_id.mk 변경으로 정상적으로 포팅이 되었는지 확인  --> CNU_AOSP
-  <포팅 사진>


## 새로운 해결점 찾기

`addWindow` 에서의 오류를 처리를 하는 로직, 혹은 정책 클래스 PhoneWindowManagerPolicy
permissionCheck -> 굳이 바꿔야 하나

switch case statement -> 코드 다시 찾기 (C 부분 까지는 찾아볼 것)


## 일정
- 8월 까지는 최대한 작성을 하고 
- 9월 부터 논문을 작성하자.


### 🧶 모든 문서는 수정될 수 있습니다
