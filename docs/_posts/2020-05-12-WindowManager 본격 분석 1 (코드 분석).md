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
`Context.getSystemService (Context.WINDOW_SERVICE)`를 사용하여 WindowManager를 가져옵니다.
--> 이 과정의 코드를 따라가고자 합니다.

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

## 3

## 4

## 5

## 6

## 7

## 8

## 9

## 10

## 11

### 참고

- [<https://developer.android.com/>]
- [<http://oss.kr/]>
- 인사이드 안드로이드
- [<https://cs.android.com/>]
