---
layout: post
title: "WindowManager4"
date: 2020-07-22 00:00:01
author: Keelim
categories: AOSP
comments: true
toc: true
toc_sticky: true
---

### 지난 주차 코드 

```java
public int afterTest() {
        final String TAG_WM = "WindowManager";
        final WindowState parentWindow;
        final DisplayContent displayContent;
        final WindowManager.LayoutParams attrs;
        final int type = attrs.type;
        final WindowToken atoken;
        final boolean hasParent = parentWindow != null;
        final int rootType = hasParent ? parentWindow.mAttrs.type : type;
        final WindowToken token = displayContent.getWindowToken(hasParent ? parentWindow.mAttrs.token : attrs.token);

        if (token == null) {
            if (rootType >= FIRST_APPLICATION_WINDOW && rootType <= LAST_APPLICATION_WINDOW) {
                Slog.w(TAG_WM, "s1" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }
            if (rootType == TYPE_INPUT_METHOD) {
                Slog.w(TAG_WM, "s2" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }
            if (rootType == TYPE_VOICE_INTERACTION) {
                Slog.w(TAG_WM, "s3" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }
            if (rootType == TYPE_WALLPAPER) {
                Slog.w(TAG_WM, "s4" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }
            if (rootType == TYPE_DREAM) {
                Slog.w(TAG_WM, "s5" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }
            if (rootType == TYPE_QS_DIALOG) {
                Slog.w(TAG_WM, "s6" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }
            if (rootType == TYPE_ACCESSIBILITY_OVERLAY) {
                Slog.w(TAG_WM, "s7" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }

            if (type == TYPE_TOAST) {
                // Apps targeting SDK above N MR1 cannot arbitrary add toast windows.
                if (doesAddToastWindowRequireToken(attrs.packageName, callingUid, parentWindow)) {
                    Slog.w(TAG_WM, "s15" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                }
            }

            final IBinder binder = attrs.token != null ? attrs.token : client.asBinder();
            final boolean isRoundedCornerOverlay = (attrs.privateFlags & PRIVATE_FLAG_IS_ROUNDED_CORNERS_OVERLAY) != 0;
            token = new WindowToken(this, binder, type, false, displayContent, session.mCanAddInternalSystemWindow, isRoundedCornerOverlay);

        } else if (rootType >= FIRST_APPLICATION_WINDOW && rootType <= LAST_APPLICATION_WINDOW) {
            atoken = token.asAppWindowToken();

            if (atoken == null) {
                Slog.w(TAG_WM, "Attempted to add window with non-application token " + token + "s0");
                return WindowManagerGlobal.ADD_NOT_APP_TOKEN;

            } else if (atoken.removed) {
                Slog.w(TAG_WM, "Attempted to add window with exiting application token " + token + "s0");
                return WindowManagerGlobal.ADD_APP_EXITING;

            } else if (type == TYPE_APPLICATION_STARTING && atoken.startingWindow != null) {
                Slog.w(TAG_WM, "Attempted to add starting window to token with already existing" + " starting window");
                return WindowManagerGlobal.ADD_DUPLICATE_ADD;

            }

        } else if (rootType == TYPE_INPUT_METHOD) {
            if (token.windowType != TYPE_INPUT_METHOD) {
                Slog.w(TAG_WM, "s8" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }
        } else if (rootType == TYPE_VOICE_INTERACTION) {
            if (token.windowType != TYPE_VOICE_INTERACTION) {
                Slog.w(TAG_WM, "s9" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }
        } else if (rootType == TYPE_WALLPAPER) {
            if (token.windowType != TYPE_WALLPAPER) {
                Slog.w(TAG_WM, "s10" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }
        } else if (rootType == TYPE_DREAM) {
            if (token.windowType != TYPE_DREAM) {
                Slog.w(TAG_WM, "s11" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }
        } else if (rootType == TYPE_ACCESSIBILITY_OVERLAY) {
            if (token.windowType != TYPE_ACCESSIBILITY_OVERLAY) {
                Slog.w(TAG_WM, "s12" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }
        } else if (type == TYPE_TOAST) {
            // Apps targeting SDK above N MR1 cannot arbitrary add toast windows.
            addToastWindowRequiresToken = doesAddToastWindowRequireToken(attrs.packageName, callingUid, parentWindow);
            if (addToastWindowRequiresToken && token.windowType != TYPE_TOAST) {
                Slog.w(TAG_WM, "s13" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }
        } else if (type == TYPE_QS_DIALOG) {
            if (token.windowType != TYPE_QS_DIALOG) {
                Slog.w(TAG_WM, "s14" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }
        } else if (token.asAppWindowToken() != null) {
            Slog.w(TAG_WM, "Non-null appWindowToken for system window of rootType=" + rootType);
            // It is not valid to use an app token with other system types; we will
            // instead make a new token for it (as if null had been passed in for the token).
            attrs.token = null;
            token = new WindowToken(this, client.asBinder(), type, false, displayContent, session.mCanAddInternalSystemWindow);
        }
        return 0;
    }
```

