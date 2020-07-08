---
layout: post
title: "WindowManager1"
date: 2020-07-08 00:00:01
author: Keelim
categories: AOSP
comments: true
toc: true
toc_sticky: true
---

## WindowManager2

저번에 발견한 레퍼런스를 토대로 해서 Anonymous class 와 lambda 를 사용을 함에 있어서
컴파일을 하여 smail를 바이트 코드로 확인을 하여 Memory leak의 대한 가능성을 확인을 한다.

`결론적으로 익명클래스와 람다는 는 동일하게 작동하지는 않는다.` 이제부터 그 결과를 조금씩 풀어나가면

우선은 개념은 Java 적으로 볼 때 Java 8 이 업데이트를 한 이 후 추가된 기능으로써 익명 함수를 정의하는
간편한 방법이다. 사용법은 함수형 인터페이스 `(단 하나의 메소드만이 선언된 인터페이스)` 만이 이를 람다식으로
전환을 할 수 있다.

필자 본인 또한 그저 치환을 하여 해석하는 것으로 이해를 하였으나 컴파일을 했을 때 결과는 우선 달랐다.
이를 안드로이드에서 확인을 하는 방법은 하나의 `Activity`를 만들고 어플리케이션은 apk 로 만들어서 그 안에 컴파일 코드를
보면 확인을 할 수 있다.

