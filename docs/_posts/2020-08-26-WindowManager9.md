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
---

## 커스텀 빌드 실행 결과

- build_id.mk 변경으로 정상적으로 포팅이 되었는지 확인 --> CNU_AOSP

### 포팅과정

![force_compile](https://github.com/keelim/AOSP/blob/master/docs/assets/po1.png?raw=true)

```sh
@ECHO OFF
:: Copyright 2012 The Android Open Source Project
::
:: Licensed under the Apache License, Version 2.0 (the "License");
:: you may not use this file except in compliance with the License.
:: You may obtain a copy of the License at
::
::      http://www.apache.org/licenses/LICENSE-2.0
::
:: Unless required by applicable law or agreed to in writing, software
:: distributed under the License is distributed on an "AS IS" BASIS,
:: WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
:: See the License for the specific language governing permissions and
:: limitations under the License.

PATH=%PATH%;"%SYSTEMROOT%\System32"
echo play to update

fastboot reboot-bootloader
ping -n 5 127.0.0.1 >nul
fastboot -w update clone2.zip

echo Press any key to exit...
pause >nul
exit

```

![force_compile](https://github.com/keelim/AOSP/blob/master/docs/assets/1.gif?raw=true)

## clone 3차 수정 final과 관련한 할당 문제

- 이로 인해서 field : field 로 할당을 했다는 생각
- clone 을 하고 넘기려면 final 을 지워야 한다.
- 코드 중 특이한 점

<https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/view/DisplayInfo.java;drc=master;l=69?q=DIsplayInfo>

DisplayInfo.java line 295

```java
 public DisplayInfo(DisplayInfo other) {
        copyFrom(other);
    }
```

생성자와 copyFrom을 같이 사용할 수 있다는 아이디어

<script src="https://gist.github.com/keelim/5c78ca59e6f22598127dcf6fa2f292db.js"></script>

## 코드에서 볼 수 있듯이 clone 과 기존의 것을 같이 사용

- 만약 exception 이 나오는 경우 기존의 것을 활용을 할 수 있게 한다.

```java
// 기존의 사용한 방법

public class example {
    private final DisplayInfo mInfo = new DisplayInfo();

    void fun(){
        DisplayInfo newDisplayInfo = displayManagerInternal.getDisplayInfo(mDisplayId); // ����
        mInfo.copyFrom(newDisplayInfo);
    }
}
```

```java

public class example {
    private DisplayInfo mInfo = new DisplayInfo();

    void fun(){
        DisplayInfo newDisplayInfo = displayManagerInternal.getDisplayInfo(mDisplayId); // ����
        mInfo = mInfo.copyFrom(newDisplayInfo);
    }
}

```

### 예상되는 문제점

- final 로 인한 컴파일 부분 생길 수 있을 것 같다.<br>
  코드 다 찾아서 고쳐주면 된다. final 을 고치고 assign 을 해준다. <br>
  (`final 하나로 crash가 날까?`)

- 포팅관련 error -> 최대한 에러가 나지 않도록 기존의 함수를 활용
  (clone 실행이 되는 경우 Log 를 붙여서 확인을 할 필요가 있을 것 같다. )

### 포팅하고 결과 측정 할 것 -> Todo

- 결과 측정 툴 (dumpsys)

## 일정

- 8월 까지는 최대한 성능 향상점을 찾아보자 (네이티브 구간까지도 생각 해보자)
- 9월 부터 논문을 작성

### 🧶 모든 문서는 수정될 수 있습니다
