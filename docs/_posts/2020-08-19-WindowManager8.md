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

## 일정
- 8월 까지는 최대한 작성을 하고 
- 9월 부터 논문을 작성하자.


### 🧶 모든 문서는 수정될 수 있습니다
