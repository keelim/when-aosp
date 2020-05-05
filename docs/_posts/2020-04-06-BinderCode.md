---
layout: post
title: "바인더(2)"
date: 2020-04-07 00:00:01
author: Keelim
categories: AOSP
comments: true
toc: true
toc_sticky: true
---


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
package android.os;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.compat.annotation.UnsupportedAppUsage;
import java.io.FileDescriptor;
/**
 * Base interface for a remotable object, the core part of a lightweight
 * remote procedure call mechanism designed for high performance when
 * performing in-process and cross-process calls.  This
 * interface describes the abstract protocol for interacting with a
 * remotable object.  Do not implement this interface directly, instead
 * extend from {@link Binder}.
 *
 * <p>The key IBinder API is {@link #transact transact()} matched by
 * {@link Binder#onTransact Binder.onTransact()}.  These
 * methods allow you to send a call to an IBinder object and receive a
 * call coming in to a Binder object, respectively.  This transaction API
 * is synchronous, such that a call to {@link #transact transact()} does not
 * return until the target has returned from
 * {@link Binder#onTransact Binder.onTransact()}; this is the
 * expected behavior when calling an object that exists in the local
 * process, and the underlying inter-process communication (IPC) mechanism
 * ensures that these same semantics apply when going across processes.
 *
 * <p>The data sent through transact() is a {@link Parcel}, a generic buffer
 * of data that also maintains some meta-data about its contents.  The meta
 * data is used to manage IBinder object references in the buffer, so that those
 * references can be maintained as the buffer moves across processes.  This
 * mechanism ensures that when an IBinder is written into a Parcel and sent to
 * another process, if that other process sends a reference to that same IBinder
 * back to the original process, then the original process will receive the
 * same IBinder object back.  These semantics allow IBinder/Binder objects to
 * be used as a unique identity (to serve as a token or for other purposes)
 * that can be managed across processes.
 *
 * <p>The system maintains a pool of transaction threads in each process that
 * it runs in.  These threads are used to dispatch all
 * IPCs coming in from other processes.  For example, when an IPC is made from
 * process A to process B, the calling thread in A blocks in transact() as
 * it sends the transaction to process B.  The next available pool thread in
 * B receives the incoming transaction, calls Binder.onTransact() on the target
 * object, and replies with the result Parcel.  Upon receiving its result, the
 * thread in process A returns to allow its execution to continue.  In effect,
 * other processes appear to use as additional threads that you did not create
 * executing in your own process.
 *
 * <p>The Binder system also supports recursion across processes.  For example
 * if process A performs a transaction to process B, and process B while
 * handling that transaction calls transact() on an IBinder that is implemented
 * in A, then the thread in A that is currently waiting for the original
 * transaction to finish will take care of calling Binder.onTransact() on the
 * object being called by B.  This ensures that the recursion semantics when
 * calling remote binder object are the same as when calling local objects.
 *
 * <p>When working with remote objects, you often want to find out when they
 * are no longer valid.  There are three ways this can be determined:
 * <ul>
 * <li> The {@link #transact transact()} method will throw a
 * {@link RemoteException} exception if you try to call it on an IBinder
 * whose process no longer exists.
 * <li> The {@link #pingBinder()} method can be called, and will return false
 * if the remote process no longer exists.
 * <li> The {@link #linkToDeath linkToDeath()} method can be used to register
 * a {@link DeathRecipient} with the IBinder, which will be called when its
 * containing process goes away.
 * </ul>
 *
 * @see Binder
 */
public interface IBinder {
    /**
     * The first transaction code available for user commands.
     */
    int FIRST_CALL_TRANSACTION  = 0x00000001;
    /**
     * The last transaction code available for user commands.
     */
    int LAST_CALL_TRANSACTION   = 0x00ffffff;

    /**
     * IBinder protocol transaction code: pingBinder().
     */
    int PING_TRANSACTION        = ('_'<<24)|('P'<<16)|('N'<<8)|'G';

    /**
     * IBinder protocol transaction code: dump internal state.
     */
    int DUMP_TRANSACTION        = ('_'<<24)|('D'<<16)|('M'<<8)|'P';

    /**
     * IBinder protocol transaction code: execute a shell command.
     * @hide
     */
    int SHELL_COMMAND_TRANSACTION = ('_'<<24)|('C'<<16)|('M'<<8)|'D';
    /**
     * IBinder protocol transaction code: interrogate the recipient side
     * of the transaction for its canonical interface descriptor.
     */
    int INTERFACE_TRANSACTION   = ('_'<<24)|('N'<<16)|('T'<<8)|'F';
    /**
     * IBinder protocol transaction code: send a tweet to the target
     * object.  The data in the parcel is intended to be delivered to
     * a shared messaging service associated with the object; it can be
     * anything, as long as it is not more than 130 UTF-8 characters to
     * conservatively fit within common messaging services.  As part of
     * {@link Build.VERSION_CODES#HONEYCOMB_MR2}, all Binder objects are
     * expected to support this protocol for fully integrated tweeting
     * across the platform.  To support older code, the default implementation
     * logs the tweet to the main log as a simple emulation of broadcasting
     * it publicly over the Internet.
     *
     * <p>Also, upon completing the dispatch, the object must make a cup
     * of tea, return it to the caller, and exclaim "jolly good message
     * old boy!".
     */
    int TWEET_TRANSACTION   = ('_'<<24)|('T'<<16)|('W'<<8)|'T';
    /**
     * IBinder protocol transaction code: tell an app asynchronously that the
     * caller likes it.  The app is responsible for incrementing and maintaining
     * its own like counter, and may display this value to the user to indicate the
     * quality of the app.  This is an optional command that applications do not
     * need to handle, so the default implementation is to do nothing.
     *
     * <p>There is no response returned and nothing about the
     * system will be functionally affected by it, but it will improve the
     * app's self-esteem.
     */
    int LIKE_TRANSACTION   = ('_'<<24)|('L'<<16)|('I'<<8)|'K';
    /** @hide */
    @UnsupportedAppUsage
    int SYSPROPS_TRANSACTION = ('_'<<24)|('S'<<16)|('P'<<8)|'R';
    /**
     * Flag to {@link #transact}: this is a one-way call, meaning that the
     * caller returns immediately, without waiting for a result from the
     * callee. Applies only if the caller and callee are in different
     * processes.
     *
     * <p>The system provides special ordering semantics for multiple oneway calls
     * being made to the same IBinder object: these calls will be dispatched in the
     * other process one at a time, with the same order as the original calls.  These
     * are still dispatched by the IPC thread pool, so may execute on different threads,
     * but the next one will not be dispatched until the previous one completes.  This
     * ordering is not guaranteed for calls on different IBinder objects or when mixing
     * oneway and non-oneway calls on the same IBinder object.</p>
     */
    int FLAG_ONEWAY             = 0x00000001;
    /**
     * Limit that should be placed on IPC sizes to keep them safely under the
     * transaction buffer limit.
     * @hide
     */
    public static final int MAX_IPC_SIZE = 64 * 1024;
    /**
     * Get the canonical name of the interface supported by this binder.
     */
    public @Nullable String getInterfaceDescriptor() throws RemoteException;
    /**
     * Check to see if the object still exists.
     *
     * @return Returns false if the
     * hosting process is gone, otherwise the result (always by default
     * true) returned by the pingBinder() implementation on the other
     * side.
     */
    public boolean pingBinder();
    /**
     * Check to see if the process that the binder is in is still alive.
     *
     * @return false if the process is not alive.  Note that if it returns
     * true, the process may have died while the call is returning.
     */
    public boolean isBinderAlive();
    
