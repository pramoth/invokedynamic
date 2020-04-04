package th.co.geniustree.indy;

import java.lang.invoke.*;

public class LambdaTest {

    public void test() {
        // 1. Not refer to outer scope
        // javac will create a synthetic static  method  name "lambda$test$0()"
        Runnable a = () -> System.out.println("Hello From Lambda. no capture variable");

        // 2. Refer/capture value from outer scope
        // javac will create a synthetic static method name "lambda$test$1(java.lang.String)V"
        String name = "Pramoth";
        Runnable b = () -> System.out.println("Hello From Lambda " + name + " with capture variable");
    }


    public static void main(String[] args) throws Throwable {
        // จำลอง invokedynamic ไปที่ bootstrap `lambda$test$0`
        Runnable r = mimic_call_lambda$test$0();
        // print "Hello From Lambda. no capture variable"
        r.run();

        // จำลอง invokedynamic ไปที่ bootstrap `lambda$test$1`
        r = mimic_call_lambda$test$1("Pramoth");
        // print "Hello From Lambda Pramoth with capture variable"
        r.run();

    }

    private static Runnable mimic_call_lambda$test$0() throws Throwable {
        // LambdaMetafactory จะทำการ implement Runnable.run ด้วยการทำ bytecode manipulation โดย ASM ดูการทำงานใน  java.lang.invoke.InnerClassLambdaMetafactory
        CallSite lambda$test$CallSite = LambdaMetafactory.metafactory(MethodHandles.lookup(),
                "run", // method ที่จะ  implement คือ Runnable.run()
                MethodType.methodType(Runnable.class), // method type ของ factory method จะรีเทริน Runnable อารมณ์คล้ายๆ Runnable Factory.create()
                MethodType.methodType(void.class), // อิมพลีเมนต์เมธอดจะรีเทร์น void `public void run()`
                MethodHandles.lookup().findStatic(LambdaTest.class, "lambda$test$0", MethodType.methodType(void.class)), // MethodHandle ที่เอาไว้เรียกเมธอดที่ javac สร้างมาเป็น body ของ lambda
                MethodType.methodType(void.class)); // อิมพลีเมนต์เมธอดจะรีเทร์น void `public void run()` อันนี้ที่เพราะสามารถรีเทริน more specific class ได้ กรณีนี้ ไม่ได้ใช้ก็ระบุ void.class เหมือนรีเทรินของอิมพลีเมนต์เมธอด
        // invoke โดยไม่มี capture vairable
        return (Runnable) lambda$test$CallSite.dynamicInvoker().invokeExact();
    }

    private static Runnable mimic_call_lambda$test$1(String captureVariable) throws Throwable {
        CallSite lambda$test$CallSite = LambdaMetafactory.metafactory(MethodHandles.lookup(),
                "run",
                MethodType.methodType(Runnable.class, String.class), // method type ของ factory method จะรีเทริน Runnable แต่ว่าเคสนี้มีการ capture variable ด้วย ดังนั้นจึงต้องระบุ parameter ด้วย อารมณ์คล้ายๆ Runnable Factory.create(String)
                MethodType.methodType(void.class),
                MethodHandles.lookup().findStatic(LambdaTest.class, "lambda$test$1", MethodType.methodType(void.class, String.class)),
                MethodType.methodType(void.class));
        // invoke พร้อมกับ capture vairable
        return (Runnable) lambda$test$CallSite.dynamicInvoker().invokeExact(captureVariable);
    }
}
