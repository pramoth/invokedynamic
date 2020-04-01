package th.co.geniustree.indy;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.objectweb.asm.Opcodes.*;

public class MethodHandleTest3 {
    final static String JAVA_LANG_OBJECT_CLASSNAME = "java/lang/Object";
    final static String CLASS_NAME = "th/co/geniustree/indy/InvokeDynamic";

    /**
     * ใช้รันเพื่อสร้าง InvokeDynamic.class  **ไม่ต้องสนใจ**
     **/
    public static void main(String[] args) throws IOException {
        final ClassWriter cw = new ClassWriter(0);
        // สร้างคลาส public InvokeDynamic exntend Object
        cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, CLASS_NAME, null, JAVA_LANG_OBJECT_CLASSNAME, null);

        //สร้าง constructor ที่จะต้องมีทุกคลาสอยู่อยู่ ไม่ต้องสนใจตรงนี้
        createDefaultConstructor(cw);

        // สร้าง  public static void main(String[] args) ซึ่ง invokedynamic จะอยู่ในนี้ **สำคัญ**
        create_InvokeDynamic_Main_Method(cw);
        cw.visitEnd();
        // เขียน bytecode ลงที่ target/classes/th/co/geniustree/indy/InvokeDynamic.class
        writeBytecodeToFile(cw);
    }

    /**
     * สร้าง เขียนลงไฟล์  **ไม่ต้องสนใจ**
     **/
    private static void writeBytecodeToFile(ClassWriter cw) throws IOException {
        String targetPath = "target/classes/"+CLASS_NAME+".class";
        try (FileOutputStream fos = new FileOutputStream(new File(targetPath))) {
            fos.write(cw.toByteArray());
        }

    }

    /**
     * สร้าง default constructor **ไม่ต้องสนใจ**
     **/
    private static void createDefaultConstructor(ClassWriter cw) {
        MethodVisitor mv;// Create a standard void constructor
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    /** bytecode ที่เดกี่ยวกับ invokedynamic อยู่ในนี้ **/
    private static void create_InvokeDynamic_Main_Method(ClassWriter cw) {
        MethodVisitor mv;
        // สร้าง  public static void main(String[]) สำหรับรันทดสอบ
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        mv.visitCode();

        // ตัวแปร bootstrapMethodType เป็น method descripter ของ bootstrap method ที่เราต้องการเรียก
        // มีพารามิเตอร์ (MethodHandles.Lookup,String,MethodType) และรีเทิร์น CallSite
        String bootstrapMethodType = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;";

        // ตัวแปร bootstrap คือ api  ของ ASM ที่จะถูกแปลงไปเป็น BootstrapMethods attribute ที่ index 0 เพราะมีตัวเดียวในคลาสนี้
        // ซึ่ง invokedynamic จะไปเรียก static method ที่ชื่อ MethodHandleTest2.boostrapMethod() ที่เราเขียนไว้ใน part 2 อีกที
        Handle bootstrap = new Handle(Opcodes.H_INVOKESTATIC, "th/co/geniustree/indy/MethodHandleTest2", "boostrapMethod", bootstrapMethodType, false);

        // ก่อนที่จะเรียก MethodHandleTest2.target(String) จะต้อง push ค่า "World" ลงไปใน operand stakc ก่อน
        mv.visitLdcInsn("World");

        // สร้าง bytecode เพื่อเรียก method MethodHandleTest2.target()
        mv.visitInvokeDynamicInsn("target", "(Ljava/lang/String;)V", bootstrap);

        //return void
        mv.visitInsn(RETURN);

        // stack size 1 เนื่องจาก ก่อนเรียก MethodHandleTest2.target(String) จะต้อง push ค่าของตัวแปร message ลงใน operand stack ก่อนเรียกใช้งาน
        // local variable 1 คือ String[] args และ String message
        mv.visitMaxs(1, 2);
        mv.visitEnd();
    }
}