    /**
     * Attempt to retrieve a local implementation of an interface
     * for this Binder object.  If null is returned, you will need
     * to instantiate a proxy class to marshall calls through
     * the transact() method.
     */
    public @Nullable IInterface queryLocalInterface(@NonNull String descriptor);
    /**
     * Print the object's state into the given stream.
     * 
     * @param fd The raw file descriptor that the dump is being sent to.
     * @param args additional arguments to the dump request.
     */
    public void dump(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException;
    /**
     * Like {@link #dump(FileDescriptor, String[])} but always executes
     * asynchronously.  If the object is local, a new thread is created
     * to perform the dump.
     *
     * @param fd The raw file descriptor that the dump is being sent to.
     * @param args additional arguments to the dump request.
     */
    public void dumpAsync(@NonNull FileDescriptor fd, @Nullable String[] args)
            throws RemoteException;
    /**
     * Execute a shell command on this object.  This may be performed asynchrously from the caller;
     * the implementation must always call resultReceiver when finished.
     *
     * @param in The raw file descriptor that an input data stream can be read from.
     * @param out The raw file descriptor that normal command messages should be written to.
     * @param err The raw file descriptor that command error messages should be written to.
     * @param args Command-line arguments.
     * @param shellCallback Optional callback to the caller's shell to perform operations in it.
     * @param resultReceiver Called when the command has finished executing, with the result code.
     * @hide
     */
    public void shellCommand(@Nullable FileDescriptor in, @Nullable FileDescriptor out,
            @Nullable FileDescriptor err,
            @NonNull String[] args, @Nullable ShellCallback shellCallback,
            @NonNull ResultReceiver resultReceiver) throws RemoteException;
    /**
     * Get the binder extension of this binder interface.
     * This allows one to customize an interface without having to modify the original interface.
     *
     * @return null if don't have binder extension
     * @throws RemoteException
     * @hide
     */
    public default @Nullable IBinder getExtension() throws RemoteException {
        throw new IllegalStateException("Method is not implemented");
    }
    /**
     * Perform a generic operation with the object.
     * 
     * @param code The action to perform.  This should
     * be a number between {@link #FIRST_CALL_TRANSACTION} and
     * {@link #LAST_CALL_TRANSACTION}.
     * @param data Marshalled data to send to the target.  Must not be null.
     * If you are not sending any data, you must create an empty Parcel
     * that is given here.
     * @param reply Marshalled data to be received from the target.  May be
     * null if you are not interested in the return value.
     * @param flags Additional operation flags.  Either 0 for a normal
     * RPC, or {@link #FLAG_ONEWAY} for a one-way RPC.
     *
     * @return Returns the result from {@link Binder#onTransact}.  A successful call
     * generally returns true; false generally means the transaction code was not
     * understood.
     */
    public boolean transact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags)
        throws RemoteException;
    /**
     * Interface for receiving a callback when the process hosting an IBinder
     * has gone away.
     * 
     * @see #linkToDeath
     */
    public interface DeathRecipient {
        public void binderDied();
    }
    /**
     * Register the recipient for a notification if this binder
     * goes away.  If this binder object unexpectedly goes away
     * (typically because its hosting process has been killed),
     * then the given {@link DeathRecipient}'s
     * {@link DeathRecipient#binderDied DeathRecipient.binderDied()} method
     * will be called.
     *
     * <p>This will automatically be unlinked when all references to the linked
     * binder proxy are dropped.</p>
     *
     * <p>You will only receive death notifications for remote binders,
     * as local binders by definition can't die without you dying as well.</p>
     *
     * @throws RemoteException if the target IBinder's
     * process has already died.
     *
     * @see #unlinkToDeath
     */
    public void linkToDeath(@NonNull DeathRecipient recipient, int flags)
            throws RemoteException;
    /**
     * Remove a previously registered death notification.
     * The recipient will no longer be called if this object
     * dies.
     * 
     * @return {@code true} if the <var>recipient</var> is successfully
     * unlinked, assuring you that its
     * {@link DeathRecipient#binderDied DeathRecipient.binderDied()} method
     * will not be called;  {@code false} if the target IBinder has already
     * died, meaning the method has been (or soon will be) called.
     * 
     * @throws java.util.NoSuchElementException if the given
     * <var>recipient</var> has not been registered with the IBinder, and
     * the IBinder is still alive.  Note that if the <var>recipient</var>
     * was never registered, but the IBinder has already died, then this
     * exception will <em>not</em> be thrown, and you will receive a false
     * return value instead.
     */
    public boolean unlinkToDeath(@NonNull DeathRecipient recipient, int flags);
}
```

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
package android.os;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SystemApi;
import android.compat.annotation.UnsupportedAppUsage;
import android.util.ExceptionUtils;
import android.util.Log;
import android.util.Slog;
import com.android.internal.os.BinderInternal;
import com.android.internal.os.BinderInternal.CallSession;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.FunctionalUtils.ThrowingRunnable;
import com.android.internal.util.FunctionalUtils.ThrowingSupplier;
import dalvik.annotation.optimization.CriticalNative;
import libcore.io.IoUtils;
import libcore.util.NativeAllocationRegistry;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
/**
 * Base class for a remotable object, the core part of a lightweight
 * remote procedure call mechanism defined by {@link IBinder}.
 * This class is an implementation of IBinder that provides
 * standard local implementation of such an object.
 *
 * <p>Most developers will not implement this class directly, instead using the
 * <a href="{@docRoot}guide/components/aidl.html">aidl</a> tool to describe the desired
 * interface, having it generate the appropriate Binder subclass.  You can,
 * however, derive directly from Binder to implement your own custom RPC
 * protocol or simply instantiate a raw Binder object directly to use as a
 * token that can be shared across processes.
 *
 * <p>This class is just a basic IPC primitive; it has no impact on an application's
 * lifecycle, and is valid only as long as the process that created it continues to run.
 * To use this correctly, you must be doing so within the context of a top-level
 * application component (a {@link android.app.Service}, {@link android.app.Activity},
 * or {@link android.content.ContentProvider}) that lets the system know your process
 * should remain running.</p>
 *
 * <p>You must keep in mind the situations in which your process
 * could go away, and thus require that you later re-create a new Binder and re-attach
 * it when the process starts again.  For example, if you are using this within an
 * {@link android.app.Activity}, your activity's process may be killed any time the
 * activity is not started; if the activity is later re-created you will need to
 * create a new Binder and hand it back to the correct place again; you need to be
 * aware that your process may be started for another reason (for example to receive
 * a broadcast) that will not involve re-creating the activity and thus run its code
 * to create a new Binder.</p>
 *
 * @see IBinder
 */
public class Binder implements IBinder {
    /*
     * Set this flag to true to detect anonymous, local or member classes
     * that extend this Binder class and that are not static. These kind
     * of classes can potentially create leaks.
     */
    private static final boolean FIND_POTENTIAL_LEAKS = false;
    /** @hide */
    public static final boolean CHECK_PARCEL_SIZE = false;
    static final String TAG = "Binder";
    /** @hide */
    public static boolean LOG_RUNTIME_EXCEPTION = false; // DO NOT SUBMIT WITH TRUE
    /**
     * Value to represents that a calling work source is not set.
     *
     * This constatnt needs to be kept in sync with IPCThreadState::kUnsetWorkSource.
     *
     * @hide
     */
    public static final int UNSET_WORKSOURCE = -1;
    /**
     * Control whether dump() calls are allowed.
     */
    private static volatile String sDumpDisabled = null;
    /**
     * Global transaction tracker instance for this process.
     */
    private static volatile TransactionTracker sTransactionTracker = null;
    /**
     * Global observer for this process.
     */
    private static BinderInternal.Observer sObserver = null;
    /**
     * Guestimate of native memory associated with a Binder.
     */
    private static final int NATIVE_ALLOCATION_SIZE = 500;
    private static native long getNativeFinalizer();
    // Use a Holder to allow static initialization of Binder in the boot image, and
    // possibly to avoid some initialization ordering issues.
    private static class NoImagePreloadHolder {
        public static final NativeAllocationRegistry sRegistry = new NativeAllocationRegistry(
                Binder.class.getClassLoader(), getNativeFinalizer(), NATIVE_ALLOCATION_SIZE);
    }
    // Transaction tracking code.
    /**
     * Flag indicating whether we should be tracing transact calls.
     */
    private static volatile boolean sTracingEnabled = false;
    /**
     * Enable Binder IPC tracing.
     *
     * @hide
     */
    public static void enableTracing() {
        sTracingEnabled = true;
    }
    /**
     * Disable Binder IPC tracing.
     *
     * @hide
     */
    public static void disableTracing() {
        sTracingEnabled = false;
    }
    /**
     * Check if binder transaction tracing is enabled.
     *
     * @hide
     */
    public static boolean isTracingEnabled() {
        return sTracingEnabled;
    }
    /**
     * Get the binder transaction tracker for this process.
     *
     * @hide
     */
    public synchronized static TransactionTracker getTransactionTracker() {
        if (sTransactionTracker == null)
            sTransactionTracker = new TransactionTracker();
        return sTransactionTracker;
    }
    /**
     * Get the binder transaction observer for this process.
     *
     * @hide
     */
    public static void setObserver(@Nullable BinderInternal.Observer observer) {
        sObserver = observer;
    }
    /** {@hide} */
    static volatile boolean sWarnOnBlocking = false;
    /**
     * Warn if any blocking binder transactions are made out from this process.
     * This is typically only useful for the system process, to prevent it from
     * blocking on calls to external untrusted code. Instead, all outgoing calls
     * that require a result must be sent as {@link IBinder#FLAG_ONEWAY} calls
     * which deliver results through a callback interface.
     *
     * @hide
     */
    public static void setWarnOnBlocking(boolean warnOnBlocking) {
        sWarnOnBlocking = warnOnBlocking;
    }
    /**
     * Allow blocking calls on the given interface, overriding the requested
     * value of {@link #setWarnOnBlocking(boolean)}.
     * <p>
     * This should only be rarely called when you are <em>absolutely sure</em>
     * the remote interface is a built-in system component that can never be
     * upgraded. In particular, this <em>must never</em> be called for
     * interfaces hosted by package that could be upgraded or replaced,
     * otherwise you risk system instability if that remote interface wedges.
     *
     * @hide
     */
    public static IBinder allowBlocking(IBinder binder) {
        try {
            if (binder instanceof BinderProxy) {
                ((BinderProxy) binder).mWarnOnBlocking = false;
            } else if (binder != null && binder.getInterfaceDescriptor() != null
                    && binder.queryLocalInterface(binder.getInterfaceDescriptor()) == null) {
                Log.w(TAG, "Unable to allow blocking on interface " + binder);
            }
        } catch (RemoteException ignored) {
        }
        return binder;
    }
    /**
     * Reset the given interface back to the default blocking behavior,
     * reverting any changes made by {@link #allowBlocking(IBinder)}.
     *
     * @hide
     */
    public static IBinder defaultBlocking(IBinder binder) {
        if (binder instanceof BinderProxy) {
            ((BinderProxy) binder).mWarnOnBlocking = sWarnOnBlocking;
        }
        return binder;
    }
    /**
     * Inherit the current {@link #allowBlocking(IBinder)} value from one given
     * interface to another.
     *
     * @hide
     */
    public static void copyAllowBlocking(IBinder fromBinder, IBinder toBinder) {
        if (fromBinder instanceof BinderProxy && toBinder instanceof BinderProxy) {
            ((BinderProxy) toBinder).mWarnOnBlocking = ((BinderProxy) fromBinder).mWarnOnBlocking;
        }
    }
    /**
     * Raw native pointer to JavaBBinderHolder object. Owned by this Java object. Not null.
     */
    @UnsupportedAppUsage
    private final long mObject;
    private IInterface mOwner;
    private String mDescriptor;
    /**
     * Return the ID of the process that sent you the current transaction
     * that is being processed.  This pid can be used with higher-level
     * system services to determine its identity and check permissions.
     * If the current thread is not currently executing an incoming transaction,
     * then its own pid is returned.
     *
     * Warning: oneway transactions do not receive PID.
     */
    @CriticalNative
    public static final native int getCallingPid();
    /**
     * Return the Linux uid assigned to the process that sent you the
     * current transaction that is being processed.  This uid can be used with
     * higher-level system services to determine its identity and check
     * permissions.  If the current thread is not currently executing an
     * incoming transaction, then its own uid is returned.
     */
    @CriticalNative
    public static final native int getCallingUid();
    /**
     * Returns {@code true} if the current thread is currently executing an
     * incoming transaction.
     *
     * @hide
     */
    @CriticalNative
    public static final native boolean isHandlingTransaction();
    /**
     * Return the Linux uid assigned to the process that sent the transaction
     * currently being processed.
     *
     * @throws IllegalStateException if the current thread is not currently
     *        executing an incoming transaction.
     */
    public static final int getCallingUidOrThrow() {
        if (!isHandlingTransaction()) {
            throw new IllegalStateException(
                  "Thread is not in a binder transcation");
        }
        return getCallingUid();
    }
    /**
     * Return the UserHandle assigned to the process that sent you the
     * current transaction that is being processed.  This is the user
     * of the caller.  It is distinct from {@link #getCallingUid()} in that a
     * particular user will have multiple distinct apps running under it each
     * with their own uid.  If the current thread is not currently executing an
     * incoming transaction, then its own UserHandle is returned.
     */
    public static final @NonNull UserHandle getCallingUserHandle() {
        return UserHandle.of(UserHandle.getUserId(getCallingUid()));
    }
    /**
     * Reset the identity of the incoming IPC on the current thread.  This can
     * be useful if, while handling an incoming call, you will be calling
     * on interfaces of other objects that may be local to your process and
     * need to do permission checks on the calls coming into them (so they
     * will check the permission of your own local process, and not whatever
     * process originally called you).
     *
     * @return Returns an opaque token that can be used to restore the
     * original calling identity by passing it to
     * {@link #restoreCallingIdentity(long)}.
     *
     * @see #getCallingPid()
     * @see #getCallingUid()
     * @see #restoreCallingIdentity(long)
     */
    @CriticalNative
    public static final native long clearCallingIdentity();
    /**
     * Restore the identity of the incoming IPC on the current thread
     * back to a previously identity that was returned by {@link
     * #clearCallingIdentity}.
     *
     * @param token The opaque token that was previously returned by
     * {@link #clearCallingIdentity}.
     *
     * @see #clearCallingIdentity
     */
    public static final native void restoreCallingIdentity(long token);
    /**
     * Convenience method for running the provided action enclosed in
     * {@link #clearCallingIdentity}/{@link #restoreCallingIdentity}
     *
     * Any exception thrown by the given action will be caught and rethrown after the call to
     * {@link #restoreCallingIdentity}
     *
     * @hide
     */
    public static final void withCleanCallingIdentity(@NonNull ThrowingRunnable action) {
        long callingIdentity = clearCallingIdentity();
        Throwable throwableToPropagate = null;
        try {
            action.runOrThrow();
        } catch (Throwable throwable) {
            throwableToPropagate = throwable;
        } finally {
            restoreCallingIdentity(callingIdentity);
            if (throwableToPropagate != null) {
                throw ExceptionUtils.propagate(throwableToPropagate);
            }
        }
    }
    /**
     * Convenience method for running the provided action enclosed in
     * {@link #clearCallingIdentity}/{@link #restoreCallingIdentity} returning the result
     *
     * Any exception thrown by the given action will be caught and rethrown after the call to
     * {@link #restoreCallingIdentity}
     *
     * @hide
     */
    public static final <T> T withCleanCallingIdentity(@NonNull ThrowingSupplier<T> action) {
        long callingIdentity = clearCallingIdentity();
        Throwable throwableToPropagate = null;
        try {
            return action.getOrThrow();
        } catch (Throwable throwable) {
            throwableToPropagate = throwable;
            return null; // overridden by throwing in finally block
        } finally {
            restoreCallingIdentity(callingIdentity);
            if (throwableToPropagate != null) {
                throw ExceptionUtils.propagate(throwableToPropagate);
            }
        }
    }
    /**
     * Sets the native thread-local StrictMode policy mask.
     *
     * <p>The StrictMode settings are kept in two places: a Java-level
     * threadlocal for libcore/Dalvik, and a native threadlocal (set
     * here) for propagation via Binder calls.  This is a little
     * unfortunate, but necessary to break otherwise more unfortunate
     * dependencies either of Dalvik on Android, or Android
     * native-only code on Dalvik.
     *
     * @see StrictMode
     * @hide
     */
    @CriticalNative
    public static final native void setThreadStrictModePolicy(int policyMask);
    /**
     * Gets the current native thread-local StrictMode policy mask.
     *
     * @see #setThreadStrictModePolicy
     * @hide
     */
    @CriticalNative
    public static final native int getThreadStrictModePolicy();
    /**
     * Sets the work source for this thread.
     *
     * <p>All the following binder calls on this thread will use the provided work source. If this
     * is called during an on-going binder transaction, all the following binder calls will use the
     * work source until the end of the transaction.
     *
     * <p>The concept of worksource is similar to {@link WorkSource}. However, for performance
     * reasons, we only support one UID. This UID represents the original user responsible for the
     * binder calls.
     *
     * <p>{@link Binder#restoreCallingWorkSource(long)} must always be called after setting the
     * worksource.
     *
     * <p>A typical use case would be
     * <pre>
     * long token = Binder.setCallingWorkSourceUid(uid);
     * try {
     *   // Call an API.
     * } finally {
     *   Binder.restoreCallingWorkSource(token);
     * }
     * </pre>
     *
     * <p>The work source will be propagated for future outgoing binder transactions
     * executed on this thread.
     *
     * @param workSource The original UID responsible for the binder call.
     * @return token to restore original work source.
     **/
    @CriticalNative
    public static final native long setCallingWorkSourceUid(int workSource);
    /**
     * Returns the work source set by the caller.
     *
     * Unlike {@link Binder#getCallingUid()}, this result of this method cannot be trusted. The
     * caller can set the value to whatever they want. Only use this value if you trust the calling
     * uid.
     *
     * @return The original UID responsible for the binder transaction.
     */
    @CriticalNative
    public static final native int getCallingWorkSourceUid();
    /**
     * Clears the work source on this thread.
     *
     * <p>The work source will be propagated for future outgoing binder transactions
     * executed on this thread.
     *
     * <p>{@link Binder#restoreCallingWorkSource(long)} must always be called after clearing the
     * worksource.
     *
     * <p>A typical use case would be
     * <pre>
     * long token = Binder.clearCallingWorkSource();
     * try {
     *   // Call an API.
     * } finally {
     *   Binder.restoreCallingWorkSource(token);
     * }
     * </pre>
     *
     * @return token to restore original work source.
     **/
    @CriticalNative
    public static final native long clearCallingWorkSource();
    /**
     * Restores the work source on this thread using a token returned by
     * {@link #setCallingWorkSourceUid(int) or {@link clearCallingWorkSource()}.
     *
     * <p>A typical use case would be
     * <pre>
     * long token = Binder.setCallingWorkSourceUid(uid);
     * try {
     *   // Call an API.
     * } finally {
     *   Binder.restoreCallingWorkSource(token);
     * }
     * </pre>
     **/
    @CriticalNative
    public static final native void restoreCallingWorkSource(long token);
    /**
     * Mark as being built with VINTF-level stability promise. This API should
     * only ever be invoked by the build system. It means that the interface
     * represented by this binder is guaranteed to be kept stable for several
     * years, and the build system also keeps snapshots of these APIs and
     * invokes the AIDL compiler to make sure that these snapshots are
     * backwards compatible. Instead of using this API, use an @VintfStability
     * interface.
     *
     * @hide
     */
    public final native void markVintfStability();
    /**
     * Flush any Binder commands pending in the current thread to the kernel
     * driver.  This can be
     * useful to call before performing an operation that may block for a long
     * time, to ensure that any pending object references have been released
     * in order to prevent the process from holding on to objects longer than
     * it needs to.
     */
    public static final native void flushPendingCommands();
    /**
     * Add the calling thread to the IPC thread pool.  This function does
     * not return until the current process is exiting.
     */
    public static final void joinThreadPool() {
        BinderInternal.joinThreadPool();
    }
    /**
     * Returns true if the specified interface is a proxy.
     * @hide
     */
    public static final boolean isProxy(IInterface iface) {
        return iface.asBinder() != iface;
    }
    /**
     * Call blocks until the number of executing binder threads is less
     * than the maximum number of binder threads allowed for this process.
     * @hide
     */
    public static final native void blockUntilThreadAvailable();
    /**
     * Default constructor just initializes the object.
     *
     * If you're creating a Binder token (a Binder object without an attached interface),
     * you should use {@link #Binder(String)} instead.
     */
    public Binder() {
        this(null);
    }
    /**
     * Constructor for creating a raw Binder object (token) along with a descriptor.
     *
     * The descriptor of binder objects usually specifies the interface they are implementing.
     * In case of binder tokens, no interface is implemented, and the descriptor can be used
     * as a sort of tag to help identify the binder token. This will help identify remote
     * references to these objects more easily when debugging.
     *
     * @param descriptor Used to identify the creator of this token, for example the class name.
     * Instead of creating multiple tokens with the same descriptor, consider adding a suffix to
     * help identify them.
     */
    public Binder(@Nullable String descriptor)  {
        mObject = getNativeBBinderHolder();
        NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, mObject);
        if (FIND_POTENTIAL_LEAKS) {
            final Class<? extends Binder> klass = getClass();
            if ((klass.isAnonymousClass() || klass.isMemberClass() || klass.isLocalClass()) &&
                    (klass.getModifiers() & Modifier.STATIC) == 0) {
                Log.w(TAG, "The following Binder class should be static or leaks might occur: " +
                    klass.getCanonicalName());
            }
        }
        mDescriptor = descriptor;
    }
    /**
     * Convenience method for associating a specific interface with the Binder.
     * After calling, queryLocalInterface() will be implemented for you
     * to return the given owner IInterface when the corresponding
     * descriptor is requested.
     */
    public void attachInterface(@Nullable IInterface owner, @Nullable String descriptor) {
        mOwner = owner;
        mDescriptor = descriptor;
    }
    /**
     * Default implementation returns an empty interface name.
     */
    public @Nullable String getInterfaceDescriptor() {
        return mDescriptor;
    }
    /**
     * Default implementation always returns true -- if you got here,
     * the object is alive.
     */
    public boolean pingBinder() {
        return true;
    }
    /**
     * {@inheritDoc}
     *
     * Note that if you're calling on a local binder, this always returns true
     * because your process is alive if you're calling it.
     */
    public boolean isBinderAlive() {
        return true;
    }
    /**
     * Use information supplied to attachInterface() to return the
     * associated IInterface if it matches the requested
     * descriptor.
     */
    public @Nullable IInterface queryLocalInterface(@NonNull String descriptor) {
        if (mDescriptor != null && mDescriptor.equals(descriptor)) {
            return mOwner;
        }
        return null;
    }
    /**
     * Control disabling of dump calls in this process.  This is used by the system
     * process watchdog to disable incoming dump calls while it has detecting the system
     * is hung and is reporting that back to the activity controller.  This is to
     * prevent the controller from getting hung up on bug reports at this point.
     * @hide
     *
     * @param msg The message to show instead of the dump; if null, dumps are
     * re-enabled.
     */
    public static void setDumpDisabled(String msg) {
        sDumpDisabled = msg;
    }
    /**
     * Listener to be notified about each proxy-side binder call.
     *
     * See {@link setProxyTransactListener}.
     * @hide
     */
    @SystemApi
    public interface ProxyTransactListener {
        /**
         * Called before onTransact.
         *
         * @return an object that will be passed back to #onTransactEnded (or null).
         */
        @Nullable
        Object onTransactStarted(@NonNull IBinder binder, int transactionCode);
        /**
         * Called after onTranact (even when an exception is thrown).
         *
         * @param session The object return by #onTransactStarted.
         */
        void onTransactEnded(@Nullable Object session);
    }
    /**
     * Propagates the work source to binder calls executed by the system server.
     *
     * <li>By default, this listener will propagate the worksource if the outgoing call happens on
     * the same thread as the incoming binder call.
     * <li>Custom attribution can be done by calling {@link ThreadLocalWorkSource#setUid(int)}.
     * @hide
     */
    public static class PropagateWorkSourceTransactListener implements ProxyTransactListener {
        @Override
        public Object onTransactStarted(IBinder binder, int transactionCode) {
           // Note that {@link Binder#getCallingUid()} is already set to the UID of the current
           // process when this method is called.
           //
           // We use ThreadLocalWorkSource instead. It also allows feature owners to set
           // {@link ThreadLocalWorkSource#set(int) manually to attribute resources to a UID.
            int uid = ThreadLocalWorkSource.getUid();
            if (uid != ThreadLocalWorkSource.UID_NONE) {
                return Binder.setCallingWorkSourceUid(uid);
            }
            return null;
        }
        @Override
        public void onTransactEnded(Object session) {
            if (session != null) {
                long token = (long) session;
                Binder.restoreCallingWorkSource(token);
            }
        }
    }
    /**
     * Sets a listener for the transact method on the proxy-side.
     *
     * <li>The listener is global. Only fast operations should be done to avoid thread
     * contentions.
     * <li>The listener implementation needs to handle synchronization if needed. The methods on the
     * listener can be called concurrently.
     * <li>Listener set will be used for new transactions. On-going transaction will still use the
     * previous listener (if already set).
     * <li>The listener is called on the critical path of the binder transaction so be careful about
     * performance.
     * <li>Never execute another binder transaction inside the listener.
     * @hide
     */
    @SystemApi
    public static void setProxyTransactListener(@Nullable ProxyTransactListener listener) {
        BinderProxy.setTransactListener(listener);
    }
    /**
     * Default implementation is a stub that returns false.  You will want
     * to override this to do the appropriate unmarshalling of transactions.
     *
     * <p>If you want to call this, call transact().
     *
     * <p>Implementations that are returning a result should generally use
     * {@link Parcel#writeNoException() Parcel.writeNoException} and
     * {@link Parcel#writeException(Exception) Parcel.writeException} to propagate
     * exceptions back to the caller.</p>
     *
     * @param code The action to perform.  This should
     * be a number between {@link #FIRST_CALL_TRANSACTION} and
     * {@link #LAST_CALL_TRANSACTION}.
     * @param data Marshalled data being received from the caller.
     * @param reply If the caller is expecting a result back, it should be marshalled
     * in to here.
     * @param flags Additional operation flags.  Either 0 for a normal
     * RPC, or {@link #FLAG_ONEWAY} for a one-way RPC.
     *
     * @return Return true on a successful call; returning false is generally used to
     * indicate that you did not understand the transaction code.
     */
    protected boolean onTransact(int code, @NonNull Parcel data, @Nullable Parcel reply,
            int flags) throws RemoteException {
        if (code == INTERFACE_TRANSACTION) {
            reply.writeString(getInterfaceDescriptor());
            return true;
        } else if (code == DUMP_TRANSACTION) {
            ParcelFileDescriptor fd = data.readFileDescriptor();
            String[] args = data.readStringArray();
            if (fd != null) {
                try {
                    dump(fd.getFileDescriptor(), args);
                } finally {
                    IoUtils.closeQuietly(fd);
                }
            }
            // Write the StrictMode header.
            if (reply != null) {
                reply.writeNoException();
            } else {
                StrictMode.clearGatheredViolations();
            }
            return true;
        } else if (code == SHELL_COMMAND_TRANSACTION) {
            ParcelFileDescriptor in = data.readFileDescriptor();
            ParcelFileDescriptor out = data.readFileDescriptor();
            ParcelFileDescriptor err = data.readFileDescriptor();
            String[] args = data.readStringArray();
            ShellCallback shellCallback = ShellCallback.CREATOR.createFromParcel(data);
            ResultReceiver resultReceiver = ResultReceiver.CREATOR.createFromParcel(data);
            try {
                if (out != null) {
                    shellCommand(in != null ? in.getFileDescriptor() : null,
                            out.getFileDescriptor(),
                            err != null ? err.getFileDescriptor() : out.getFileDescriptor(),
                            args, shellCallback, resultReceiver);
                }
            } finally {
                IoUtils.closeQuietly(in);
                IoUtils.closeQuietly(out);
                IoUtils.closeQuietly(err);
                // Write the StrictMode header.
                if (reply != null) {
                    reply.writeNoException();
                } else {
                    StrictMode.clearGatheredViolations();
                }
            }
            return true;
        }
        return false;
    }
    /**
     * Resolves a transaction code to a human readable name.
     *
     * <p>Default implementation is a stub that returns null.
     * <p>AIDL generated code will return the original method name.
     *
     * @param transactionCode The code to resolve.
     * @return A human readable name.
     * @hide
     */
    public @Nullable String getTransactionName(int transactionCode) {
        return null;
    }
    /**
     * Implemented to call the more convenient version
     * {@link #dump(FileDescriptor, PrintWriter, String[])}.
     */
    public void dump(@NonNull FileDescriptor fd, @Nullable String[] args) {
        FileOutputStream fout = new FileOutputStream(fd);
        PrintWriter pw = new FastPrintWriter(fout);
        try {
            doDump(fd, pw, args);
        } finally {
            pw.flush();
        }
    }
    void doDump(FileDescriptor fd, PrintWriter pw, String[] args) {
        final String disabled = sDumpDisabled;
        if (disabled == null) {
            try {
                dump(fd, pw, args);
            } catch (SecurityException e) {
                pw.println("Security exception: " + e.getMessage());
                throw e;
            } catch (Throwable e) {
                // Unlike usual calls, in this case if an exception gets thrown
                // back to us we want to print it back in to the dump data, since
                // that is where the caller expects all interesting information to
                // go.
                pw.println();
                pw.println("Exception occurred while dumping:");
                e.printStackTrace(pw);
            }
        } else {
            pw.println(sDumpDisabled);
        }
    }
    /**
     * Like {@link #dump(FileDescriptor, String[])}, but ensures the target
     * executes asynchronously.
     */
    public void dumpAsync(@NonNull final FileDescriptor fd, @Nullable final String[] args) {
        final FileOutputStream fout = new FileOutputStream(fd);
        final PrintWriter pw = new FastPrintWriter(fout);
        Thread thr = new Thread("Binder.dumpAsync") {
            public void run() {
                try {
                    dump(fd, pw, args);
                } finally {
                    pw.flush();
                }
            }
        };
        thr.start();
    }
    /**
     * Print the object's state into the given stream.
     *
     * @param fd The raw file descriptor that the dump is being sent to.
     * @param fout The file to which you should dump your state.  This will be
     * closed for you after you return.
     * @param args additional arguments to the dump request.
     */
    protected void dump(@NonNull FileDescriptor fd, @NonNull PrintWriter fout,
            @Nullable String[] args) {
    }
    /**
     * @param in The raw file descriptor that an input data stream can be read from.
     * @param out The raw file descriptor that normal command messages should be written to.
     * @param err The raw file descriptor that command error messages should be written to.
     * @param args Command-line arguments.
     * @param callback Callback through which to interact with the invoking shell.
     * @param resultReceiver Called when the command has finished executing, with the result code.
     * @throws RemoteException
     * @hide
     */
    public void shellCommand(@Nullable FileDescriptor in, @Nullable FileDescriptor out,
            @Nullable FileDescriptor err,
            @NonNull String[] args, @Nullable ShellCallback callback,
            @NonNull ResultReceiver resultReceiver) throws RemoteException {
        onShellCommand(in, out, err, args, callback, resultReceiver);
    }
    /**
     * Handle a call to {@link #shellCommand}.  The default implementation simply prints
     * an error message.  Override and replace with your own.
     * <p class="caution">Note: no permission checking is done before calling this method; you must
     * apply any security checks as appropriate for the command being executed.
     * Consider using {@link ShellCommand} to help in the implementation.</p>
     * @hide
     */
    public void onShellCommand(@Nullable FileDescriptor in, @Nullable FileDescriptor out,
            @Nullable FileDescriptor err,
            @NonNull String[] args, @Nullable ShellCallback callback,
            @NonNull ResultReceiver resultReceiver) throws RemoteException {
        FileOutputStream fout = new FileOutputStream(err != null ? err : out);
        PrintWriter pw = new FastPrintWriter(fout);
        pw.println("No shell command implementation.");
        pw.flush();
        resultReceiver.send(0, null);
    }
    /** @hide */
    @Override
    public final native @Nullable IBinder getExtension();
    /**
     * Set the binder extension.
     * This should be called immediately when the object is created.
     *
     * @hide
     */
    public final native void setExtension(@Nullable IBinder extension);
    /**
     * Default implementation rewinds the parcels and calls onTransact.  On
     * the remote side, transact calls into the binder to do the IPC.
     */
    public final boolean transact(int code, @NonNull Parcel data, @Nullable Parcel reply,
            int flags) throws RemoteException {
        if (false) Log.v("Binder", "Transact: " + code + " to " + this);
        if (data != null) {
            data.setDataPosition(0);
        }
        boolean r = onTransact(code, data, reply, flags);
        if (reply != null) {
            reply.setDataPosition(0);
        }
        return r;
    }
    /**
     * Local implementation is a no-op.
     */
    public void linkToDeath(@NonNull DeathRecipient recipient, int flags) {
    }
    /**
     * Local implementation is a no-op.
     */
    public boolean unlinkToDeath(@NonNull DeathRecipient recipient, int flags) {
        return true;
    }
    static void checkParcel(IBinder obj, int code, Parcel parcel, String msg) {
        if (CHECK_PARCEL_SIZE && parcel.dataSize() >= 800*1024) {
            // Trying to send > 800k, this is way too much
            StringBuilder sb = new StringBuilder();
            sb.append(msg);
            sb.append(": on ");
            sb.append(obj);
            sb.append(" calling ");
            sb.append(code);
            sb.append(" size ");
            sb.append(parcel.dataSize());
            sb.append(" (data: ");
            parcel.setDataPosition(0);
            sb.append(parcel.readInt());
            sb.append(", ");
            sb.append(parcel.readInt());
            sb.append(", ");
            sb.append(parcel.readInt());
            sb.append(")");
            Slog.wtfStack(TAG, sb.toString());
        }
    }
    private static native long getNativeBBinderHolder();
    private static native long getFinalizer();
    /**
     * By default, we use the calling uid since we can always trust it.
     */
    private static volatile BinderInternal.WorkSourceProvider sWorkSourceProvider =
            (x) -> Binder.getCallingUid();
    /**
     * Sets the work source provider.
     *
     * <li>The callback is global. Only fast operations should be done to avoid thread
     * contentions.
     * <li>The callback implementation needs to handle synchronization if needed. The methods on the
     * callback can be called concurrently.
     * <li>The callback is called on the critical path of the binder transaction so be careful about
     * performance.
     * <li>Never execute another binder transaction inside the callback.
     * @hide
     */
    public static void setWorkSourceProvider(BinderInternal.WorkSourceProvider workSourceProvider) {
        if (workSourceProvider == null) {
            throw new IllegalArgumentException("workSourceProvider cannot be null");
        }
        sWorkSourceProvider = workSourceProvider;
    }
    // Entry point from android_util_Binder.cpp's onTransact
    @UnsupportedAppUsage
    private boolean execTransact(int code, long dataObj, long replyObj,
            int flags) {
        // At that point, the parcel request headers haven't been parsed so we do not know what
        // WorkSource the caller has set. Use calling uid as the default.
        final int callingUid = Binder.getCallingUid();
        final long origWorkSource = ThreadLocalWorkSource.setUid(callingUid);
        try {
            return execTransactInternal(code, dataObj, replyObj, flags, callingUid);
        } finally {
            ThreadLocalWorkSource.restore(origWorkSource);
        }
    }
    private boolean execTransactInternal(int code, long dataObj, long replyObj, int flags,
            int callingUid) {
        // Make sure the observer won't change while processing a transaction.
        final BinderInternal.Observer observer = sObserver;
        final CallSession callSession =
                observer != null ? observer.callStarted(this, code, UNSET_WORKSOURCE) : null;
        Parcel data = Parcel.obtain(dataObj);
        Parcel reply = Parcel.obtain(replyObj);
        // theoretically, we should call transact, which will call onTransact,
        // but all that does is rewind it, and we just got these from an IPC,
        // so we'll just call it directly.
        boolean res;
        // Log any exceptions as warnings, don't silently suppress them.
        // If the call was FLAG_ONEWAY then these exceptions disappear into the ether.
        final boolean tracingEnabled = Binder.isTracingEnabled();
        try {
            if (tracingEnabled) {
                final String transactionName = getTransactionName(code);
                Trace.traceBegin(Trace.TRACE_TAG_ALWAYS, getClass().getName() + ":"
                        + (transactionName != null ? transactionName : code));
            }
            res = onTransact(code, data, reply, flags);
        } catch (RemoteException|RuntimeException e) {
            if (observer != null) {
                observer.callThrewException(callSession, e);
            }
            if (LOG_RUNTIME_EXCEPTION) {
                Log.w(TAG, "Caught a RuntimeException from the binder stub implementation.", e);
            }
            if ((flags & FLAG_ONEWAY) != 0) {
                if (e instanceof RemoteException) {
                    Log.w(TAG, "Binder call failed.", e);
                } else {
                    Log.w(TAG, "Caught a RuntimeException from the binder stub implementation.", e);
                }
            } else {
                // Clear the parcel before writing the exception
                reply.setDataSize(0);
                reply.setDataPosition(0);
                reply.writeException(e);
            }
            res = true;
        } finally {
            if (tracingEnabled) {
                Trace.traceEnd(Trace.TRACE_TAG_ALWAYS);
            }
            if (observer != null) {
                // The parcel RPC headers have been called during onTransact so we can now access
                // the worksource uid from the parcel.
                final int workSourceUid = sWorkSourceProvider.resolveWorkSourceUid(
                        data.readCallingWorkSourceUid());
                observer.callEnded(callSession, data.dataSize(), reply.dataSize(), workSourceUid);
            }
        }
        checkParcel(this, code, reply, "Unreasonably large binder reply buffer");
        reply.recycle();
        data.recycle();
        // Just in case -- we are done with the IPC, so there should be no more strict
        // mode violations that have gathered for this thread.  Either they have been
        // parceled and are now in transport off to the caller, or we are returning back
        // to the main transaction loop to wait for another incoming transaction.  Either
        // way, strict mode begone!
        StrictMode.clearGatheredViolations();
        return res;
    }
    /**
     * Returns the specified service from servicemanager. If the service is not running,
     * servicemanager will attempt to start it, and this function will wait for it to be ready.
     * Returns nullptr only if there are permission problems or fatal errors.
     * @hide
     */
    public static final native @Nullable IBinder waitForService(@NonNull String serviceName)
            throws RemoteException;
}
```

