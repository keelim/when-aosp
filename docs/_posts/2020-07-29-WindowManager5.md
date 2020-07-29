---
layout: post
title: "WindowManager5"
date: 2020-07-28 00:00:01
author: Keelim
categories: AOSP
comments: true
toc: true
toc_sticky: true
---
## Java 함수형 인터페이스를 통해 초기화를 하고 이를 통해서 람다를 담을 수 있도록 한다.
기본 함수형 인터페이스
Runnable
Supplier
Consumer
Function<T, R>
Predicate

Runnable: Runnable은 인자를 받지 않고 리턴값도 없는 인터페이스

```java
public interface Runnable {
  public abstract void run();
}

Runnable runnable = () -> System.out.println("run anything!");
runnable.run();
// 결과
// run anything!
```

Runnable은 run()을 호출. 함수형 인터페이스마다 run() 비슷.

Supplier: Supplier<T>는 인자를 받지 않고 T 타입의 객체를 리턴

```java
public interface Supplier<T> {
    T get();
}
```

```java
Supplier<String> getString = () -> "Happy new year!";
String str = getString.get();
System.out.println(str);
// 결과
// Happy new year!
```

Consumer: Consumer<T>는 T 타입의 객체를 인자로 받고 리턴 값은 없음

```java
public interface Consumer<T> {
    void accept(T t);

    default Consumer<T> andThen(Consumer<? super T> after) {
        Objects.requireNonNull(after);
        return (T t) -> { accept(t); after.accept(t); };
    }
}

Consumer<String> printString = text -> System.out.println("Miss " + text + "?");
printString.accept("me");
// 결과
// 또한, andThen()을 사용하면 두개 이상의 Consumer를 연속적으로 실행할 수 있습니다.
```

```java
Consumer<String> printString = text -> System.out.println("Miss " + text + "?");
Consumer<String> printString2 = text -> System.out.println("--> Yes");
printString.andThen(printString2).accept("me");
// Miss me?
// --> Yes
```

Function Function<T, R>는 T타입의 인자 받고, R타입의 객체를 리턴
```
public interface Function<T, R> {
    R apply(T t);

    default <V> Function<V, R> compose(Function<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }

    default <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

    static <T> Function<T, T> identity() {
        return t -> t;
    }
}

Function<Integer, Integer> multiply = (value) -> value * 2;
Integer result = multiply.apply(3);
System.out.println(result);
// 결과
// 6
```

compose()는 두개의 Function을 조합하여 새로운 Function 객체를 만들어주는 메소드입니다. 
주의할 점은 andThen()과는 실행 순서가 반대입니다. 
compose()에 인자로 전달되는 Function이 먼저 수행되고 그 이후에 호출하는 객체의 Function이 수행됩니다.

예를들어, 다음과 같이 compose를 사용하여 새로운 Function을 만들 수 있습니다. apply를 호출하면 add 먼저 수행되고 그 이후에 multiply가 수행됩니다.

```
Function<Integer, Integer> multiply = (value) -> value * 2;
Function<Integer, Integer> add      = (value) -> value + 3;

Function<Integer, Integer> addThenMultiply = multiply.compose(add);

Integer result1 = addThenMultiply.apply(3);
System.out.println(result1);
// 결과
// 12
```
Predicate Predicate<T>는 T타입 인자를 받고 결과로 boolean을 리턴합니다.
```
public interface Predicate<T> {
    boolean test(T t);

    default Predicate<T> and(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) && other.test(t);
    }

    default Predicate<T> negate() {
        return (t) -> !test(t);
    }

    default Predicate<T> or(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) || other.test(t);
    }

    static <T> Predicate<T> isEqual(Object targetRef) {
        return (null == targetRef)
                ? Objects::isNull
                : object -> targetRef.equals(object);
    }
}
```

```java
Predicate<Integer> isBiggerThanFive = num -> num > 5;
System.out.println("10 is bigger than 5? -> " + isBiggerThanFive.test(10));
// 결과
// 10 is bigger than 5? -> true
and()와 or()는 다른 Predicate와 함께 사용됩니다. 
```

직관적으로 and()는 두개의 Predicate가 true일 때 true를 리턴하며 or()는 두개 중에 하나만 true이면 true를 리턴합니다.
```java
Predicate<Integer> isBiggerThanFive = num -> num > 5;
Predicate<Integer> isLowerThanSix = num -> num < 6;
System.out.println(isBiggerThanFive.and(isLowerThanSix).test(10));
System.out.println(isBiggerThanFive.or(isLowerThanSix).test(10));
// 결과
// false
// true
```

isEqual()은 static 메소드로, 인자로 전달되는 객체와 같은지 체크하는 Predicate 객체를 만들어 줍니다. 다음과 같이 사용할 수 있습니다.
```java
Predicate<String> isEquals = Predicate.isEqual("Google");
isEquals.test("Google");
// 결과
// true
```

정리
자바에서 기본적으로 제공하는 함수형 인터페이스에 대해서 알아보았습니다. 인터페이스마다 인자와 리턴타입이 다르고 이름이 다릅니다. 사용하는 목적에 맞게 이름을 지었습니다. 그렇기 때문에 실행하는 메소드 이름도 다릅니다.

- - -




## 로직 수정
switch case 문도, hashMap, enumMap

<script src="https://gist.github.com/keelim/2282e666f1a3b8fecdc03b74e5f6c59e.js"></script>


<script src="https://gist.github.com/keelim/e6a04c99c9002a91129127d0b993cfac.js"></script>


함수형 인터페이스를 활용을 하여 작성



초기화 코드 길어지고 구현 코드 짧아지고 

<script src="https://gist.github.com/keelim/0d614157d63a6159512e239a55652599.js"></script>




- - -


 

### 🧶 모든 문서는 수정될 수 있습니다
