import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author chenjimou
 * @description gradle插件实体
 * @date 2023/8/29 18:21
 */
class PluginConfig : Plugin<Project> {
    override fun apply(project: Project) {
        if (project.plugins.hasPlugin("com.android.application")) {
            println("> ==================== Treasure Plugin Load Success ====================")
            project.extensions.getByType(AndroidComponentsExtension::class.java)
                .onVariants { variant ->
                    variant.instrumentation.transformClassesWith(
                        ClassVisitorFactory::class.java, InstrumentationScope.ALL
                    ) {}
                    variant.instrumentation.setAsmFramesComputationMode(
                        FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
                    )
                }
        } else {
            println("> treasure-plugin: only work on Android Application!")
        }
    }
}