`WindowManagerService` 안에 있는 addWindow 부분 중 오류 처리를 담당을 하고 있는 부분이다. 

이에 이곳에서 if~else, nested if 등의 문제가 있어 이를 해결을 하기 위하여 EnumMap 을 사용을 하여

`static field` 를 참조하는 것과 enumMap 중 속도 향상 있는 것을 확인 한다. 


```java 
public int afterTest2() {
        final String TAG_WM = "WindowManager";
        final WindowState parentWindow;
        final DisplayContent displayContent;
        final WindowManager.LayoutParams attrs;
        final int type = attrs.type;
        final WindowToken atoken;
        final boolean hasParent = parentWindow != null;
        final int rootType = hasParent ? parentWindow.mAttrs.type : type;
        final WindowToken token = displayContent.getWindowToken(hasParent ? parentWindow.mAttrs.token : attrs.token);

        ///////    /////////////////////////////////////////////////////////////////////////////////////////////
        if (token == null) {
            if (rootType >= FIRST_APPLICATION_WINDOW && rootType <= LAST_APPLICATION_WINDOW) {
                Slog.w(TAG_WM, "s1" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }

            switch (rootType) {
                case TYPE_INPUT_METHOD:
                    Slog.w(TAG_WM, "s2" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                break;
                case TYPE_VOICE_INTERACTION:
                    Slog.w(TAG_WM, "s3" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                break;

                case TYPE_ACCESSIBILITY_OVERLAY:
                    Slog.w(TAG_WM, "s7" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                break;
                case TYPE_TOAST:
                    if (doesAddToastWindowRequireToken(attrs.packageName, callingUid, parentWindow)) {
                        Slog.w(TAG_WM, "s15" + attrs.token + "s0");
                        return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                    }
                    break;
                case TYPE_WALLPAPER:
                    Slog.w(TAG_WM, "s4" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                break;
                case TYPE_DREAM:
                    Slog.w(TAG_WM, "s5" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                break;

                case TYPE_QS_DIALOG:
                    Slog.w(TAG_WM, "s6" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                break;

            }
            final IBinder binder = attrs.token != null ? attrs.token : client.asBinder();
            final boolean isRoundedCornerOverlay = (attrs.privateFlags & PRIVATE_FLAG_IS_ROUNDED_CORNERS_OVERLAY) != 0;
            token = new WindowToken(this, binder, type, false, displayContent, session.mCanAddInternalSystemWindow, isRoundedCornerOverlay);

        } else if (rootType >= FIRST_APPLICATION_WINDOW && rootType <= LAST_APPLICATION_WINDOW) {
            atoken = token.asAppWindowToken();

            if (atoken == null) {
                Slog.w(TAG_WM, "Attempted to add window with non-application token " + token + "s0");
                return WindowManagerGlobal.ADD_NOT_APP_TOKEN;

            } else if (atoken.removed) {
                Slog.w(TAG_WM, "Attempted to add window with exiting application token " + token + "s0");
                return WindowManagerGlobal.ADD_APP_EXITING;

            } else if (type == TYPE_APPLICATION_STARTING && atoken.startingWindow != null) {
                Slog.w(TAG_WM, "Attempted to add starting window to token with already existing" + " starting window");
                return WindowManagerGlobal.ADD_DUPLICATE_ADD;

            }

        } else if (token.asAppWindowToken() != null) {
            Slog.w(TAG_WM, "Non-null appWindowToken for system window of rootType=" + rootType);
            // It is not valid to use an app token with other system types; we will
            // instead make a new token for it (as if null had been passed in for the token).
            attrs.token = null;
            token = new WindowToken(this, client.asBinder(), type, false, displayContent, session.mCanAddInternalSystemWindow);

        } else {
            switch (rootType) {
                case TYPE_INPUT_METHOD:
                    if (token.windowType != TYPE_INPUT_METHOD) {
                        Slog.w(TAG_WM, "s8" + attrs.token + "s0");
                        return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                    }
                    break;
                case TYPE_VOICE_INTERACTION:
                    if (token.windowType != TYPE_VOICE_INTERACTION) {
                        Slog.w(TAG_WM, "s9" + attrs.token + "s0");
                        return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                    }
                    break;
                case TYPE_WALLPAPER:
                    if (token.windowType != TYPE_WALLPAPER) {
                        Slog.w(TAG_WM, "s10" + attrs.token + "s0");
                        return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                    }
                    break;
                case TYPE_DREAM:
                    if (token.windowType != TYPE_DREAM) {
                        Slog.w(TAG_WM, "s11" + attrs.token + "s0");
                        return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                    }
                    break;
                case TYPE_ACCESSIBILITY_OVERLAY:
                    if (token.windowType != TYPE_ACCESSIBILITY_OVERLAY) {
                        Slog.w(TAG_WM, "s12" + attrs.token + "s0");
                        return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                    }
                    break;
                case TYPE_TOAST:
                    // Apps targeting SDK above N MR1 cannot arbitrary add toast windows.
                    addToastWindowRequiresToken = doesAddToastWindowRequireToken(attrs.packageName, callingUid, parentWindow);
                    if (addToastWindowRequiresToken && token.windowType != TYPE_TOAST) {
                        Slog.w(TAG_WM, "s13" + attrs.token + "s0");
                        return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                    }
                    break;
                case TYPE_QS_DIALOG:
                    if (token.windowType != TYPE_QS_DIALOG) {
                        Slog.w(TAG_WM, "s14" + attrs.token + "s0");
                        return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                    }
                    break;
            }
        }
        return 0;
    } //todo change switch case statement
```

