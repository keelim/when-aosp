---
layout: post
title: "WindowManager 코드 본격 분석"
date: 2020-05-12 00:00:01
author: Keelim
categories: AOSP
comments: true
toc: true
toc_sticky: true
---

## WindowManager 대략적인 설명

각 WindowManager 인스턴스는 특정 디스플레이에 바인딩됩니다.
다른 디스플레이에 대한 WindowManager를 얻으려면 Context # createDisplayContext를 사용하여
해당 디스플레이에 대한 컨텍스트를 얻은 다음
`Context.getSystemService (Context.WINDOW_SERVICE)`를 사용하여 WindowManager를 가져옴


## 1

//wm 1
Window Manager
![Activity에서 실행](https://github.com/keelim/AOSP/blob/master/docs/assets/wm1.png?raw=true)

![구성](https://github.com/keelim/AOSP/blob/master/docs/assets/wm2.png?raw=true)
이번 분석에서 다루고자 하는 부분은 이 3가지의 클래스이다. `WindowManagerPolicyConstant` 클래스도 존재를 한다.

![ActivityManager](https://github.com/keelim/AOSP/blob/master/docs/assets/service4.png?raw=true)

이 과정을  `Window Manger`에서 다시 확인 일단은 바인더 아래 영역이 아니라 바인더 위에 자바 프레임워크 레벨까지만

## 2

지금 부터는 첨부된 코드는 안드로이드 sdk 에서 얻어온 코드입니다. `todo`를 따라가면서 코드를 보겠습니다.
위 sdk version 은 API 29를 참조를 하였습니다. (현재 `API 30 Preview` 공개)
![구성](https://github.com/keelim/AOSP/blob/master/docs/assets/wm3.png?raw=true)

getWindowMangerService() 메소드를 통하여 접근해보자
접근 전에 definition을 한번 보았을 때  `IBInder instance` 를 넘겨준다.
여기서 확인할 것은 서비스 매니저에서 `window` 관련 바인더를 찾아 넘겨준다는 것이다.
![그림](https://t1.daumcdn.net/cfile/tistory/247B334A56A6248B3A)

`서비스 매니저` 관련은 다음 포스트에서 설명

![구성](https://github.com/keelim/AOSP/blob/master/docs/assets/wm4.png?raw=true)

![구성](https://github.com/keelim/AOSP/blob/master/docs/assets/wm5.png?raw=true)

## 3

aidl IWindowManager에 대한 설명

![그림](https://t1.daumcdn.net/cfile/tistory/2361873754C292C81F)

안드로이드 aidl 파일을 이용을 하여서 프로세스 간의 IPC를 통해 상호 작용하는 코드를 만드는 인터페이스 정의 언어
따라서, 이를 이용하면 최종적으로 서비스 인터페잇, 서비스 프록시, 서비스 스텁 코드가 생성된다.

`서비스 인터페이스` 서비스 프록시와 서비스가 서로 동일한 인터페이스 공유하기 위한 함수 정의
서비스와 IBinder 사이에 형변화 기능 구현

`서비스 프록시` 바인더 RPC 를 위한 RPC 코드와 데이터 생성

![구성](https://github.com/keelim/AOSP/blob/master/docs/assets/wm10.png?raw=true)
IWindowManger.aidl -> IWindowManger.java

![구성](https://github.com/keelim/AOSP/blob/master/docs/assets/wm11.png?raw=true)

## 4

![구성](https://github.com/keelim/AOSP/blob/master/docs/assets/wm6.png?raw=true)

## 5

![구성](https://github.com/keelim/AOSP/blob/master/docs/assets/wm7.png?raw=true)

![구성](https://github.com/keelim/AOSP/blob/master/docs/assets/wm8.png?raw=true)

IWindowManger 에서 정의된 startViewServer 로 이동 하지만 인터페이스로 내용없다 -> 그럼 어디?

## 6

![구성](https://github.com/keelim/AOSP/blob/master/docs/assets/wm9.png?raw=true)

이번에 확인하고 싶은 것은 네이티브 단계 위인 `Java` 레벨에서 확인을 하고 싶었다.
다음 코드는 중간 과정인 WindwoManager  에서 Binder를 확인하자

### 참고

- [<https://developer.android.com/>]
- [<http://oss.kr/]>
- 인사이드 안드로이드
- [<https://cs.android.com/>]