![force_compile](https://github.com/keelim/AOSP/blob/master/docs/assets/ru1.png?raw=true)

![force_compile](https://github.com/keelim/AOSP/blob/master/docs/assets/ru2.png?raw=true)

와 같이 activity를 구성을 하고 apk 를 만들어 이에 바이트 코드를 살펴보자

![force_compile](https://github.com/keelim/AOSP/blob/master/docs/assets/ru3.png?raw=true)

와 같이 `.class` 파일들이 만들어 지는 것을 확인 할 수 있다. 이를 살펴보자

## JavaLeakActivity

```java
.class public Lcom/keelim/aosp_code/JavaLeakActivity;
.super Landroidx/appcompat/app/AppCompatActivity;
.source "JavaLeakActivity.java"


# direct methods
.method public constructor <init>()V
    .registers 1

    .line 8
    invoke-direct {p0}, Landroidx/appcompat/app/AppCompatActivity;-><init>()V

    return-void
.end method

.method private task()V
    .registers 3

    .line 18
    new-instance v0, Lcom/keelim/aosp_code/JavaLeakActivity$1; //todo 인스턴스를 만들고  변수에 저장을 한다.

    invoke-direct {v0, p0}, Lcom/keelim/aosp_code/JavaLeakActivity$1;-><init>(Lcom/keelim/aosp_code/JavaLeakActivity;)V //만들어진 변수를 이용해서  초기화를 한다



    .line 25
    .local v0, "task":Ljava/lang/Runnable;
    new-instance v1, Ljava/lang/Thread;

    invoke-direct {v1, v0}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;)V

    invoke-virtual {v1}, Ljava/lang/Thread;->start()V

    .line 26
    return-void
.end method


# virtual methods
.method protected onCreate(Landroid/os/Bundle;)V
    .registers 3
    .param p1, "savedInstanceState"    # Landroid/os/Bundle;

    .line 11
    invoke-super {p0, p1}, Landroidx/appcompat/app/AppCompatActivity;->onCreate(Landroid/os/Bundle;)V

    .line 12
    const v0, 0x7f0b001c

    invoke-virtual {p0, v0}, Lcom/keelim/aosp_code/JavaLeakActivity;->setContentView(I)V

    .line 14
    invoke-direct {p0}, Lcom/keelim/aosp_code/JavaLeakActivity;->task()V

    .line 15
    return-void
.end method

```

## JavaLeakActivity\$1

```java
.class Lcom/keelim/aosp_code/JavaLeakActivity$1;
.super Ljava/lang/Object;
.source "JavaLeakActivity.java"

# interfaces
.implements Ljava/lang/Runnable;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/keelim/aosp_code/JavaLeakActivity;->task()V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic this$0:Lcom/keelim/aosp_code/JavaLeakActivity;


# direct methods
.method constructor <init>(Lcom/keelim/aosp_code/JavaLeakActivity;)V
    .registers 2
    .param p1, "this$0"    # Lcom/keelim/aosp_code/JavaLeakActivity;

    .line 18
    iput-object p1, p0, Lcom/keelim/aosp_code/JavaLeakActivity$1;->this$0:Lcom/keelim/aosp_code/JavaLeakActivity;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public run()V
    .registers 3

    .line 21
    const-wide/16 v0, 0x4e20

    invoke-static {v0, v1}, Landroid/os/SystemClock;->sleep(J)V

    .line 22
    return-void
.end method

```

## JavaLambdaLeakActivity

```java
   .class public Lcom/keelim/aosp_code/JavaLambdaLeakActivity;
.super Landroidx/appcompat/app/AppCompatActivity;
.source "JavaLambdaLeakActivity.java"


# direct methods
.method public constructor <init>()V
    .registers 1

    .line 8
    invoke-direct {p0}, Landroidx/appcompat/app/AppCompatActivity;-><init>()V

    return-void
.end method

.method static synthetic lambda$task$0()V
    .registers 2

    .line 19
    const-wide/16 v0, 0x4e20

    invoke-static {v0, v1}, Landroid/os/SystemClock;->sleep(J)V

    .line 20
    return-void
.end method

.method private task()V
    .registers 3

    .line 18
    sget-object v0, Lcom/keelim/aosp_code/-$$Lambda$JavaLambdaLeakActivity$6HQyu6yEK3hA26vJhBTjyZIVBPE;->INSTANCE:Lcom/keelim/aosp_code/-$$Lambda$JavaLambdaLeakActivity$6HQyu6yEK3hA26vJhBTjyZIVBPE;

    .line 22
    .local v0, "task":Ljava/lang/Runnable;
    new-instance v1, Ljava/lang/Thread;

    invoke-direct {v1, v0}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;)V

    invoke-virtual {v1}, Ljava/lang/Thread;->start()V

    .line 23
    return-void
.end method


# virtual methods
.method protected onCreate(Landroid/os/Bundle;)V
    .registers 3
    .param p1, "savedInstanceState"    # Landroid/os/Bundle;

    .line 11
    invoke-super {p0, p1}, Landroidx/appcompat/app/AppCompatActivity;->onCreate(Landroid/os/Bundle;)V

    .line 12
    const v0, 0x7f0b001c

    invoke-virtual {p0, v0}, Lcom/keelim/aosp_code/JavaLambdaLeakActivity;->setContentView(I)V

    .line 14
    invoke-direct {p0}, Lcom/keelim/aosp_code/JavaLambdaLeakActivity;->task()V

    .line 15
    return-void
.end method

```

## 2kind

```java
.class public final synthetic Lcom/keelim/aosp_code/-$$Lambda$JavaLambdaLeakActivity$6HQyu6yEK3hA26vJhBTjyZIVBPE;
.super Ljava/lang/Object;
.source "lambda"

# interfaces
.implements Ljava/lang/Runnable;


# static fields //todo static 임으로 외부 참조를 가지지 않는다.
.field public static final synthetic INSTANCE:Lcom/keelim/aosp_code/-$$Lambda$JavaLambdaLeakActivity$6HQyu6yEK3hA26vJhBTjyZIVBPE;


# direct methods
.method static synthetic constructor <clinit>()V
    .registers 1

    new-instance v0, Lcom/keelim/aosp_code/-$$Lambda$JavaLambdaLeakActivity$6HQyu6yEK3hA26vJhBTjyZIVBPE;

    invoke-direct {v0}, Lcom/keelim/aosp_code/-$$Lambda$JavaLambdaLeakActivity$6HQyu6yEK3hA26vJhBTjyZIVBPE;-><init>()V

    sput-object v0, Lcom/keelim/aosp_code/-$$Lambda$JavaLambdaLeakActivity$6HQyu6yEK3hA26vJhBTjyZIVBPE;->INSTANCE:Lcom/keelim/aosp_code/-$$Lambda$JavaLambdaLeakActivity$6HQyu6yEK3hA26vJhBTjyZIVBPE;

    return-void
.end method

.method private synthetic constructor <init>()V
    .registers 1

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public final run()V
    .registers 1

    invoke-static {}, Lcom/keelim/aosp_code/JavaLambdaLeakActivity;->lambda$task$0()V

    return-void
.end method

```

바이트 코드 주석을 따라가면서 읽어 보자

### 결론

결론적으로는 람다를 사용할 수 있는 환경이라면 메모리 관리 측면에서는 외부 레퍼런스를 받지 않기 때문에 가비지 컬렉터가
좀 더 나은 성능으로 작동할 것이다.

이에 WindowManager 관련 클래스 들이 람다와 Runnable 을 얼마나 혼용을 하고 있는지를 살펴보자

Runanble, lambda

이에 필자의 결론은 람다를 사용을 하면 프레임워크 측면에서
코드 가독성과 메모리 누수를 예방을 할 수 있을 것으로 생각한다.

메모리 누수를 확인을 할 수 있는 방법은 3가지 정도가 있다.

어플리케이션 구역에서는 Android LeackCanary 라이브러리, 안드로이드 제공하는 메모리 프로파일러

### 라이브러리 사진과 메모리 프로파일러

![canary](https://square.github.io/leakcanary/images/screenshot-2.0.png)

![profiler](https://developer.android.com/studio/images/profile/memory-profiler-callouts_2x.png?hl=ko)

시스템 구역에서는 adb 에서 제공하는 dumpy 가 있다.

// 덤파이 사진

```bash
   adb shell dumpys meminfo
```

![force_compile](https://github.com/keelim/AOSP/blob/master/docs/assets/ru4.png?raw=true)

필자의 생각은 이러한 메모리 누수가 일어날 경우 총체적인 안드로이드 프레임워크 OS 가 차지하는 램 용량이 상승할 것으로
판단을 하여 2개를 동시에 빌드 후 메모리 상태를 비교 하는 것이 좋을 것 같다.

### 꼭 클라우드 여쭤볼 것 제일 중요

### 참고문헌

- <https://meetup.toast.com/posts/186>

#### 🧶 모든 문서는 수정될 수 있습니다
