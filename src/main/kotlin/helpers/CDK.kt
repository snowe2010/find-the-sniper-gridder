package helpers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.awscdk.core.App
import software.amazon.awscdk.core.Construct
import software.amazon.awscdk.core.Duration
import software.amazon.awscdk.services.lambda.Code
import software.amazon.awscdk.services.lambda.FunctionProps
import software.amazon.awscdk.services.lambda.Function
import software.amazon.awscdk.services.lambda.Runtime.PROVIDED


const val appName = "find-the-sniper-helper"

class FindTheSniperApp {
    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val app = App()
            FindTheSniperCdk(app)
            app.synth()
        }
    }
}

class FindTheSniperCdk(
    scope: Construct,
) : software.amazon.awscdk.core.Stack(scope, appName, null) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    init {
        buildFunction()
    }

    private fun buildFunction(): Function {
        val functionName = "$stackName-gridImage"
        val function = function(functionName, functionProps {
            this.runtime(PROVIDED)
            this.handler("not.used.by.quarkus.in.native.mode")
            this.code(Code.fromAsset("target/function.zip"))
//            this.tracing(Tracing.ACTIVE)
            this.timeout(Duration.seconds(30))
            this.memorySize(256)
            this.functionName(functionName)
            environment(
                mapOf(
                    "DISABLE_SIGNAL_HANDLERS" to "true",            // required by graal native
                    "QUARKUS_LAMBDA_HANDLER" to "handler"  // https://quarkus.io/guides/amazon-lambda#choose
                )
            )
        })
        return function
    }
}



/**
 * @see Function
 */
fun Construct.function(
    id: String,
    props: FunctionProps,
    init: (Function.() -> Unit)? = null
): Function {
    val obj = Function(this, id, props)
    init?.invoke(obj)
    return obj
}


/**
 * @see FunctionProps.Builder
 */
fun functionProps(init: FunctionProps.Builder.() -> Unit): FunctionProps {
    val builder = FunctionProps.Builder()
    builder.init()
    return builder.build()
}
