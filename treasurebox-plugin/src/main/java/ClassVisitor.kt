import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

internal class ClassVisitor(nextClassVisitor: ClassVisitor) :
    ClassVisitor(Opcodes.ASM9, nextClassVisitor) {
    private lateinit var className: String

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        className = name
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<String>?
    ): MethodVisitor {
        val visitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        return object : AdviceAdapter(ASM9, visitor, access, name, descriptor) {
            val methodName = "${className.replace('/', '.')}.$name"

            override fun onMethodEnter() {
                visitLdcInsn(methodName)
                visitMethodInsn(
                    INVOKESTATIC,
                    "treasurebox/monitor/StackCache",
                    "methodEnter",
                    "(Ljava/lang/String;)V",
                    false
                )
            }

            override fun onMethodExit(opcode: Int) {
                visitLdcInsn(methodName)
                visitMethodInsn(
                    INVOKESTATIC,
                    "treasurebox/monitor/StackCache",
                    "methodExit",
                    "(Ljava/lang/String;)V",
                    false
                )
            }
        }
    }
}