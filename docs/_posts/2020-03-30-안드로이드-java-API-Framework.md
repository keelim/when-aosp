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
- canvas를 이용하여 surface에 view를 그림.
- canvas는 surface에 그리는 방법을 알고 있다(bitmap, glcontainer...etc..)
- Surface manager가 Surface들을 레이어로 취급하며 합성....(z-order)
- HWC는 SurfaceFlinger의 job을 나누어 담당할 수 있는 레이어로 HAL의 사용여부와 IP의 종류는 사용자 마음.
- glSurfaceView는 백 스래드에서 ui를 업데이트 하는 view
- rendering을 하기 위해서는 glRenderer 가 필요

### 자바 API 지원하는 클래스

![windowmanger 클래스](https://raw.githubusercontent.com/keelim/AOSP/master/docs/assets/windowmanger.png)
위와 같이 WindowManger 가 구성이 되어 있다. Exception은 예외처리이기 떄문에 나중에 살펴보고
WindwoManger와 WindowManger.LayoutParans 를 살펴보기로 하였다.

### WindowManager 분석 시작

```java
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.view;

import android.annotation.NonNull;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewDebug.FlagToString;
import android.view.ViewDebug.IntToString;

public interface WindowManager extends ViewManager { // WindowManager로 되어 있으며 ViewPager 상속
    /** @deprecated */
    @Deprecated // android-R 에서 Deprecated 임으로 프로젝트 개선사항 고려 x
    Display getDefaultDisplay();

    void removeViewImmediate(View var1); //Window 에서 View를 삭제 한다.

    @NonNull
    default WindowMetrics getCurrentWindowMetrics() { //현재 윈도우 크기를 보여준다.
        throw new RuntimeException("Stub!");
    }

    @NonNull
    default WindowMetrics getMaximumWindowMetrics() { //윈도우의 최대 크기를 보여준다.
        throw new RuntimeException("Stub!");
    }

    public static class LayoutParams extends android.view.ViewGroup.LayoutParams implements Parcelable {
        // 내부 클래스로 LayoutParms 가 구현이 되어 있다.
        // 상수등을 이용해서 window 에서 어떻게 표시할지를 정한다.
        // 의문점: 중복이 있는 상수가 있는데 이는 무엇을 의미하는가?
        public static final int ALPHA_CHANGED = 128;
        public static final int ANIMATION_CHANGED = 16;
        public static final float BRIGHTNESS_OVERRIDE_FULL = 1.0F;
        public static final float BRIGHTNESS_OVERRIDE_NONE = -1.0F;
        public static final float BRIGHTNESS_OVERRIDE_OFF = 0.0F;
        @NonNull
        public static final Creator<WindowManager.LayoutParams> CREATOR = null;
        public static final int DIM_AMOUNT_CHANGED = 32;
        public static final int FIRST_APPLICATION_WINDOW = 1;
        public static final int FIRST_SUB_WINDOW = 1000;
        public static final int FIRST_SYSTEM_WINDOW = 2000;
        public static final int FLAGS_CHANGED = 4;
        public static final int FLAG_ALLOW_LOCK_WHILE_SCREEN_ON = 1;
        public static final int FLAG_ALT_FOCUSABLE_IM = 131072;
        /** @deprecated */
        @Deprecated
        public static final int FLAG_BLUR_BEHIND = 4;
        public static final int FLAG_DIM_BEHIND = 2;
        /** @deprecated */
        @Deprecated
        public static final int FLAG_DISMISS_KEYGUARD = 4194304;
        /** @deprecated */
        @Deprecated
        public static final int FLAG_DITHER = 4096;
        public static final int FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS = -2147483648;
        /** @deprecated */
        @Deprecated
        public static final int FLAG_FORCE_NOT_FULLSCREEN = 2048;
        /** @deprecated */
        @Deprecated
        public static final int FLAG_FULLSCREEN = 1024;
        public static final int FLAG_HARDWARE_ACCELERATED = 16777216;
        public static final int FLAG_IGNORE_CHEEK_PRESSES = 32768;
        public static final int FLAG_KEEP_SCREEN_ON = 128;
        /** @deprecated */
        @Deprecated
        public static final int FLAG_LAYOUT_ATTACHED_IN_DECOR = 1073741824;
        /** @deprecated */
        @Deprecated
        public static final int FLAG_LAYOUT_INSET_DECOR = 65536;
        /** @deprecated */
        @Deprecated
        public static final int FLAG_LAYOUT_IN_OVERSCAN = 33554432;
        public static final int FLAG_LAYOUT_IN_SCREEN = 256;
        public static final int FLAG_LAYOUT_NO_LIMITS = 512;
        public static final int FLAG_LOCAL_FOCUS_MODE = 268435456;
        public static final int FLAG_NOT_FOCUSABLE = 8;
        public static final int FLAG_NOT_TOUCHABLE = 16;
        public static final int FLAG_NOT_TOUCH_MODAL = 32;
        public static final int FLAG_SCALED = 16384;
        public static final int FLAG_SECURE = 8192;
        public static final int FLAG_SHOW_WALLPAPER = 1048576;
        /** @deprecated */
        @Deprecated
        public static final int FLAG_SHOW_WHEN_LOCKED = 524288;
        public static final int FLAG_SPLIT_TOUCH = 8388608;
        /** @deprecated */
        @Deprecated
        public static final int FLAG_TOUCHABLE_WHEN_WAKING = 64;
        /** @deprecated */
        @Deprecated
        public static final int FLAG_TRANSLUCENT_NAVIGATION = 134217728;
        /** @deprecated */
        @Deprecated
        public static final int FLAG_TRANSLUCENT_STATUS = 67108864;
        /** @deprecated */
        @Deprecated
        public static final int FLAG_TURN_SCREEN_ON = 2097152;
        public static final int FLAG_WATCH_OUTSIDE_TOUCH = 262144;
        public static final int FORMAT_CHANGED = 8;
        public static final int LAST_APPLICATION_WINDOW = 99;
        public static final int LAST_SUB_WINDOW = 1999;
        public static final int LAST_SYSTEM_WINDOW = 2999;
        public static final int LAYOUT_CHANGED = 1;
        public static final int LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS = 3;
        public static final int LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT = 0;
        public static final int LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER = 2;
        public static final int LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES = 1;
        public static final int MEMORY_TYPE_CHANGED = 256;
        /** @deprecated */
        @Deprecated
        public static final int MEMORY_TYPE_GPU = 2;
        //todo 메모리 타입에 대한 옵션? (Go와 연관 지을 수 있을까?)
        //혹은 다른 최적화 방안이라도
        /** @deprecated */
        @Deprecated
        public static final int MEMORY_TYPE_HARDWARE = 1;
        /** @deprecated */
        @Deprecated
        public static final int MEMORY_TYPE_NORMAL = 0;
        /** @deprecated */
        @Deprecated
        public static final int MEMORY_TYPE_PUSH_BUFFERS = 3;
        public static final int ROTATION_ANIMATION_CHANGED = 4096;
        public static final int ROTATION_ANIMATION_CROSSFADE = 1;
        public static final int ROTATION_ANIMATION_JUMPCUT = 2;
        public static final int ROTATION_ANIMATION_ROTATE = 0;
        public static final int ROTATION_ANIMATION_SEAMLESS = 3;
        public static final int SCREEN_BRIGHTNESS_CHANGED = 2048;
        public static final int SCREEN_ORIENTATION_CHANGED = 1024;
        public static final int SOFT_INPUT_ADJUST_NOTHING = 48;
        public static final int SOFT_INPUT_ADJUST_PAN = 32;
        /** @deprecated */
        @Deprecated
        public static final int SOFT_INPUT_ADJUST_RESIZE = 16;
        public static final int SOFT_INPUT_ADJUST_UNSPECIFIED = 0;
        public static final int SOFT_INPUT_IS_FORWARD_NAVIGATION = 256;
        public static final int SOFT_INPUT_MASK_ADJUST = 240;
        public static final int SOFT_INPUT_MASK_STATE = 15;
        public static final int SOFT_INPUT_MODE_CHANGED = 512;
        public static final int SOFT_INPUT_STATE_ALWAYS_HIDDEN = 3;
        public static final int SOFT_INPUT_STATE_ALWAYS_VISIBLE = 5;
        public static final int SOFT_INPUT_STATE_HIDDEN = 2;
        public static final int SOFT_INPUT_STATE_UNCHANGED = 1;
        public static final int SOFT_INPUT_STATE_UNSPECIFIED = 0;
        public static final int SOFT_INPUT_STATE_VISIBLE = 4;
        public static final int TITLE_CHANGED = 64;
        public static final int TYPE_ACCESSIBILITY_OVERLAY = 2032;
        public static final int TYPE_APPLICATION = 2;
        public static final int TYPE_APPLICATION_ATTACHED_DIALOG = 1003;
        public static final int TYPE_APPLICATION_MEDIA = 1001;
        public static final int TYPE_APPLICATION_OVERLAY = 2038;
        public static final int TYPE_APPLICATION_PANEL = 1000;
        public static final int TYPE_APPLICATION_STARTING = 3;
        public static final int TYPE_APPLICATION_SUB_PANEL = 1002;
        public static final int TYPE_BASE_APPLICATION = 1;
        public static final int TYPE_CHANGED = 2;
        public static final int TYPE_DRAWN_APPLICATION = 4;
        public static final int TYPE_INPUT_METHOD = 2011;
        public static final int TYPE_INPUT_METHOD_DIALOG = 2012;
        public static final int TYPE_KEYGUARD_DIALOG = 2009;
        /** @deprecated */
        @Deprecated
        public static final int TYPE_PHONE = 2002;
        /** @deprecated */
        @Deprecated
        public static final int TYPE_PRIORITY_PHONE = 2007;
        public static final int TYPE_PRIVATE_PRESENTATION = 2030;
        public static final int TYPE_SEARCH_BAR = 2001;
        public static final int TYPE_STATUS_BAR = 2000;
        public static final int TYPE_STATUS_BAR_PANEL = 2014;
        /** @deprecated */
        @Deprecated
        public static final int TYPE_SYSTEM_ALERT = 2003;
        public static final int TYPE_SYSTEM_DIALOG = 2008;
        /** @deprecated */
        @Deprecated
        public static final int TYPE_SYSTEM_ERROR = 2010;
        /** @deprecated */
        @Deprecated
        public static final int TYPE_SYSTEM_OVERLAY = 2006;
        /** @deprecated */
        @Deprecated
        public static final int TYPE_TOAST = 2005;
        public static final int TYPE_WALLPAPER = 2013;
        public float alpha = 1.0F;
        public float buttonBrightness = -1.0F;
        public float dimAmount = 1.0F;
        @ExportedProperty(
            flagMapping = {@FlagToString(
    mask = 1,
    equals = 1,
    name = "ALLOW_LOCK_WHILE_SCREEN_ON"
), @FlagToString(
    mask = 2,
    equals = 2,
    name = "DIM_BEHIND"
), @FlagToString(
    mask = 4,
    equals = 4,
    name = "BLUR_BEHIND"
), @FlagToString(
    mask = 8,
    equals = 8,
    name = "NOT_FOCUSABLE"
), @FlagToString(
    mask = 16,
    equals = 16,
    name = "NOT_TOUCHABLE"
), @FlagToString(
    mask = 32,
    equals = 32,
    name = "NOT_TOUCH_MODAL"
), @FlagToString(
    mask = 64,
    equals = 64,
    name = "TOUCHABLE_WHEN_WAKING"
), @FlagToString(
    mask = 128,
    equals = 128,
    name = "KEEP_SCREEN_ON"
), @FlagToString(
    mask = 256,
    equals = 256,
    name = "LAYOUT_IN_SCREEN"
), @FlagToString(
    mask = 512,
    equals = 512,
    name = "LAYOUT_NO_LIMITS"
), @FlagToString(
    mask = 1024,
    equals = 1024,
    name = "FULLSCREEN"
), @FlagToString(
    mask = 2048,
    equals = 2048,
    name = "FORCE_NOT_FULLSCREEN"
), @FlagToString(
    mask = 4096,
    equals = 4096,
    name = "DITHER"
), @FlagToString(
    mask = 8192,
    equals = 8192,
    name = "SECURE"
), @FlagToString(
    mask = 16384,
    equals = 16384,
    name = "SCALED"
), @FlagToString(
    mask = 32768,
    equals = 32768,
    name = "IGNORE_CHEEK_PRESSES"
), @FlagToString(
    mask = 65536,
    equals = 65536,
    name = "LAYOUT_INSET_DECOR"
), @FlagToString(
    mask = 131072,
    equals = 131072,
    name = "ALT_FOCUSABLE_IM"
), @FlagToString(
    mask = 262144,
    equals = 262144,
    name = "WATCH_OUTSIDE_TOUCH"
), @FlagToString(
    mask = 524288,
    equals = 524288,
    name = "SHOW_WHEN_LOCKED"
), @FlagToString(
    mask = 1048576,
    equals = 1048576,
    name = "SHOW_WALLPAPER"
), @FlagToString(
    mask = 2097152,
    equals = 2097152,
    name = "TURN_SCREEN_ON"
), @FlagToString(
    mask = 4194304,
    equals = 4194304,
    name = "DISMISS_KEYGUARD"
), @FlagToString(
    mask = 8388608,
    equals = 8388608,
    name = "SPLIT_TOUCH"
), @FlagToString(
    mask = 16777216,
    equals = 16777216,
    name = "HARDWARE_ACCELERATED"
), @FlagToString(
    mask = 33554432,
    equals = 33554432,
    name = "LOCAL_FOCUS_MODE"
), @FlagToString(
    mask = 67108864,
    equals = 67108864,
    name = "TRANSLUCENT_STATUS"
), @FlagToString(
    mask = 134217728,
    equals = 134217728,
    name = "TRANSLUCENT_NAVIGATION"
), @FlagToString(
    mask = 268435456,
    equals = 268435456,
    name = "LOCAL_FOCUS_MODE"
), @FlagToString(
    mask = 536870912,
    equals = 536870912,
    name = "FLAG_SLIPPERY"
), @FlagToString(
    mask = 1073741824,
    equals = 1073741824,
    name = "FLAG_LAYOUT_ATTACHED_IN_DECOR"
), @FlagToString(
    mask = -2147483648,
    equals = -2147483648,
    name = "DRAWS_SYSTEM_BAR_BACKGROUNDS"
)},
            formatToHexString = true
        )
        public int flags;
        public int format;
        public int gravity;
        public float horizontalMargin;
        @ExportedProperty
        public float horizontalWeight;
        public int layoutInDisplayCutoutMode = 0;
        /** @deprecated */
        @Deprecated
        public int memoryType;
        public String packageName;
        public boolean preferMinimalPostProcessing = false;
        public int preferredDisplayModeId;
        /** @deprecated */
        @Deprecated
        public float preferredRefreshRate;
        public int rotationAnimation = 0;
        public float screenBrightness = -1.0F;
        public int screenOrientation = -1;
        public int softInputMode;
        /** @deprecated */
        @Deprecated
        public int systemUiVisibility;
        public IBinder token;
        @ExportedProperty(
            mapping = {@IntToString(
    from = 1,
    to = "BASE_APPLICATION"
), @IntToString(
    from = 2,
    to = "APPLICATION"
), @IntToString(
    from = 3,
    to = "APPLICATION_STARTING"
), @IntToString(
    from = 4,
    to = "DRAWN_APPLICATION"
), @IntToString(
    from = 1000,
    to = "APPLICATION_PANEL"
), @IntToString(
    from = 1001,
    to = "APPLICATION_MEDIA"
), @IntToString(
    from = 1002,
    to = "APPLICATION_SUB_PANEL"
), @IntToString(
    from = 1005,
    to = "APPLICATION_ABOVE_SUB_PANEL"
), @IntToString(
    from = 1003,
    to = "APPLICATION_ATTACHED_DIALOG"
), @IntToString(
    from = 1004,
    to = "APPLICATION_MEDIA_OVERLAY"
), @IntToString(
    from = 2000,
    to = "STATUS_BAR"
), @IntToString(
    from = 2001,
    to = "SEARCH_BAR"
), @IntToString(
    from = 2002,
    to = "PHONE"
), @IntToString(
    from = 2003,
    to = "SYSTEM_ALERT"
), @IntToString(
    from = 2005,
    to = "TOAST"
), @IntToString(
    from = 2006,
    to = "SYSTEM_OVERLAY"
), @IntToString(
    from = 2007,
    to = "PRIORITY_PHONE"
), @IntToString(
    from = 2008,
    to = "SYSTEM_DIALOG"
), @IntToString(
    from = 2009,
    to = "KEYGUARD_DIALOG"
), @IntToString(
    from = 2010,
    to = "SYSTEM_ERROR"
), @IntToString(
    from = 2011,
    to = "INPUT_METHOD"
), @IntToString(
    from = 2012,
    to = "INPUT_METHOD_DIALOG"
), @IntToString(
    from = 2013,
    to = "WALLPAPER"
), @IntToString(
    from = 2014,
    to = "STATUS_BAR_PANEL"
), @IntToString(
    from = 2015,
    to = "SECURE_SYSTEM_OVERLAY"
), @IntToString(
    from = 2016,
    to = "DRAG"
), @IntToString(
    from = 2017,
    to = "STATUS_BAR_SUB_PANEL"
), @IntToString(
    from = 2018,
    to = "POINTER"
), @IntToString(
    from = 2019,
    to = "NAVIGATION_BAR"
), @IntToString(
    from = 2020,
    to = "VOLUME_OVERLAY"
), @IntToString(
    from = 2021,
    to = "BOOT_PROGRESS"
), @IntToString(
    from = 2022,
    to = "INPUT_CONSUMER"
), @IntToString(
    from = 2023,
    to = "DREAM"
), @IntToString(
    from = 2024,
    to = "NAVIGATION_BAR_PANEL"
), @IntToString(
    from = 2026,
    to = "DISPLAY_OVERLAY"
), @IntToString(
    from = 2027,
    to = "MAGNIFICATION_OVERLAY"
), @IntToString(
    from = 2037,
    to = "PRESENTATION"
), @IntToString(
    from = 2030,
    to = "PRIVATE_PRESENTATION"
), @IntToString(
    from = 2031,
    to = "VOICE_INTERACTION"
), @IntToString(
    from = 2033,
    to = "VOICE_INTERACTION_STARTING"
), @IntToString(
    from = 2034,
    to = "DOCK_DIVIDER"
), @IntToString(
    from = 2035,
    to = "QS_DIALOG"
), @IntToString(
    from = 2036,
    to = "SCREENSHOT"
), @IntToString(
    from = 2038,
    to = "APPLICATION_OVERLAY"
)}
        )
        public int type;
        public float verticalMargin;
        @ExportedProperty
        public float verticalWeight;
        public int windowAnimations;
        @ExportedProperty
        public int x;
        @ExportedProperty
        public int y;

        public LayoutParams() { //todo 생성자 영역
            super((android.view.ViewGroup.LayoutParams)null);
            throw new RuntimeException("Stub!");
        }

        public LayoutParams(int _type) {
            super((android.view.ViewGroup.LayoutParams)null);
            throw new RuntimeException("Stub!");
        }

        public LayoutParams(int _type, int _flags) {
            super((android.view.ViewGroup.LayoutParams)null);
            throw new RuntimeException("Stub!");
        }

        public LayoutParams(int _type, int _flags, int _format) {
            super((android.view.ViewGroup.LayoutParams)null);
            throw new RuntimeException("Stub!");
        }

        public LayoutParams(int w, int h, int _type, int _flags, int _format) {
            super((android.view.ViewGroup.LayoutParams)null);
            throw new RuntimeException("Stub!");
        }

        public LayoutParams(int w, int h, int xpos, int ypos, int _type, int _flags, int _format) {
            super((android.view.ViewGroup.LayoutParams)null);
            throw new RuntimeException("Stub!");
        }

        public LayoutParams(Parcel in) {
            super((android.view.ViewGroup.LayoutParams)null);
            throw new RuntimeException("Stub!");
        }

        public static boolean mayUseInputMethod(int flags) {
            throw new RuntimeException("Stub!");
        }

        public void setFitInsetsTypes(int types) {
            throw new RuntimeException("Stub!");
        }

        public void setFitInsetsSides(int sides) {
            throw new RuntimeException("Stub!");
        }

        public void setFitInsetsIgnoringVisibility(boolean ignore) {
            throw new RuntimeException("Stub!");
        }

        public int getFitInsetsTypes() {
            throw new RuntimeException("Stub!");
        }

        public int getFitInsetsSides() {
            throw new RuntimeException("Stub!");
        }

        public boolean isFitInsetsIgnoringVisibility() {
            throw new RuntimeException("Stub!");
        }

        public final void setTitle(CharSequence title) {
            throw new RuntimeException("Stub!");
        }

        public final CharSequence getTitle() {
            throw new RuntimeException("Stub!");
        }

        public void setColorMode(int colorMode) {
            throw new RuntimeException("Stub!");
        }

        public int getColorMode() {
            throw new RuntimeException("Stub!");
        }

        public int describeContents() {
            throw new RuntimeException("Stub!");
        }

        public void writeToParcel(Parcel out, int parcelableFlags) {
            throw new RuntimeException("Stub!");
        }

        public final int copyFrom(WindowManager.LayoutParams o) {
            throw new RuntimeException("Stub!");
        }

        public String debug(String output) {
            throw new RuntimeException("Stub!");
        }

        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    public static class InvalidDisplayException extends RuntimeException {
        public InvalidDisplayException() {
            throw new RuntimeException("Stub!");
        }

        public InvalidDisplayException(String name) {
            throw new RuntimeException("Stub!");
        }
    }

    public static class BadTokenException extends RuntimeException {
        public BadTokenException() {
            throw new RuntimeException("Stub!");
        }

        public BadTokenException(String name) {
            throw new RuntimeException("Stub!");
        }
    }
}

```

//todo WindowManagerImpl --> 인터페이스 구현한곳

```java
/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.view;
import android.annotation.NonNull;
import android.compat.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.graphics.Region;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import com.android.internal.os.IResultReceiver;
import java.util.List;
/**
 * Provides low-level communication with the system window manager for
 * operations that are bound to a particular context, display or parent window.
 * Instances of this object are sensitive to the compatibility info associated
 * with the running application.
 *
 * This object implements the {@link ViewManager} interface,
 * allowing you to add any View subclass as a top-level window on the screen.
 * Additional window manager specific layout parameters are defined for
 * control over how windows are displayed.  It also implements the {@link WindowManager}
 * interface, allowing you to control the displays attached to the device.
 *
 * <p>Applications will not normally use WindowManager directly, instead relying
 * on the higher-level facilities in {@link android.app.Activity} and
 * {@link android.app.Dialog}.
 *
 * <p>Even for low-level window manager access, it is almost never correct to use
 * this class.  For example, {@link android.app.Activity#getWindowManager}
 * provides a window manager for adding windows that are associated with that
 * activity -- the window manager will not normally allow you to add arbitrary
 * windows that are not associated with an activity.
 *
 * @see WindowManager
 * @see WindowManagerGlobal
 * @hide
 */
public final class WindowManagerImpl implements WindowManager {
    @UnsupportedAppUsage
    private final WindowManagerGlobal mGlobal = WindowManagerGlobal.getInstance();
    private final Context mContext;
    private final Window mParentWindow;
    private IBinder mDefaultToken; //바인더 개념 다시 체크
    public WindowManagerImpl(Context context) {
        this(context, null);
    }
    private WindowManagerImpl(Context context, Window parentWindow) { //컨텍스트는 액티비티에서 사용하는 것
        mContext = context;
        mParentWindow = parentWindow;
    }
    public WindowManagerImpl createLocalWindowManager(Window parentWindow) {
        return new WindowManagerImpl(mContext, parentWindow);
    }
    public WindowManagerImpl createPresentationWindowManager(Context displayContext) {
        return new WindowManagerImpl(displayContext, mParentWindow);
    }
    /**
     * Sets the window token to assign when none is specified by the client or
     * available from the parent window.
     *
     * @param token The default token to assign.
     */
    public void setDefaultToken(IBinder token) {
        mDefaultToken = token;
    }
    @Override
    public void addView(@NonNull View view, @NonNull ViewGroup.LayoutParams params) {
        applyDefaultToken(params);
        mGlobal.addView(view, params, mContext.getDisplay(), mParentWindow);
    }
    @Override
    public void updateViewLayout(@NonNull View view, @NonNull ViewGroup.LayoutParams params) {
        applyDefaultToken(params);
        mGlobal.updateViewLayout(view, params);
    }
    private void applyDefaultToken(@NonNull ViewGroup.LayoutParams params) {
        // Only use the default token if we don't have a parent window.
        if (mDefaultToken != null && mParentWindow == null) {
            if (!(params instanceof WindowManager.LayoutParams)) {
                throw new IllegalArgumentException("Params must be WindowManager.LayoutParams");
            }
            // Only use the default token if we don't already have a token.
            final WindowManager.LayoutParams wparams = (WindowManager.LayoutParams) params;
            if (wparams.token == null) {
                wparams.token = mDefaultToken;
            }
        }
    }
    @Override
    public void removeView(View view) {
        mGlobal.removeView(view, false);
    }
    @Override
    public void removeViewImmediate(View view) { //WindowManager
        mGlobal.removeView(view, true);
    }
    @Override
    public void requestAppKeyboardShortcuts(
            final KeyboardShortcutsReceiver receiver, int deviceId) {
        IResultReceiver resultReceiver = new IResultReceiver.Stub() {
            @Override
            public void send(int resultCode, Bundle resultData) throws RemoteException {
                List<KeyboardShortcutGroup> result =
                        resultData.getParcelableArrayList(PARCEL_KEY_SHORTCUTS_ARRAY);
                receiver.onKeyboardShortcutsReceived(result);
            }
        };
        try {
            WindowManagerGlobal.getWindowManagerService()
                .requestAppKeyboardShortcuts(resultReceiver, deviceId);
        } catch (RemoteException e) {
        }
    }

    //todo ? 굳이 catch 영역 쓰지도 않는데 적어야 하나? // 직렬화 통신관련 예외
    @Override
    public Display getDefaultDisplay() { //WindowManager
        return mContext.getDisplay();
    }
    @Override
    public Region getCurrentImeTouchRegion() {
        try {
            return WindowManagerGlobal.getWindowManagerService().getCurrentImeTouchRegion();
        } catch (RemoteException e) {
        }
        return null;
    }
    @Override
    public void setShouldShowWithInsecureKeyguard(int displayId, boolean shouldShow) {
        try {
            WindowManagerGlobal.getWindowManagerService()
                    .setShouldShowWithInsecureKeyguard(displayId, shouldShow);
        } catch (RemoteException e) {
        }
    }
    @Override
    public void setShouldShowSystemDecors(int displayId, boolean shouldShow) {
        try {
            WindowManagerGlobal.getWindowManagerService()
                    .setShouldShowSystemDecors(displayId, shouldShow);
        } catch (RemoteException e) {
        }
    }
    @Override
    public boolean shouldShowSystemDecors(int displayId) {
        try {
            return WindowManagerGlobal.getWindowManagerService().shouldShowSystemDecors(displayId);
        } catch (RemoteException e) {
        }
        return false;
    }
    @Override
    public void setShouldShowIme(int displayId, boolean shouldShow) {
        try {
            WindowManagerGlobal.getWindowManagerService().setShouldShowIme(displayId, shouldShow);
        } catch (RemoteException e) {
        }
    }
    @Override
    public boolean shouldShowIme(int displayId) {
        try {
            return WindowManagerGlobal.getWindowManagerService().shouldShowIme(displayId);
        } catch (RemoteException e) {
        }
        return false;
    }
}
```

### WindowGlobal

![WindowGlobal](https://raw.githubusercontent.com/keelim/AOSP/master/docs/assets/windowglobal1.png)

### Binder

각각의 독립된 프로세스를 연결해주는 역할을 하는 것 (IPC)
![Binder](http://oss.kr/oss/images/news/000000005592-0003.jpg)

### 앞으로

window manger 좀 더 알아보기
안드로이드 빌드 병행 준비 (장비, 기기) -> 모바일 개발용 덤

```java
//모르는 개념
Surface
egl, skia, opengl
canvas
HWC, SurfaceFlinger, glSurfaceView, glRenderer
```

### 참고

- [<https://arabiannight.tistory.com/entry/343]>
- [<https://developer.android.com/reference/android/view/WindowManager.LayoutParams]>
- [<https://developer.android.com/reference/android/view/WindowManager]>
- [<https://en.wikipedia.org/wiki/Skia_Graphics_Engine]>
- [<http://oss.kr/oss/images/news/]>

#### 🧶 모든 문서는 수정될 수 있습니다.
