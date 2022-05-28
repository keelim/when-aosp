---
layout: post
title: "addWindow convert to kotlin "
date: 2020-05-19 00:00:01
author: Keelim
categories: AOSP
comments: true
toc: true
toc_sticky: true
---

이번 포스팅은 저번에 분석한 addWindow 를 추가로 분석하기로 하자.
저번 미팅에서 개선할 사항 후부로 옮길 수 있는 것은?

1. kotlin 으로 변환을 하여 synchronized 처리를 하는데에 `coroutine` 을 사용을 하는 것
   - 확장으로 따로 VM 을 올려서 성능 향상이 가능한지를 체크
2. 중복된 if else 문에 `Hashing`을 도입을 해보는 것 // 이전 PackageManger 에서 비슷한 내용을 본 것 같다.
3. 배열 1 // -> 이 부분은 앞으로를 위해 있을 수 있다//

이렇게 정리를 할 수 있다. 앞으로의 아이디어는 계속 적어놓자.

오늘 진행할 내용은 addWindow 에서 로그처리를 제외한 그 이후 부분이다.

## addWindow 어디서 사용하는지

## Window 에 대한 개념

![그림](https://miro.medium.com/max/2160/1*z15f9yf-mMXlC_dyFZc9Ig.png)

`Surface`는 화면에 합성되는 픽셀을 보유한 객체입니다.
화면에 표시되는 모든 `Window`는 자신만의 `Surface`가 포함되어 있으며, `Surface Flinger`가 여러 소스로부터 그래픽 데이터 버퍼를 받고,
그것들을 합성해서 Display로 보냅니다.

개별 Surface는 \*\*이중 버퍼 렌더링을 위한 1개 이상(보통 2개)의 버퍼를 가집니다.

### SurfaceView

View는 Main Thread에서 캔버스를 그리기 때문에, 그리기를 하는 동안에는 사용자의 입력을 받을 수 없고
그로 인해 반응성이 좋지 못합니다. 그렇다고 그리는 작업을 별도의 작업 스레드에서 처리하고 싶어도 안드로이드 정책 상 Main Thread가 아닌
별도의 Thread에서는 UI 관련 작업을 할 수도 없습니다. 이럴때 사용할 수 있는게 `SurfaceView`

SurfaceView는 Canvas 아닌 Surface(=가상 메모리 화면)에 그리고 그려진 Surface를 화면에 뿌리기 때문에 게임이나,
카메라 같은 높은 반응성이 필요한 UI 작업이 필요한 경우 사용.

뷰가 그려지는 과정, View Hierachy 살펴보기, 뷰 관련 용어 잡기, 카메라 관련 코드 다시 보자

## 나머지 코드 설명

```java
            res = WindowManagerGlobal.ADD_OKAY;
            if (displayContent.mCurrentFocus == null) {
                displayContent.mWinAddedSinceNullFocus.add(win);
            }

            if (excludeWindowTypeFromTapOutTask(type)) {
                displayContent.mTapExcludedWindows.add(win);
            }

            origId = Binder.clearCallingIdentity();

            win.attach();
            mWindowMap.put(client.asBinder(), win);

            win.initAppOpsState();

            final boolean suspended = mPmInternal.isPackageSuspended(win.getOwningPackage(),
                    UserHandle.getUserId(win.getOwningUid()));
            win.setHiddenWhileSuspended(suspended);

            final boolean hideSystemAlertWindows = !mHidingNonSystemOverlayWindows.isEmpty();
            win.setForceHideNonSystemOverlayWindowIfNeeded(hideSystemAlertWindows);

            final AppWindowToken aToken = token.asAppWindowToken();
            if (type == TYPE_APPLICATION_STARTING && aToken != null) {
                aToken.startingWindow = win;
                if (DEBUG_STARTING_WINDOW) Slog.v (TAG_WM, "addWindow: " + aToken
                        + " startingWindow=" + win);
            }

            boolean imMayMove = true;

            win.mToken.addWindow(win);
            if (type == TYPE_INPUT_METHOD) {
                displayContent.setInputMethodWindowLocked(win);
                imMayMove = false;
            } else if (type == TYPE_INPUT_METHOD_DIALOG) {
                displayContent.computeImeTarget(true /* updateImeTarget */);
                imMayMove = false;
            } else {
                if (type == TYPE_WALLPAPER) {
                    displayContent.mWallpaperController.clearLastWallpaperTimeoutTime();
                    displayContent.pendingLayoutChanges |= FINISH_LAYOUT_REDO_WALLPAPER;
                } else if ((attrs.flags&FLAG_SHOW_WALLPAPER) != 0) {
                    displayContent.pendingLayoutChanges |= FINISH_LAYOUT_REDO_WALLPAPER;
                } else if (displayContent.mWallpaperController.isBelowWallpaperTarget(win)) {
                    // If there is currently a wallpaper being shown, and
                    // the base layer of the new window is below the current
                    // layer of the target window, then adjust the wallpaper.
                    // This is to avoid a new window being placed between the
                    // wallpaper and its target.
                    displayContent.pendingLayoutChanges |= FINISH_LAYOUT_REDO_WALLPAPER;
                }
            }

            // If the window is being added to a stack that's currently adjusted for IME,
            // make sure to apply the same adjust to this new window.
            win.applyAdjustForImeIfNeeded();

            if (type == TYPE_DOCK_DIVIDER) {
                mRoot.getDisplayContent(displayId).getDockedDividerController().setWindow(win);
            }

            final WindowStateAnimator winAnimator = win.mWinAnimator;
            winAnimator.mEnterAnimationPending = true;
            winAnimator.mEnteringAnimation = true;
            // Check if we need to prepare a transition for replacing window first.
            if (atoken != null && atoken.isVisible()
                    && !prepareWindowReplacementTransition(atoken)) {
                // If not, check if need to set up a dummy transition during display freeze
                // so that the unfreeze wait for the apps to draw. This might be needed if
                // the app is relaunching.
                prepareNoneTransitionForRelaunching(atoken);
            }

            final DisplayFrames displayFrames = displayContent.mDisplayFrames;
            // TODO: Not sure if onDisplayInfoUpdated() call is needed.
            final DisplayInfo displayInfo = displayContent.getDisplayInfo();
            displayFrames.onDisplayInfoUpdated(displayInfo,
                    displayContent.calculateDisplayCutoutForRotation(displayInfo.rotation));
            final Rect taskBounds;
            final boolean floatingStack;
            if (atoken != null && atoken.getTask() != null) {
                taskBounds = mTmpRect;
                atoken.getTask().getBounds(mTmpRect);
                floatingStack = atoken.getTask().isFloating();
            } else {
                taskBounds = null;
                floatingStack = false;
            }
            if (displayPolicy.getLayoutHintLw(win.mAttrs, taskBounds, displayFrames, floatingStack,
                    outFrame, outContentInsets, outStableInsets, outOutsets, outDisplayCutout)) {
                res |= WindowManagerGlobal.ADD_FLAG_ALWAYS_CONSUME_SYSTEM_BARS;
            }
            outInsetsState.set(displayContent.getInsetsStateController().getInsetsForDispatch(win));

            if (mInTouchMode) {
                res |= WindowManagerGlobal.ADD_FLAG_IN_TOUCH_MODE;
            }
            if (win.mAppToken == null || !win.mAppToken.isClientHidden()) {
                res |= WindowManagerGlobal.ADD_FLAG_APP_VISIBLE;
            }

            displayContent.getInputMonitor().setUpdateInputWindowsNeededLw();

            boolean focusChanged = false;
            if (win.canReceiveKeys()) {
                focusChanged = updateFocusedWindowLocked(UPDATE_FOCUS_WILL_ASSIGN_LAYERS,
                        false /*updateInputWindows*/);
                if (focusChanged) {
                    imMayMove = false;
                }
            }

            if (imMayMove) {
                displayContent.computeImeTarget(true /* updateImeTarget */);
            }

            // Don't do layout here, the window must call
            // relayout to be displayed, so we'll do it there.
            win.getParent().assignChildLayers();

            if (focusChanged) {
                displayContent.getInputMonitor().setInputFocusLw(displayContent.mCurrentFocus,
                        false /*updateInputWindows*/);
            }
            displayContent.getInputMonitor().updateInputWindowsLw(false /*force*/);

            if (localLOGV || DEBUG_ADD_REMOVE) Slog.v(TAG_WM, "addWindow: New client "
                    + client.asBinder() + ": window=" + win + " Callers=" + Debug.getCallers(5));

            if (win.isVisibleOrAdding() && displayContent.updateOrientationFromAppTokens()) {
                reportNewConfig = true;
            }
        }

        if (reportNewConfig) {
            sendNewConfiguration(displayId);
        }

        Binder.restoreCallingIdentity(origId);

        return res;
    }
```

## 코루틴

코틀린에서만 쓰이는 개념이 아니며 이는

1. 협력형 멀티 태스킹
2. 동시성 프로그래밍 지원
3. 비동기 처리를 쉽게 도와줌

이를 해결 할 수 있게 한다.

![구성](https://github.com/keelim/AOSP/blob/master/docs/assets/reset1.png?raw=true)

### `코틀린으로 임의로 변경해보았다.`

```kotlin
import android.graphics.Rect
import android.os.Binder
import android.os.Debug
import android.os.UserHandle
import android.view.DisplayCutout
import android.view.WindowManager

class WindowManagerService {

    fun addWindow(session: Session, client: IWindow, seq: Int, attrs: WindowManager.LayoutParams,
                  viewVisibility: Int, displayId: Int, outFrame: Rect?, outContentInsets: Rect?, outStableInsets: Rect?, outOutsets: Rect?, outDisplayCutout: DisplayCutout.ParcelableWrapper?,
                  outInputChannel: InputChannel?, outInsetsState: InsetsState): Int {
        val appOp = IntArray(1)
        var res: Int = mPolicy.checkAddPermission(attrs, appOp)
        if (res != WindowManagerGlobal.ADD_OKAY) { return res }

        var reportNewConfig = false
        var parentWindow: WindowState? = null
        var origId: Long
        val callingUid = Binder.getCallingUid()
        val type = attrs.type

        synchronized(mGlobalLock) {
            check(mDisplayReady) { "Display has not been initialialized" }
            val displayContent: DisplayContent = getDisplayContentOrCreate(displayId, attrs.token)
            if (displayContent == null) {
                Slog.w(
                    TAG_WM, "Attempted to add window to a display that does not exist: "
                            + displayId + ".  Aborting."
                )
                return WindowManagerGlobal.ADD_INVALID_DISPLAY
            }
            if (!displayContent.hasAccess(session.mUid)) {
                Slog.w(
                    TAG_WM, "Attempted to add window to a display for which the application "
                            + "does not have access: " + displayId + ".  Aborting."
                )
                return WindowManagerGlobal.ADD_INVALID_DISPLAY
            }
            if (mWindowMap.containsKey(client.asBinder())) {
                Slog.w(TAG_WM, "Window $client is already added")
                return WindowManagerGlobal.ADD_DUPLICATE_ADD
            }
            if (type >= WindowManager.LayoutParams.FIRST_SUB_WINDOW && type <= WindowManager.LayoutParams.LAST_SUB_WINDOW) {
                parentWindow = windowForClientLocked(null, attrs.token, false)
                if (parentWindow == null) {
                    Slog.w(
                        TAG_WM, "Attempted to add window with token that is not a window: "
                                + attrs.token + ".  Aborting."
                    )
                    return WindowManagerGlobal.ADD_BAD_SUBWINDOW_TOKEN
                }
                if (parentWindow.mAttrs.type >= WindowManager.LayoutParams.FIRST_SUB_WINDOW
                    && parentWindow.mAttrs.type <= WindowManager.LayoutParams.LAST_SUB_WINDOW
                ) {
                    Slog.w(
                        TAG_WM, "Attempted to add window with token that is a sub-window: "
                                + attrs.token + ".  Aborting."
                    )
                    return WindowManagerGlobal.ADD_BAD_SUBWINDOW_TOKEN
                }
            }
            if (type == WindowManager.LayoutParams.TYPE_PRIVATE_PRESENTATION && !displayContent.isPrivate()) {
                Slog.w(
                    TAG_WM,
                    "Attempted to add private presentation window to a non-private display.  Aborting."
                )
                return WindowManagerGlobal.ADD_PERMISSION_DENIED
            }
            var atoken: AppWindowToken? = null
            val hasParent = parentWindow != null
            // Use existing parent window token for child windows since they go in the same token
            // as there parent window so we can apply the same policy on them.
            var token: WindowToken? = displayContent.getWindowToken(
                if (hasParent) parentWindow.mAttrs.token else attrs.token
            )
            // If this is a child window, we want to apply the same type checking rules as the
            // parent window type.
            val rootType = if (hasParent) parentWindow.mAttrs.type else type
            var addToastWindowRequiresToken = false
            if (token == null) {
                if (rootType >= WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW && rootType <= WindowManager.LayoutParams.LAST_APPLICATION_WINDOW) {
                    Slog.w(
                        TAG_WM, "Attempted to add application window with unknown token "
                                + attrs.token + ".  Aborting."
                    )
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN
                }
                if (rootType == WindowManager.LayoutParams.TYPE_INPUT_METHOD) {
                    Slog.w(
                        TAG_WM, "Attempted to add input method window with unknown token "
                                + attrs.token + ".  Aborting."
                    )
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN
                }
                if (rootType == TYPE_VOICE_INTERACTION) {
                    Slog.w(
                        TAG_WM, "Attempted to add voice interaction window with unknown token "
                                + attrs.token + ".  Aborting."
                    )
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN
                }
                if (rootType == WindowManager.LayoutParams.TYPE_WALLPAPER) {
                    Slog.w(
                        TAG_WM, "Attempted to add wallpaper window with unknown token "
                                + attrs.token + ".  Aborting."
                    )
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN
                }
                if (rootType == TYPE_DREAM) {
                    Slog.w(
                        TAG_WM, "Attempted to add Dream window with unknown token "
                                + attrs.token + ".  Aborting."
                    )
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN
                }
                if (rootType == TYPE_QS_DIALOG) {
                    Slog.w(
                        TAG_WM, "Attempted to add QS dialog window with unknown token "
                                + attrs.token + ".  Aborting."
                    )
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN
                }
                if (rootType == WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY) {
                    Slog.w(
                        TAG_WM, "Attempted to add Accessibility overlay window with unknown token "
                                + attrs.token + ".  Aborting."
                    )
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN
                }
                if (type == WindowManager.LayoutParams.TYPE_TOAST) {
                    // Apps targeting SDK above N MR1 cannot arbitrary add toast windows.
                    if (doesAddToastWindowRequireToken(
                            attrs.packageName, callingUid,
                            parentWindow
                        )
                    ) {
                        Slog.w(
                            TAG_WM, "Attempted to add a toast window with unknown token "
                                    + attrs.token + ".  Aborting."
                        )
                        return WindowManagerGlobal.ADD_BAD_APP_TOKEN
                    }
                }
                val binder = if (attrs.token != null) attrs.token else client.asBinder()
                val isRoundedCornerOverlay =
                    attrs.privateFlags and PRIVATE_FLAG_IS_ROUNDED_CORNERS_OVERLAY !== 0
                token = WindowToken(
                    this, binder, type, false, displayContent,
                    session.mCanAddInternalSystemWindow, isRoundedCornerOverlay
                )
            } else if (rootType >= WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW && rootType <= WindowManager.LayoutParams.LAST_APPLICATION_WINDOW) {
                atoken = token.asAppWindowToken()
                if (atoken == null) {
                    Slog.w(
                        TAG_WM, "Attempted to add window with non-application token "
                                + token + ".  Aborting."
                    )
                    return WindowManagerGlobal.ADD_NOT_APP_TOKEN
                } else if (atoken.removed) {
                    Slog.w(
                        TAG_WM, "Attempted to add window with exiting application token "
                                + token + ".  Aborting."
                    )
                    return WindowManagerGlobal.ADD_APP_EXITING
                } else if (type == WindowManager.LayoutParams.TYPE_APPLICATION_STARTING && atoken.startingWindow != null) {
                    Slog.w(
                        TAG_WM, "Attempted to add starting window to token with already existing"
                                + " starting window"
                    )
                    return WindowManagerGlobal.ADD_DUPLICATE_ADD
                }
            } else if (rootType == WindowManager.LayoutParams.TYPE_INPUT_METHOD) {
                if (token.windowType !== WindowManager.LayoutParams.TYPE_INPUT_METHOD) {
                    Slog.w(
                        TAG_WM, "Attempted to add input method window with bad token "
                                + attrs.token + ".  Aborting."
                    )
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN
                }
            } else if (rootType == TYPE_VOICE_INTERACTION) {
                if (token.windowType !== TYPE_VOICE_INTERACTION) {
                    Slog.w(
                        TAG_WM, "Attempted to add voice interaction window with bad token "
                                + attrs.token + ".  Aborting."
                    )
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN
                }
            } else if (rootType == WindowManager.LayoutParams.TYPE_WALLPAPER) {
                if (token.windowType !== WindowManager.LayoutParams.TYPE_WALLPAPER) {
                    Slog.w(
                        TAG_WM, "Attempted to add wallpaper window with bad token "
                                + attrs.token + ".  Aborting."
                    )
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN
                }
            } else if (rootType == TYPE_DREAM) {
                if (token.windowType !== TYPE_DREAM) {
                    Slog.w(
                        TAG_WM, "Attempted to add Dream window with bad token "
                                + attrs.token + ".  Aborting."
                    )
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN
                }
            } else if (rootType == WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY) {
                if (token.windowType !== WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY) {
                    Slog.w(
                        TAG_WM, "Attempted to add Accessibility overlay window with bad token "
                                + attrs.token + ".  Aborting."
                    )
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN
                }
            } else if (type == WindowManager.LayoutParams.TYPE_TOAST) {
                // Apps targeting SDK above N MR1 cannot arbitrary add toast windows.
                addToastWindowRequiresToken = doesAddToastWindowRequireToken(
                    attrs.packageName,
                    callingUid, parentWindow
                )
                if (addToastWindowRequiresToken && token.windowType !== WindowManager.LayoutParams.TYPE_TOAST) {
                    Slog.w(
                        TAG_WM, "Attempted to add a toast window with bad token "
                                + attrs.token + ".  Aborting."
                    )
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN
                }
            } else if (type == TYPE_QS_DIALOG) {
                if (token.windowType !== TYPE_QS_DIALOG) {
                    Slog.w(
                        TAG_WM, "Attempted to add QS dialog window with bad token "
                                + attrs.token + ".  Aborting."
                    )
                    return WindowManagerGlobal.ADD_BAD_APP_TOKEN
                }
            } else if (token.asAppWindowToken() != null) {
                Slog.w(TAG_WM, "Non-null appWindowToken for system window of rootType=$rootType")
                // It is not valid to use an app token with other system types; we will
                // instead make a new token for it (as if null had been passed in for the token).
                attrs.token = null
                token = WindowToken(
                    this, client.asBinder(), type, false, displayContent,
                    session.mCanAddInternalSystemWindow
                )
            }
            val win = WindowState(
                this, session, client, token, parentWindow,
                appOp[0], seq, attrs, viewVisibility, session.mUid,
                session.mCanAddInternalSystemWindow
            )
            if (win.mDeathRecipient == null) {
                // Client has apparently died, so there is no reason to
                // continue.
                Slog.w(
                    TAG_WM, "Adding window client " + client.asBinder()
                        .toString() + " that is dead, aborting."
                )
                return WindowManagerGlobal.ADD_APP_EXITING
            }
            if (win.getDisplayContent() == null) {
                Slog.w(TAG_WM, "Adding window to Display that has been removed.")
                return WindowManagerGlobal.ADD_INVALID_DISPLAY
            }
            val displayPolicy: DisplayPolicy = displayContent.getDisplayPolicy()
            displayPolicy.adjustWindowParamsLw(
                win, win.mAttrs, Binder.getCallingPid(),
                Binder.getCallingUid()
            )
            win.setShowToOwnerOnlyLocked(mPolicy.checkShowToOwnerOnly(attrs))
            res = displayPolicy.prepareAddWindowLw(win, attrs)
            if (res != WindowManagerGlobal.ADD_OKAY) {
                return res
            }
            val openInputChannels = (outInputChannel != null
                    && attrs.inputFeatures and INPUT_FEATURE_NO_INPUT_CHANNEL === 0)
            if (openInputChannels) {
                win.openInputChannel(outInputChannel)
            }

            // If adding a toast requires a token for this app we always schedule hiding
            // toast windows to make sure they don't stick around longer then necessary.
            // We hide instead of remove such windows as apps aren't prepared to handle
            // windows being removed under them.
            //
            // If the app is older it can add toasts without a token and hence overlay
            // other apps. To be maximally compatible with these apps we will hide the
            // window after the toast timeout only if the focused window is from another
            // UID, otherwise we allow unlimited duration. When a UID looses focus we
            // schedule hiding all of its toast windows.
            if (type == WindowManager.LayoutParams.TYPE_TOAST) {
                if (!displayContent.canAddToastWindowForUid(callingUid)) {
                    Slog.w(TAG_WM, "Adding more than one toast window for UID at a time.")
                    return WindowManagerGlobal.ADD_DUPLICATE_ADD
                }
                // Make sure this happens before we moved focus as one can make the
                // toast focusable to force it not being hidden after the timeout.
                // Focusable toasts are always timed out to prevent a focused app to
                // show a focusable toasts while it has focus which will be kept on
                // the screen after the activity goes away.
                if (addToastWindowRequiresToken
                    || attrs.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE == 0 || displayContent.mCurrentFocus == null || displayContent.mCurrentFocus.mOwnerUid !== callingUid
                ) {
                    mH.sendMessageDelayed(
                        mH.obtainMessage(H.WINDOW_HIDE_TIMEOUT, win),
                        win.mAttrs.hideTimeoutMilliseconds
                    )
                }
            }

            // From now on, no exceptions or errors allowed!
            res = WindowManagerGlobal.ADD_OKAY
            if (displayContent.mCurrentFocus == null) {
                displayContent.mWinAddedSinceNullFocus.add(win)
            }
            if (com.android.server.wm.WindowManagerService.excludeWindowTypeFromTapOutTask(type)) {
                displayContent.mTapExcludedWindows.add(win)
            }
            origId = Binder.clearCallingIdentity()
            win.attach()
            mWindowMap.put(client.asBinder(), win)
            win.initAppOpsState()
            val suspended: Boolean = mPmInternal.isPackageSuspended(
                win.getOwningPackage(),
                UserHandle.getUserId(win.getOwningUid())
            )
            win.setHiddenWhileSuspended(suspended)
            val hideSystemAlertWindows: Boolean = !mHidingNonSystemOverlayWindows.isEmpty()
            win.setForceHideNonSystemOverlayWindowIfNeeded(hideSystemAlertWindows)
            val aToken: AppWindowToken = token.asAppWindowToken()
            if (type == WindowManager.LayoutParams.TYPE_APPLICATION_STARTING && aToken != null) {
                aToken.startingWindow = win
                if (DEBUG_STARTING_WINDOW) Slog.v(
                    TAG_WM, "addWindow: " + aToken
                            + " startingWindow=" + win
                )
            }
            var imMayMove = true
            win.mToken.addWindow(win)
            if (type == WindowManager.LayoutParams.TYPE_INPUT_METHOD) {
                displayContent.setInputMethodWindowLocked(win)
                imMayMove = false
            } else if (type == WindowManager.LayoutParams.TYPE_INPUT_METHOD_DIALOG) {
                displayContent.computeImeTarget(true /* updateImeTarget */)
                imMayMove = false
            } else {
                if (type == WindowManager.LayoutParams.TYPE_WALLPAPER) {
                    displayContent.mWallpaperController.clearLastWallpaperTimeoutTime()
                    displayContent.pendingLayoutChanges =
                        displayContent.pendingLayoutChanges or FINISH_LAYOUT_REDO_WALLPAPER
                } else if (attrs.flags and WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER != 0) {
                    displayContent.pendingLayoutChanges =
                        displayContent.pendingLayoutChanges or FINISH_LAYOUT_REDO_WALLPAPER
                } else if (displayContent.mWallpaperController.isBelowWallpaperTarget(win)) {
                    // If there is currently a wallpaper being shown, and
                    // the base layer of the new window is below the current
                    // layer of the target window, then adjust the wallpaper.
                    // This is to avoid a new window being placed between the
                    // wallpaper and its target.
                    displayContent.pendingLayoutChanges =
                        displayContent.pendingLayoutChanges or FINISH_LAYOUT_REDO_WALLPAPER
                }
            }

            // If the window is being added to a stack that's currently adjusted for IME,
            // make sure to apply the same adjust to this new window.
            win.applyAdjustForImeIfNeeded()
            if (type == TYPE_DOCK_DIVIDER) {
                mRoot.getDisplayContent(displayId).getDockedDividerController().setWindow(win)
            }
            val winAnimator: WindowStateAnimator = win.mWinAnimator
            winAnimator.mEnterAnimationPending = true
            winAnimator.mEnteringAnimation = true
            // Check if we need to prepare a transition for replacing window first.
            if (atoken != null && atoken.isVisible()
                && !prepareWindowReplacementTransition(atoken)
            ) {
                // If not, check if need to set up a dummy transition during display freeze
                // so that the unfreeze wait for the apps to draw. This might be needed if
                // the app is relaunching.
                prepareNoneTransitionForRelaunching(atoken)
            }
            val displayFrames: DisplayFrames = displayContent.mDisplayFrames
            // TODO: Not sure if onDisplayInfoUpdated() call is needed.
            val displayInfo: DisplayInfo = displayContent.getDisplayInfo()
            displayFrames.onDisplayInfoUpdated(
                displayInfo,
                displayContent.calculateDisplayCutoutForRotation(displayInfo.rotation)
            )
            val taskBounds: Rect?
            val floatingStack: Boolean
            if (atoken != null && atoken.getTask() != null) {
                taskBounds = mTmpRect
                atoken.getTask().getBounds(mTmpRect)
                floatingStack = atoken.getTask().isFloating()
            } else {
                taskBounds = null
                floatingStack = false
            }
            if (displayPolicy.getLayoutHintLw(
                    win.mAttrs, taskBounds, displayFrames, floatingStack,
                    outFrame, outContentInsets, outStableInsets, outOutsets, outDisplayCutout
                )
            ) {
                res = res or WindowManagerGlobal.ADD_FLAG_ALWAYS_CONSUME_SYSTEM_BARS
            }
            outInsetsState.set(displayContent.getInsetsStateController().getInsetsForDispatch(win))
            if (mInTouchMode) {
                res = res or WindowManagerGlobal.ADD_FLAG_IN_TOUCH_MODE
            }
            if (win.mAppToken == null || !win.mAppToken.isClientHidden()) {
                res = res or WindowManagerGlobal.ADD_FLAG_APP_VISIBLE
            }
            displayContent.getInputMonitor().setUpdateInputWindowsNeededLw()
            var focusChanged = false
            if (win.canReceiveKeys()) {
                focusChanged = updateFocusedWindowLocked(
                    com.android.server.wm.WindowManagerService.UPDATE_FOCUS_WILL_ASSIGN_LAYERS,
                    false /*updateInputWindows*/
                )
                if (focusChanged) {
                    imMayMove = false
                }
            }
            if (imMayMove) {
                displayContent.computeImeTarget(true /* updateImeTarget */)
            }

            // Don't do layout here, the window must call
            // relayout to be displayed, so we'll do it there.
            win.getParent().assignChildLayers()
            if (focusChanged) {
                displayContent.getInputMonitor().setInputFocusLw(
                    displayContent.mCurrentFocus,
                    false /*updateInputWindows*/
                )
            }
            displayContent.getInputMonitor().updateInputWindowsLw(false /*force*/)
            if (com.android.server.wm.WindowManagerService.localLOGV || DEBUG_ADD_REMOVE) Slog.v(
                TAG_WM, ("addWindow: New client "
                        + client.asBinder()) + ": window=" + win.toString() + " Callers=" + Debug.getCallers(
                    5
                )
            )
            if (win.isVisibleOrAdding() && displayContent.updateOrientationFromAppTokens()) {
                reportNewConfig = true
            }
        }
        if (reportNewConfig) {
            sendNewConfiguration(displayId)
        }
        Binder.restoreCallingIdentity(origId)
        return res
    }

}


```

### 참고

- [<https://developer.android.com/>]
- [<http://oss.kr/]>
- 인사이드 안드로이드
- [<https://cs.android.com/>]
- 안드로이드 뷰가 그려지는 과정 [https://jungwoon.github.io/android/2019/10/02/How-to-draw-View/]
- 코루틴 [https://wooooooak.github.io/kotlin/2019/08/25/%EC%BD%94%ED%8B%80%EB%A6%B0-%EC%BD%94%EB%A3%A8%ED%8B%B4-%EA%B0%9C%EB%85%90-%EC%9D%B5%ED%9E%88%EA%B8%B0/]


#### 🧶 모든 문서는 수정될 수 있습니다.