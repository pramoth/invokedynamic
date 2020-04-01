package th.co.geniustree.indy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MethodHandleTest {

    public static void main(String[] args) throws Throwable {
        //Lookup เป็น Factory สำหรับเอาไว้สร้าง MethodHandle
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        // เราจะอธิบายว่าเราต้องการ method ที่มี return type เป็นอะไร และ param เป็นอะไร
        //ในที่นี้เราจะเรียก String.concat(String)
        //กรณีที่ compiler เป็นคนจัดการให้ ตรงนีจะเป็น CONSTANT_MethodType
        //CONSTANT_MethodType => (Ljava/lang/String;)Ljava/lang/String;
        //โดย L className ; เป็น field descriptor L หมายถึง reference type ใดๆ ส่วน className จะแทน . ด้วย /
        MethodType returnStringAndStringParamMethosType = MethodType.methodType(String.class, String.class);


        // สร้าง MethodHandle เพื่อห่อหุ่มเอา invokevirtual  java/lang/String.concat:(Ljava/lang/String;)Ljava/lang/String; เอาไว้สำหรับ invoke
        //ขั้นตอนนี้จะทำการ access checking ด้วยว่ามีสิทธิเข้าถึงไหม ซึ่งจะต่างจาก Reflection ตรงที่ reflection จะ check ทุกครั้งที่ invoke method ดังนั้นตรงนี้ MethodHandle จะเร็วกว่า
        // เพราะโดยปกติแล้ว lookup.findXxx จะทำครั้งเดียวแล้วเก็บ method handle instance เอาไว้ invoke()
        MethodHandle concatMethodHandle = lookup.findVirtual(String.class, "concat",returnStringAndStringParamMethosType);

        //ถ้าเป็น bytecode ก็จะเทียบเท่ากับ
        //ldc "Hello"  --> push "Hello" reference to operand stack
        //ldc "World"  --> push "World" reference to operand stack
        //invokevirtual  java/lang/String.concat:(Ljava/lang/String;)Ljava/lang/String;
        final String result = (String) concatMethodHandle.invokeExact("Hello", "World");
        System.out.println(result); // print "HelloWorld"
    }
}