### binder.h

```cpp
/* Copyright 2008 The Android Open Source Project
 */
#ifndef _BINDER_H_
#define _BINDER_H_
#include <sys/ioctl.h>
#include <linux/android/binder.h>
struct binder_state;
struct binder_io
{
    char *data;            /* pointer to read/write from */
    binder_size_t *offs;   /* array of offsets */
    size_t data_avail;     /* bytes available in data buffer */
    size_t offs_avail;     /* entries available in offsets array */
    char *data0;           /* start of data buffer */
    binder_size_t *offs0;  /* start of offsets buffer */
    uint32_t flags;
    uint32_t unused;
};
struct binder_death {
    void (*func)(struct binder_state *bs, void *ptr);
    void *ptr;
};
/* the one magic handle */
#define BINDER_SERVICE_MANAGER  0U
#define SVC_MGR_NAME "android.os.IServiceManager"
enum {
    /* Must match definitions in IBinder.h and IServiceManager.h */
    PING_TRANSACTION  = B_PACK_CHARS('_','P','N','G'),
    SVC_MGR_GET_SERVICE = 1,
    SVC_MGR_CHECK_SERVICE,
    SVC_MGR_ADD_SERVICE,
    SVC_MGR_LIST_SERVICES,
};
typedef int (*binder_handler)(struct binder_state *bs,
                              struct binder_transaction_data *txn,
                              struct binder_io *msg,
                              struct binder_io *reply);
struct binder_state *binder_open(const char* driver, size_t mapsize);
void binder_close(struct binder_state *bs);
/* initiate a blocking binder call
 * - returns zero on success
 */
int binder_call(struct binder_state *bs,
                struct binder_io *msg, struct binder_io *reply,
                uint32_t target, uint32_t code);
/* release any state associate with the binder_io
 * - call once any necessary data has been extracted from the
 *   binder_io after binder_call() returns
 * - can safely be called even if binder_call() fails
 */
void binder_done(struct binder_state *bs,
                 struct binder_io *msg, struct binder_io *reply);
/* manipulate strong references */
void binder_acquire(struct binder_state *bs, uint32_t target);
void binder_release(struct binder_state *bs, uint32_t target);
void binder_link_to_death(struct binder_state *bs, uint32_t target, struct binder_death *death);
void binder_loop(struct binder_state *bs, binder_handler func);
int binder_become_context_manager(struct binder_state *bs);
/* allocate a binder_io, providing a stack-allocated working
 * buffer, size of the working buffer, and how many object
 * offset entries to reserve from the buffer
 */
void bio_init(struct binder_io *bio, void *data,
           size_t maxdata, size_t maxobjects);
void bio_put_obj(struct binder_io *bio, void *ptr);
void bio_put_ref(struct binder_io *bio, uint32_t handle);
void bio_put_uint32(struct binder_io *bio, uint32_t n);
void bio_put_string16(struct binder_io *bio, const uint16_t *str);
void bio_put_string16_x(struct binder_io *bio, const char *_str);
uint32_t bio_get_uint32(struct binder_io *bio);
uint16_t *bio_get_string16(struct binder_io *bio, size_t *sz);
uint32_t bio_get_ref(struct binder_io *bio);
#endif
```

