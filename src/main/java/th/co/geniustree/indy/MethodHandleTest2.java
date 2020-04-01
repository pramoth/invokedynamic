package th.co.geniustree.indy;

import java.lang.invoke.*;

public class MethodHandleTest2 {
    public static void main(String[] args) throws Throwable {
        // บรรทัดถัดไปคือ bytecode ของ invokedynamic นะครับ #1 #2 #3  จะถูกเก็บไว้ใน constant pool
        // invokedynamic #1:[#2:#3]

        // step 1 preapre อันนี้ทำโดย JVM
        final MethodHandles.Lookup callerLookup = MethodHandles.lookup();
        // #2
        String targetMethodName = "target";
        // #3
        MethodType methodType = MethodType.methodType(void.class, String.class);


        // step 2 link
        CallSite callSite = boostrapMethod(callerLookup, targetMethodName, methodType);


        // step 3 invoke
        // ในเคสของ ConstantCallSite dynamicInvoker() จะ delegate ไป getTarget()
        callSite.dynamicInvoker().invokeWithArguments("world"); // print "Hello world"

    }

    // #1
    public static CallSite boostrapMethod(MethodHandles.Lookup callerContextLookup, String name, MethodType methodType) throws NoSuchMethodException, IllegalAccessException {
        final MethodHandle methodHandle = callerContextLookup.findStatic(MethodHandleTest2.class, name, methodType);
        CallSite callSite = new ConstantCallSite(methodHandle);
        return callSite;
    }

    //target method  ของเรา มี type description (Ljava/lang/String;)V
    public static void target(String msg) {
        System.out.println("Hello " + msg);
    }
}
