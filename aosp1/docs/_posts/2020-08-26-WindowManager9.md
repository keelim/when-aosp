---
layout: post
title: "WindowManager8"
date: 2020-08-26 00:00:01
author: Keelim
categories: AOSP
comments: true
toc: true---
layout: post
title: "WindowManager9"
date: 2020-08-26 00:00:01
author: Keelim
categories: AOSP
comments: true
toc: true
toc_sticky: true
---

## 커스텀 빌드 실행 결과

- build_id.mk 변경으로 정상적으로 포팅이 되었는지 확인 --> CNU_AOSP

## CopyFrom 결과

- 소스가 이상한 것 같아서 교체 --> 런타임 종료
- 개선 코드 이상한 것 같아서 새로 교체 --> 런타임 종료

### 결론

- final 변수로 잡아주면서 시스템 안전성의 기여를 하는 것 같다.

## frameworks/base/services/core/java/com/android/server/policy/PhoneWindowManager.java

### Inner class PolicyHandler

<script src="https://gist.github.com/keelim/70b8a0a6426d947eb41daa20f62c970b.js"></script>

<script src="https://gist.github.com/keelim/46e0fc6c6098b8c1dcff9828df1ade52.js"></script>

### 포팅하고 결과 실험 측정 할 것들

- 포팅하고 결과 측정
- HashMapVersion(1) > WindowManagerService.java -> addWindow multi if statement to HashMap
- HashMapVersion(2) > PhoneWindowManager.java -> inner class PolicyHandler to HashMap

## 일정

- 지금 까지 한 내용들 정리를 할 것(9월 1주) -> 따로 Github 만들어서

  - <https://github.com/cnuaosp>
  - 모아두는 내용 + 성능 측정 어플

- 나머지 실험들 진행을 할 것 --> (생각보다 많은 내용이 있으니)

  - 전부 포팅해서 실험은 할 수 있을 것 같다.

- HashMapVersion + Functional Interface Version 1
- HashMapVersion + Functional Interface Version 2
- copyFrom (not DisplayInfo)
- Runnalbe to lambda (메모리 성능 측정) -> 예시 자료는 있다. (실험은 할 수 있을 것 같다.)
- WindowManager.LayoutParams Telescoping pattern
- Math.min vs 삼항연산자

### 🧶 모든 문서는 수정될 수 있습니다
