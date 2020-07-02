---
layout: post
title: "WindowManager1"
date: 2020-07-02 00:00:01
author: Keelim
categories: AOSP
comments: true
toc: true
toc_sticky: true
---

## WindowManager 1

이제부터 WindowManager 를 본격적으로 실행을 한다.
프로젝트에서 우리가 수행을 하는 것은 2가지로 나눌 수 있는 것 같다.
`성능 검증`, `빌드, 포팅 반복`

`성능 검증`의 경우 지금 WindowManager 관련 `class` 를 보고 중첩되는 `if - else`, `디자인 패턴 변경 가능성`
등을 찾아서 코드를 따라가면서 `//todo` 코멘트를 달면서 진행을 한다.

`빌드, 포팅 반복` 의 경우 실험 기기는 `pixel1` 인 구글 레펀런스 폰으로 최대한 포팅과정을 깔끔하게 했다.
다른 제조자 기기를 사용을 할 경우 `오딘`과 같은 것들 건드려야 해서 시간을 뺏길 것 같다.

### ⚠주의사항⚠

- 어노테이션은 안건드리는 것이 좋을 것 같다. -> 다른 라이브러리가 개입을 하는 부분일 수 있기 때문이다.
- 구글 레퍼런스 기기 (nexus 시리즈나, pixel 시리즈) 를 활용하는 것이 좋다. -> nexus 는 지원을 안하는 것일 수 있다.

### 시작

우선 연관되는 클래스를 전부 나열하기
우선 WindowMnager 는 `interface` 안에도 코드가 있기 때문에 포함을 한다.
todo 로 코멘트 남기면서 보았다.

![force_compile](https://github.com/keelim/AOSP/blob/master/docs/assets/todo0.png?raw=true)
![force_compile](https://github.com/keelim/AOSP/blob/master/docs/assets/todo1.png?raw=true)

---

#### application side

`interface` ViewManager

`interface` WindowManager

`class` ViewRootImpl

`class` Window

`class` WindowContentFraneStats

`class` WindowManageGlobal

`class` WindowManagerImpl

---

#### server side

`interface` IWindowManager

`interface` IWindowSession

`interface` IWindowSessionCallback

`class` WindowConfiguration

`class` WindowHashMap

`class` WindowList

`class` WindowManagerDebugConfig

`class` WindowManagerGlobalLock

`class` WindowManagerService

`class` WidwoMangerServiceDumpProto

### 생각하는 3가지 개선할 수 있는 것 같은 것

![force_compile](https://github.com/keelim/AOSP/blob/master/docs/assets/todo2.png?raw=true)

1. WindowHashMap --> EnumList 변환
![force_compile](https://github.com/keelim/AOSP/blob/master/docs/assets/todo_best1.png?raw=true)

2. WindowList --> ArrayList 확장 본인 생각은 그냥 queue
![force_compile](https://github.com/keelim/AOSP/blob/master/docs/assets/todo_best2.png?raw=true)

3. Annomymous `class` -> lambda 메모릭 방지 Runnable 직접 구현한 형태도 많이 보인다.  
![force_compile](https://github.com/keelim/AOSP/blob/master/docs/assets/todo_best3.png?raw=true)

---

### other section

1. application --> Object 처리
   server --> WindowManagerGlobalLock (just `class`) android bp 참조
![force_compile](https://github.com/keelim/AOSP/blob/master/docs/assets/todo_sub1.png?raw=true)
2. Generic 이상한 부분 컴파일러는 대단하다.
![force_compile](https://github.com/keelim/AOSP/blob/master/docs/assets/todo_sub2.png?raw=true)
3. Math.min 사용안하고 직접 구현
![force_compile](https://github.com/keelim/AOSP/blob/master/docs/assets/todo_sub3.png?raw=true)
4. 많은 if else 부분
5. 불필요한 if --> 바로 inline return
6. 번외) 텔레스코핑 패턴 (Rect `class`, WindowManager.LayoutParams)

### 참고문헌

- <https://meetup.toast.com/posts/186>

#### 🧶 모든 문서는 수정될 수 있습니다.
