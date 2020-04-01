---
layout: post
title: "WindowManger와 View System"
date: 2020-03-30 00:00:02
author: Keelim
categories: AOSP
comments: true
toc: true
toc_sticky: true
---

## WindowManger 와 View System

안드로이드 프레임워크에서 WindwoManger와 ViewSystem 에서 화면을 출력을 하고 관리를 함으로 이를 이용해서 개선사항이 있는지를 확인을 하기 위하여 분석을 하기로 하였다.

![텍스트](https://source.android.com/images/android_framework_details.png)

그림에서 알 수 있듯이 안드로이드 프레임워크 레벨에서는 화면을 관리를 하는 것으로 WindowManager와 ViewSystem 이 있다.

### WindowManger 와 ViewSystem 개요

- window manager는 Window를 요청하는 application에게 Surface를 생성
*윈도우즈 매니져는 네이티브 윈도우 시스템과 surface를 만들어준다. (egl을이용하여 skia나 opengl을 쓰기에 적합할 수 있도록 지원)

- view system은 Windows의 행동을 지원하는 시스템으로 단일 view 계층

- view 구조에서 새롭게 그릴 것이 있을때 (invalidate시..) , view 계층을 따라 surface안에서 새롭게 그려진다.

- canvas를 이용하여 surface에 view를 그린다.

- canvas는 surface에 그리는 방법을 알고 있다(bitmap, glcontainer...etc..)

- Surface manager가 Surface들을 레이어로 취급하며 합성한다....(z-order)

- HWC는 SurfaceFlinger의 job을 나누어 담당할 수 있는 레이어로 HAL의 사용여부와 IP의 종류는 사용자 마음.

- glSurfaceView는 백 스래드에서 ui를 업데이트 하는 view

- rendering을 하기 위해서는 glRenderer 가 필요하다


### 자바 API 지원하는 클래스

![windowmanger 클래스](/assets/windowmanger.png)

// window manger 설명하기

// 메서드 분석하기 
// View Manager 설명하기

### WindowManager 분석 시작
WindowManger 


### 향후 계획

window manger 좀 더 알아보기
안드로이드 빌드 환경 구성 (램, SSD)
안드로이드 넥서스 -> 개인 개발 + 테스트용

```java
Surface
egl, skia, opengl
canvas
HWC, SurfaceFlinger, glSurfaceView, glRenderer
```

출처: https://arabiannight.tistory.com/entry/343 [아라비안나이트]