위 코드는 우선 적정한 형태를 만들기 위하여 `switch - case statement`를 활용을 함으로써 enumMap 을 사용하기 편하도록 한다. 

또한 if 문이 중첩된 상황이라 판단을 하여 `switch - case statement` 를 사용을 하는 것이 좋을 것이라 판단했다. 
 
 ## EnumMap 의 사용
 
 ```java
 
public enum WindowEnum {

    FIRST_APPLICATION_WINDOW(1),
    LAST_APPLICATION_WINDOW(99),
    TYPE_TOAST(2005),
    TYPE_INPUT_METHOD(2011),
    TYPE_WALLPAPER(2013),
    TYPE_DREAM(2023),
    TYPE_VOICE_INTERACTION(2031),
    TYPE_ACCESSIBILITY_OVERLAY(2032),
    TYPE_QS_DIALOG(2035);


    int value;

    WindowEnum(int i) {
        this.value = i;
    }

    public int getValue() {
        return value;
    }
}

 ```
 
 enum 은 원래 순서화를 통하여 처리를 할 수 있게 하는 것으로써 이를 이용하여 키 값으로 넣어주고 값을 받는
 `EnumMap` 사용하고자 한다. 
 
 - 필드를 넣어주고 생성자에 알맞은 처리를 해주면 된다. 
 - 또한 값이 없을 경우 `,` 를 생략한다. 
 
 ```java
 // enumMap 초기화
 
         for(WindowEnum e : WindowEnum.values()){ // enumMap 초기화
            enumMap.put(e, e.value);
        }
 ```
 
 values() 함수를 이용을 하여 Enum 값을 받고 key, value 값을 넣어 줌으로써 Map 을 초기화 시킨다. 