## bincer.c

```cpp
/* Copyright 2008 The Android Open Source Project
 */
#define LOG_TAG "Binder"
#include <errno.h>
#include <fcntl.h>
#include <inttypes.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>
#include <unistd.h>
#include <log/log.h>
#include "binder.h"
#define MAX_BIO_SIZE (1 << 30)
#define TRACE 0
void bio_init_from_txn(struct binder_io *io, struct binder_transaction_data *txn);
#if TRACE
void hexdump(void *_data, size_t len)
{
    unsigned char *data = _data;
    size_t count;
    for (count = 0; count < len; count++) {
        if ((count & 15) == 0)
            fprintf(stderr,"%04zu:", count);
        fprintf(stderr," %02x %c", *data,
                (*data < 32) || (*data > 126) ? '.' : *data);
        data++;
        if ((count & 15) == 15)
            fprintf(stderr,"\n");
    }
    if ((count & 15) != 0)
        fprintf(stderr,"\n");
}
void binder_dump_txn(struct binder_transaction_data *txn)
{
    struct flat_binder_object *obj;
    binder_size_t *offs = (binder_size_t *)(uintptr_t)txn->data.ptr.offsets;
    size_t count = txn->offsets_size / sizeof(binder_size_t);
    fprintf(stderr,"  target %016"PRIx64"  cookie %016"PRIx64"  code %08x  flags %08x\n",
            (uint64_t)txn->target.ptr, (uint64_t)txn->cookie, txn->code, txn->flags);
    fprintf(stderr,"  pid %8d  uid %8d  data %"PRIu64"  offs %"PRIu64"\n",
            txn->sender_pid, txn->sender_euid, (uint64_t)txn->data_size, (uint64_t)txn->offsets_size);
    hexdump((void *)(uintptr_t)txn->data.ptr.buffer, txn->data_size);
    while (count--) {
        obj = (struct flat_binder_object *) (((char*)(uintptr_t)txn->data.ptr.buffer) + *offs++);
        fprintf(stderr,"  - type %08x  flags %08x  ptr %016"PRIx64"  cookie %016"PRIx64"\n",
                obj->type, obj->flags, (uint64_t)obj->binder, (uint64_t)obj->cookie);
    }
}
#define NAME(n) case n: return #n
const char *cmd_name(uint32_t cmd)
{
    switch(cmd) {
        NAME(BR_NOOP);
        NAME(BR_TRANSACTION_COMPLETE);
        NAME(BR_INCREFS);
        NAME(BR_ACQUIRE);
        NAME(BR_RELEASE);
        NAME(BR_DECREFS);
        NAME(BR_TRANSACTION);
        NAME(BR_REPLY);
        NAME(BR_FAILED_REPLY);
        NAME(BR_DEAD_REPLY);
        NAME(BR_DEAD_BINDER);
    default: return "???";
    }
}
#else
#define hexdump(a,b) do{} while (0)
#define binder_dump_txn(txn)  do{} while (0)
#endif
#define BIO_F_SHARED    0x01  /* needs to be buffer freed */
#define BIO_F_OVERFLOW  0x02  /* ran out of space */
#define BIO_F_IOERROR   0x04
#define BIO_F_MALLOCED  0x08  /* needs to be free()'d */
struct binder_state
{
    int fd;
    void *mapped;
    size_t mapsize;
};
struct binder_state *binder_open(const char* driver, size_t mapsize)
{
    struct binder_state *bs;
    struct binder_version vers;
    bs = malloc(sizeof(*bs));
    if (!bs) {
        errno = ENOMEM;
        return NULL;
    }
    bs->fd = open(driver, O_RDWR | O_CLOEXEC);
    if (bs->fd < 0) {
        fprintf(stderr,"binder: cannot open %s (%s)\n",
                driver, strerror(errno));
        goto fail_open;
    }
    if ((ioctl(bs->fd, BINDER_VERSION, &vers) == -1) ||
        (vers.protocol_version != BINDER_CURRENT_PROTOCOL_VERSION)) {
        fprintf(stderr,
                "binder: kernel driver version (%d) differs from user space version (%d)\n",
                vers.protocol_version, BINDER_CURRENT_PROTOCOL_VERSION);
        goto fail_open;
    }
    bs->mapsize = mapsize;
    bs->mapped = mmap(NULL, mapsize, PROT_READ, MAP_PRIVATE, bs->fd, 0);
    if (bs->mapped == MAP_FAILED) {
        fprintf(stderr,"binder: cannot map device (%s)\n",
                strerror(errno));
        goto fail_map;
    }
    return bs;
fail_map:
    close(bs->fd);
fail_open:
    free(bs);
    return NULL;
}
void binder_close(struct binder_state *bs)
{
    munmap(bs->mapped, bs->mapsize);
    close(bs->fd);
    free(bs);
}
int binder_become_context_manager(struct binder_state *bs)
{
    return ioctl(bs->fd, BINDER_SET_CONTEXT_MGR, 0);
}
int binder_write(struct binder_state *bs, void *data, size_t len)
{
    struct binder_write_read bwr;
    int res;
    bwr.write_size = len;
    bwr.write_consumed = 0;
    bwr.write_buffer = (uintptr_t) data;
    bwr.read_size = 0;
    bwr.read_consumed = 0;
    bwr.read_buffer = 0;
    res = ioctl(bs->fd, BINDER_WRITE_READ, &bwr);
    if (res < 0) {
        fprintf(stderr,"binder_write: ioctl failed (%s)\n",
                strerror(errno));
    }
    return res;
}
void binder_free_buffer(struct binder_state *bs,
                        binder_uintptr_t buffer_to_free)
{
    struct {
        uint32_t cmd_free;
        binder_uintptr_t buffer;
    } __attribute__((packed)) data;
    data.cmd_free = BC_FREE_BUFFER;
    data.buffer = buffer_to_free;
    binder_write(bs, &data, sizeof(data));
}
void binder_send_reply(struct binder_state *bs,
                       struct binder_io *reply,
                       binder_uintptr_t buffer_to_free,
                       int status)
{
    struct {
        uint32_t cmd_free;
        binder_uintptr_t buffer;
        uint32_t cmd_reply;
        struct binder_transaction_data txn;
    } __attribute__((packed)) data;
    data.cmd_free = BC_FREE_BUFFER;
    data.buffer = buffer_to_free;
    data.cmd_reply = BC_REPLY;
    data.txn.target.ptr = 0;
    data.txn.cookie = 0;
    data.txn.code = 0;
    if (status) {
        data.txn.flags = TF_STATUS_CODE;
        data.txn.data_size = sizeof(int);
        data.txn.offsets_size = 0;
        data.txn.data.ptr.buffer = (uintptr_t)&status;
        data.txn.data.ptr.offsets = 0;
    } else {
        data.txn.flags = 0;
        data.txn.data_size = reply->data - reply->data0;
        data.txn.offsets_size = ((char*) reply->offs) - ((char*) reply->offs0);
        data.txn.data.ptr.buffer = (uintptr_t)reply->data0;
        data.txn.data.ptr.offsets = (uintptr_t)reply->offs0;
    }
    binder_write(bs, &data, sizeof(data));
}
int binder_parse(struct binder_state *bs, struct binder_io *bio,
                 uintptr_t ptr, size_t size, binder_handler func)
{
    int r = 1;
    uintptr_t end = ptr + (uintptr_t) size;
    while (ptr < end) {
        uint32_t cmd = *(uint32_t *) ptr;
        ptr += sizeof(uint32_t);
#if TRACE
        fprintf(stderr,"%s:\n", cmd_name(cmd));
#endif
        switch(cmd) {
        case BR_NOOP:
            break;
        case BR_TRANSACTION_COMPLETE:
            break;
        case BR_INCREFS:
        case BR_ACQUIRE:
        case BR_RELEASE:
        case BR_DECREFS:
#if TRACE
            fprintf(stderr,"  %p, %p\n", (void *)ptr, (void *)(ptr + sizeof(void *)));
#endif
            ptr += sizeof(struct binder_ptr_cookie);
            break;
        case BR_TRANSACTION: {
            struct binder_transaction_data *txn = (struct binder_transaction_data *) ptr;
            if ((end - ptr) < sizeof(*txn)) {
                ALOGE("parse: txn too small!\n");
                return -1;
            }
            binder_dump_txn(txn);
            if (func) {
                unsigned rdata[256/4];
                struct binder_io msg;
                struct binder_io reply;
                int res;
                bio_init(&reply, rdata, sizeof(rdata), 4);
                bio_init_from_txn(&msg, txn);
                res = func(bs, txn, &msg, &reply);
                if (txn->flags & TF_ONE_WAY) {
                    binder_free_buffer(bs, txn->data.ptr.buffer);
                } else {
                    binder_send_reply(bs, &reply, txn->data.ptr.buffer, res);
                }
            }
            ptr += sizeof(*txn);
            break;
        }
        case BR_REPLY: {
            struct binder_transaction_data *txn = (struct binder_transaction_data *) ptr;
            if ((end - ptr) < sizeof(*txn)) {
                ALOGE("parse: reply too small!\n");
                return -1;
            }
            binder_dump_txn(txn);
            if (bio) {
                bio_init_from_txn(bio, txn);
                bio = 0;
            } else {
                /* todo FREE BUFFER */
            }
            ptr += sizeof(*txn);
            r = 0;
            break;
        }
        case BR_DEAD_BINDER: {
            struct binder_death *death = (struct binder_death *)(uintptr_t) *(binder_uintptr_t *)ptr;
            ptr += sizeof(binder_uintptr_t);
            death->func(bs, death->ptr);
            break;
        }
        case BR_FAILED_REPLY:
            r = -1;
            break;
        case BR_DEAD_REPLY:
            r = -1;
            break;
        default:
            ALOGE("parse: OOPS %d\n", cmd);
            return -1;
        }
    }
    return r;
}
void binder_acquire(struct binder_state *bs, uint32_t target)
{
    uint32_t cmd[2];
    cmd[0] = BC_ACQUIRE;
    cmd[1] = target;
    binder_write(bs, cmd, sizeof(cmd));
}
void binder_release(struct binder_state *bs, uint32_t target)
{
    uint32_t cmd[2];
    cmd[0] = BC_RELEASE;
    cmd[1] = target;
    binder_write(bs, cmd, sizeof(cmd));
}
void binder_link_to_death(struct binder_state *bs, uint32_t target, struct binder_death *death)
{
    struct {
        uint32_t cmd;
        struct binder_handle_cookie payload;
    } __attribute__((packed)) data;
    data.cmd = BC_REQUEST_DEATH_NOTIFICATION;
    data.payload.handle = target;
    data.payload.cookie = (uintptr_t) death;
    binder_write(bs, &data, sizeof(data));
}
int binder_call(struct binder_state *bs,
                struct binder_io *msg, struct binder_io *reply,
                uint32_t target, uint32_t code)
{
    int res;
    struct binder_write_read bwr;
    struct {
        uint32_t cmd;
        struct binder_transaction_data txn;
    } __attribute__((packed)) writebuf;
    unsigned readbuf[32];
    if (msg->flags & BIO_F_OVERFLOW) {
        fprintf(stderr,"binder: txn buffer overflow\n");
        goto fail;
    }
    writebuf.cmd = BC_TRANSACTION;
    writebuf.txn.target.handle = target;
    writebuf.txn.code = code;
    writebuf.txn.flags = 0;
    writebuf.txn.data_size = msg->data - msg->data0;
    writebuf.txn.offsets_size = ((char*) msg->offs) - ((char*) msg->offs0);
    writebuf.txn.data.ptr.buffer = (uintptr_t)msg->data0;
    writebuf.txn.data.ptr.offsets = (uintptr_t)msg->offs0;
    bwr.write_size = sizeof(writebuf);
    bwr.write_consumed = 0;
    bwr.write_buffer = (uintptr_t) &writebuf;
    hexdump(msg->data0, msg->data - msg->data0);
    for (;;) {
        bwr.read_size = sizeof(readbuf);
        bwr.read_consumed = 0;
        bwr.read_buffer = (uintptr_t) readbuf;
        res = ioctl(bs->fd, BINDER_WRITE_READ, &bwr);
        if (res < 0) {
            fprintf(stderr,"binder: ioctl failed (%s)\n", strerror(errno));
            goto fail;
        }
        res = binder_parse(bs, reply, (uintptr_t) readbuf, bwr.read_consumed, 0);
        if (res == 0) return 0;
        if (res < 0) goto fail;
    }
fail:
    memset(reply, 0, sizeof(*reply));
    reply->flags |= BIO_F_IOERROR;
    return -1;
}
void binder_loop(struct binder_state *bs, binder_handler func)
{
    int res;
    struct binder_write_read bwr;
    uint32_t readbuf[32];
    bwr.write_size = 0;
    bwr.write_consumed = 0;
    bwr.write_buffer = 0;
    readbuf[0] = BC_ENTER_LOOPER;
    binder_write(bs, readbuf, sizeof(uint32_t));
    for (;;) {
        bwr.read_size = sizeof(readbuf);
        bwr.read_consumed = 0;
        bwr.read_buffer = (uintptr_t) readbuf;
        res = ioctl(bs->fd, BINDER_WRITE_READ, &bwr);
        if (res < 0) {
            ALOGE("binder_loop: ioctl failed (%s)\n", strerror(errno));
            break;
        }
        res = binder_parse(bs, 0, (uintptr_t) readbuf, bwr.read_consumed, func);
        if (res == 0) {
            ALOGE("binder_loop: unexpected reply?!\n");
            break;
        }
        if (res < 0) {
            ALOGE("binder_loop: io error %d %s\n", res, strerror(errno));
            break;
        }
    }
}
void bio_init_from_txn(struct binder_io *bio, struct binder_transaction_data *txn)
{
    bio->data = bio->data0 = (char *)(intptr_t)txn->data.ptr.buffer;
    bio->offs = bio->offs0 = (binder_size_t *)(intptr_t)txn->data.ptr.offsets;
    bio->data_avail = txn->data_size;
    bio->offs_avail = txn->offsets_size / sizeof(size_t);
    bio->flags = BIO_F_SHARED;
}
void bio_init(struct binder_io *bio, void *data,
              size_t maxdata, size_t maxoffs)
{
    size_t n = maxoffs * sizeof(size_t);
    if (n > maxdata) {
        bio->flags = BIO_F_OVERFLOW;
        bio->data_avail = 0;
        bio->offs_avail = 0;
        return;
    }
    bio->data = bio->data0 = (char *) data + n;
    bio->offs = bio->offs0 = data;
    bio->data_avail = maxdata - n;
    bio->offs_avail = maxoffs;
    bio->flags = 0;
}
static void *bio_alloc(struct binder_io *bio, size_t size)
{
    size = (size + 3) & (~3);
    if (size > bio->data_avail) {
        bio->flags |= BIO_F_OVERFLOW;
        return NULL;
    } else {
        void *ptr = bio->data;
        bio->data += size;
        bio->data_avail -= size;
        return ptr;
    }
}
void binder_done(struct binder_state *bs,
                 __unused struct binder_io *msg,
                 struct binder_io *reply)
{
    struct {
        uint32_t cmd;
        uintptr_t buffer;
    } __attribute__((packed)) data;
    if (reply->flags & BIO_F_SHARED) {
        data.cmd = BC_FREE_BUFFER;
        data.buffer = (uintptr_t) reply->data0;
        binder_write(bs, &data, sizeof(data));
        reply->flags = 0;
    }
}
static struct flat_binder_object *bio_alloc_obj(struct binder_io *bio)
{
    struct flat_binder_object *obj;
    obj = bio_alloc(bio, sizeof(*obj));
    if (obj && bio->offs_avail) {
        bio->offs_avail--;
        *bio->offs++ = ((char*) obj) - ((char*) bio->data0);
        return obj;
    }
    bio->flags |= BIO_F_OVERFLOW;
    return NULL;
}
void bio_put_uint32(struct binder_io *bio, uint32_t n)
{
    uint32_t *ptr = bio_alloc(bio, sizeof(n));
    if (ptr)
        *ptr = n;
}
void bio_put_obj(struct binder_io *bio, void *ptr)
{
    struct flat_binder_object *obj;
    obj = bio_alloc_obj(bio);
    if (!obj)
        return;
    obj->flags = 0x7f | FLAT_BINDER_FLAG_ACCEPTS_FDS;
    obj->type = BINDER_TYPE_BINDER;
    obj->binder = (uintptr_t)ptr;
    obj->cookie = 0;
}
void bio_put_ref(struct binder_io *bio, uint32_t handle)
{
    struct flat_binder_object *obj;
    if (handle)
        obj = bio_alloc_obj(bio);
    else
        obj = bio_alloc(bio, sizeof(*obj));
    if (!obj)
        return;
    obj->flags = 0x7f | FLAT_BINDER_FLAG_ACCEPTS_FDS;
    obj->type = BINDER_TYPE_HANDLE;
    obj->handle = handle;
    obj->cookie = 0;
}
void bio_put_string16(struct binder_io *bio, const uint16_t *str)
{
    size_t len;
    uint16_t *ptr;
    if (!str) {
        bio_put_uint32(bio, 0xffffffff);
        return;
    }
    len = 0;
    while (str[len]) len++;
    if (len >= (MAX_BIO_SIZE / sizeof(uint16_t))) {
        bio_put_uint32(bio, 0xffffffff);
        return;
    }
    /* Note: The payload will carry 32bit size instead of size_t */
    bio_put_uint32(bio, (uint32_t) len);
    len = (len + 1) * sizeof(uint16_t);
    ptr = bio_alloc(bio, len);
    if (ptr)
        memcpy(ptr, str, len);
}
void bio_put_string16_x(struct binder_io *bio, const char *_str)
{
    unsigned char *str = (unsigned char*) _str;
    size_t len;
    uint16_t *ptr;
    if (!str) {
        bio_put_uint32(bio, 0xffffffff);
        return;
    }
    len = strlen(_str);
    if (len >= (MAX_BIO_SIZE / sizeof(uint16_t))) {
        bio_put_uint32(bio, 0xffffffff);
        return;
    }
    /* Note: The payload will carry 32bit size instead of size_t */
    bio_put_uint32(bio, len);
    ptr = bio_alloc(bio, (len + 1) * sizeof(uint16_t));
    if (!ptr)
        return;
    while (*str)
        *ptr++ = *str++;
    *ptr++ = 0;
}
static void *bio_get(struct binder_io *bio, size_t size)
{
    size = (size + 3) & (~3);
    if (bio->data_avail < size){
        bio->data_avail = 0;
        bio->flags |= BIO_F_OVERFLOW;
        return NULL;
    }  else {
        void *ptr = bio->data;
        bio->data += size;
        bio->data_avail -= size;
        return ptr;
    }
}
uint32_t bio_get_uint32(struct binder_io *bio)
{
    uint32_t *ptr = bio_get(bio, sizeof(*ptr));
    return ptr ? *ptr : 0;
}
uint16_t *bio_get_string16(struct binder_io *bio, size_t *sz)
{
    size_t len;
    /* Note: The payload will carry 32bit size instead of size_t */
    len = (size_t) bio_get_uint32(bio);
    if (sz)
        *sz = len;
    return bio_get(bio, (len + 1) * sizeof(uint16_t));
}
static struct flat_binder_object *_bio_get_obj(struct binder_io *bio)
{
    size_t n;
    size_t off = bio->data - bio->data0;
    /* TODO: be smarter about this? */
    for (n = 0; n < bio->offs_avail; n++) {
        if (bio->offs[n] == off)
            return bio_get(bio, sizeof(struct flat_binder_object));
    }
    bio->data_avail = 0;
    bio->flags |= BIO_F_OVERFLOW;
    return NULL;
}
uint32_t bio_get_ref(struct binder_io *bio)
{
    struct flat_binder_object *obj;
    obj = _bio_get_obj(bio);
    if (!obj)
        return 0;
    if (obj->type == BINDER_TYPE_HANDLE)
        return obj->handle;
    return 0;
}
```

## 참조

- [<https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/os/IBinder.java]>
- [<https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/os/Binder.java]>
- [<https://android.googlesource.com/platform/frameworks/native/+/refs/heads/oreo-release/cmds/servicemanager/]>