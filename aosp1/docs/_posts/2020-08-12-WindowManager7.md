---
layout: post
title: "WindowManager7"
date: 2020-08-12 00:00:01
author: Keelim
categories: AOSP
comments: true
toc: true
toc_sticky: true
---

## 새로운 해결점 찾기

`addWindow` 에서의 오류를 처리를 하는 로직, 혹은 정책 클래스 PhoneWindowManagerPolicy

<script src="https://gist.github.com/keelim/0aeed568c96e72ee45526b854cc65c7e.js"></script>

<script src="https://gist.github.com/keelim/96f6e67696be4e9dffb5b35e347a9872.js"></script>

<script src="https://gist.github.com/keelim/1c2df3193f774147348e1e08bd956f99.js"></script>

## 커스텀 빌드 실행 결과 

- 클라우드 환경에서 실행을 하고 out/target/sailfish -> img 파일을 가지고 온다. (boot, system, userdata, ramdisk, )
- fast boot를 활용을 하여 `pixel`의 포팅
- 동작 확인 -> 에뮬레이터 환경보다 좋다. (basic app으로 성능 측정 앱 설치할 것)

### 🧶 모든 문서는 수정될 수 있습니다