```java
public int afterTest4() { // EnumMap @AfterEnum
        final String TAG_WM = "WindowManager";
        final WindowState parentWindow;
        final DisplayContent displayContent;
        final WindowManager.LayoutParams attrs;
        final int type = attrs.type;
        final WindowToken atoken;
        final boolean hasParent = parentWindow != null;
        final int rootType = hasParent ? parentWindow.mAttrs.type : type;
        final WindowToken token = displayContent.getWindowToken(hasParent ? parentWindow.mAttrs.token : attrs.token);
        final Map<WindowEnum, Integer> enumMap = new EnumMap<>(WindowEnum.class);

        for(WindowEnum e : WindowEnum.values()){ // enumMap 초기화
            enumMap.put(e, e.value);
        }


        /////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (token == null) {

            if (rootType >= enumMap.get(WindowEnum.FIRST_APPLICATION_WINDOW) && rootType <= enumMap.get(WindowEnum.LAST_APPLICATION_WINDOW)) {
                Slog.w(TAG_WM, "s1" + attrs.token + "s0");
                return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }

            switch (rootType) {
                case enumMap.get(WindowEnum.TYPE_INPUT_METHOD):
                    Slog.w(TAG_WM, "s2" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                break;
                case enumMap.get(WindowEnum.TYPE_VOICE_INTERACTION):
                    Slog.w(TAG_WM, "s3" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                break;

                case enumMap.get(WindowEnum.TYPE_ACCESSIBILITY_OVERLAY):
                    Slog.w(TAG_WM, "s7" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                break;
                case enumMap.get(WindowEnum.TYPE_TOAST):
                    if (doesAddToastWindowRequireToken(attrs.packageName, callingUid, parentWindow)) {
                        Slog.w(TAG_WM, "s15" + attrs.token + "s0");
                        return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                    }
                    break;
                case enumMap.get(WindowEnum.TYPE_WALLPAPER):
                    Slog.w(TAG_WM, "s4" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                break;
                case enumMap.get(WindowEnum.TYPE_DREAM):
                    Slog.w(TAG_WM, "s5" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                break;

                case enumMap.get(WindowEnum.TYPE_QS_DIALOG):
                    Slog.w(TAG_WM, "s6" + attrs.token + "s0");
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                break;

            }

            final IBinder binder = attrs.token != null ? attrs.token : client.asBinder();
            final boolean isRoundedCornerOverlay = (attrs.privateFlags & PRIVATE_FLAG_IS_ROUNDED_CORNERS_OVERLAY) != 0;
            token = new WindowToken(this, binder, type, false, displayContent, session.mCanAddInternalSystemWindow, isRoundedCornerOverlay);

        } else if (rootType >= enumMap.get(WindowEnum.FIRST_APPLICATION_WINDOW) && rootType <= enumMap.get(WindowEnum.LAST_APPLICATION_WINDOW)) {
            atoken = token.asAppWindowToken();

            if (atoken == null) {
                Slog.w(TAG_WM, "Attempted to add window with non-application token " + token + "s0");
                return WindowManagerGlobal.ADD_NOT_APP_TOKEN;

            } else if (atoken.removed) {
                Slog.w(TAG_WM, "Attempted to add window with exiting application token " + token + "s0");
                return WindowManagerGlobal.ADD_APP_EXITING;

            } else if (type == TYPE_APPLICATION_STARTING && atoken.startingWindow != null) {
                Slog.w(TAG_WM, "Attempted to add starting window to token with already existing" + " starting window");
                return WindowManagerGlobal.ADD_DUPLICATE_ADD;

            }

        } else if (token.asAppWindowToken() != null) {
            Slog.w(TAG_WM, "Non-null appWindowToken for system window of rootType=" + rootType);
            // It is not valid to use an app token with other system types; we will
            // instead make a new token for it (as if null had been passed in for the token).
            attrs.token = null;
            token = new WindowToken(this, client.asBinder(), type, false, displayContent, session.mCanAddInternalSystemWindow);

        } else {
            switch (rootType) {
                case enumMap.get(WindowEnum.TYPE_INPUT_METHOD):
                    if (token.windowType != TYPE_INPUT_METHOD) {
                        Slog.w(TAG_WM, "s8" + attrs.token + "s0");
                        return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                    }
                    break;
                case enumMap.get(WindowEnum.TYPE_VOICE_INTERACTION):
                    if (token.windowType != TYPE_VOICE_INTERACTION) {
                        Slog.w(TAG_WM, "s9" + attrs.token + "s0");
                        return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                    }
                    break;
                case enumMap.get(WindowEnum.TYPE_WALLPAPER):
                    if (token.windowType != TYPE_WALLPAPER) {
                        Slog.w(TAG_WM, "s10" + attrs.token + "s0");
                        return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                    }
                    break;
                case enumMap.get(WindowEnum.TYPE_DREAM):
                    if (token.windowType != TYPE_DREAM) {
                        Slog.w(TAG_WM, "s11" + attrs.token + "s0");
                        return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                    }
                    break;
                case enumMap.get(WindowEnum.TYPE_ACCESSIBILITY_OVERLAY):
                    if (token.windowType != TYPE_ACCESSIBILITY_OVERLAY) {
                        Slog.w(TAG_WM, "s12" + attrs.token + "s0");
                        return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                    }
                    break;
                case enumMap.get(WindowEnum.TYPE_TOAST):
                    // Apps targeting SDK above N MR1 cannot arbitrary add toast windows.
                    addToastWindowRequiresToken = doesAddToastWindowRequireToken(attrs.packageName, callingUid, parentWindow);
                    if (addToastWindowRequiresToken && token.windowType != TYPE_TOAST) {
                        Slog.w(TAG_WM, "s13" + attrs.token + "s0");
                        return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                    }
                    break;
                case enumMap.get(WindowEnum.TYPE_QS_DIALOG):
                    if (token.windowType != TYPE_QS_DIALOG) {
                        Slog.w(TAG_WM, "s14" + attrs.token + "s0");
                        return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
                    }
                    break;
            }
        }
        return 0;
    }
```
 
 `EnumMap`을 통하여 작성한 수정된 메소드 이다. 아직 `token` 이 없는 로직처리를 좋으나 
 
 `token`이 있는 로직은 수정이 필요할 것 같다.
 

### 🧶 모든 문서는 수정될 수 있습니다
