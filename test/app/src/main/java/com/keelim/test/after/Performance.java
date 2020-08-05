package com.keelim.test.after;

import android.os.IBinder;
import android.view.WindowManager;
import com.keelim.test.*;

import java.util.EnumMap;
import java.util.Map;

import static android.view.WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW;
import static android.view.WindowManager.LayoutParams.LAST_APPLICATION_WINDOW;
import static android.view.WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_STARTING;
import static android.view.WindowManager.LayoutParams.TYPE_INPUT_METHOD;
import static android.view.WindowManager.LayoutParams.TYPE_TOAST;
import static android.view.WindowManager.LayoutParams.TYPE_WALLPAPER;
import static com.keelim.test.WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW;
import static com.keelim.test.WindowManager.LayoutParams.LAST_APPLICATION_WINDOW;
import static com.keelim.test.WindowManager.LayoutParams.PRIVATE_FLAG_IS_ROUNDED_CORNERS_OVERLAY;
import static com.keelim.test.WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
import static com.keelim.test.WindowManager.LayoutParams.TYPE_APPLICATION_STARTING;
import static com.keelim.test.WindowManager.LayoutParams.TYPE_DREAM;
import static com.keelim.test.WindowManager.LayoutParams.TYPE_INPUT_METHOD;
import static com.keelim.test.WindowManager.LayoutParams.TYPE_QS_DIALOG;
import static com.keelim.test.WindowManager.LayoutParams.TYPE_TOAST;
import static com.keelim.test.WindowManager.LayoutParams.TYPE_VOICE_INTERACTION;
import static com.keelim.test.WindowManager.LayoutParams.TYPE_WALLPAPER;


public class Performance {


    //todo this comment change string block for test
    // - /*## <".  Aborting."> ->s0*/
    // - /*## <"Attempted to add application window with unknown token "> ->s1*/
    // - /*## <"Attempted to add input method window with unknown token "> ->s2*/
    // - /*##<"Attempted to add voice interaction window with unknown token "> ->s3*/
    // - /*##<"Attempted to add wallpaper window with unknown token "> ->s4*/
    // - /*##<"Attempted to add Dream window with unknown token "> ->s5*/
    // - /*##<"Attempted to add QS dialog window with unknown token "> ->s6*/
    // - /*##<"Attempted to add Accessibility overlay window with unknown token "> ->s7*/
    // - /*##<"Attempted to add input method window with bad token "> -> s8*/
    // - /*## <"Attempted to add voice interaction window with bad token "> -> s9*/
    // - /*## <"Attempted to add wallpaper window with bad token "> -> s10*/
    // - /*## <"Attempted to add Dream window with bad token "> -> s11*/
    // - /*## <"Attempted to add Accessibility overlay window with bad token "> -> s12*/
    // - /*## <"Attempted to add a toast window with bad token "> -> s13*/
    // - /*## <"Attempted to add QS dialog window with bad token "> -> s14*/
    // -/*## <"Attempted to add a toast window with unknown token "> -> s15*/s

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

    // HASH MAP , ENUM MAP을 만드는 건 좋은데 어느 시점에서 생성을 하고 데이터를 넣어야 하는가?


}

