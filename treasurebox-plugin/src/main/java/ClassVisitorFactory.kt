import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import org.objectweb.asm.ClassVisitor

abstract class ClassVisitorFactory : AsmClassVisitorFactory<InstrumentationParameters.None> {
    override fun createClassVisitor(
        classContext: ClassContext, nextClassVisitor: ClassVisitor
    ): ClassVisitor = ClassVisitor(nextClassVisitor)

    override fun isInstrumentable(classData: ClassData): Boolean {
        println("> treasure-plugin: currentClass = ${classData.className}, isTargetClass = ${isTargetClass(classData.className)}")
        return isTargetClass(classData.className)
    }

    private fun isTargetClass(className: String): Boolean {
        for (clazzName in Constants.PACKAGE_WHITE_LIST) {
            if (className.startsWith(clazzName)) {
                return false
            }
        }
        for (clazzName in Constants.CLASS_WHITE_LIST) {
            if (className.startsWith(clazzName)) {
                return false
            }
        }
        return true
    }
}