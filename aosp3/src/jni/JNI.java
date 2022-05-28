package jni;

public class JNI {

    static {
        System.loadLibrary("JNI");
    }

    private native int getNumber();
    private native void printHelloWorld();

    public static void main(String[] args) {
        JNI jni = new JNI();

        jni.printHelloWorld();

        System.out.println(jni.getNumber());
    }
}
