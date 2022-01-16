package com.tylerthrailkill.sniper.helpers

import software.amazon.awscdk.*
import software.amazon.awscdk.services.dynamodb.Attribute
import software.amazon.awscdk.services.dynamodb.AttributeType
import software.amazon.awscdk.services.dynamodb.BillingMode
import software.amazon.awscdk.services.dynamodb.Table
import software.amazon.awscdk.services.events.CronOptions
import software.amazon.awscdk.services.events.Rule
import software.amazon.awscdk.services.events.Schedule
import software.amazon.awscdk.services.events.targets.LambdaFunction
import software.amazon.awscdk.services.lambda.*
import software.amazon.awscdk.services.lambda.Function
import software.amazon.awscdk.services.lambda.Runtime.PROVIDED
import software.amazon.awscdk.services.secretsmanager.ISecret
import software.amazon.awscdk.services.secretsmanager.Secret
import software.amazon.awscdk.services.secretsmanager.SecretProps
import software.constructs.Construct

const val appName = "find-the-sniper-helper"
val awsAccountId = System.getenv("AWS_ACCOUNT_ID") ?: "000000000000" // default to localstack

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
) : Stack(
    scope,
    appName,
    StackProps.builder().env(
        Environment.builder().account(awsAccountId).build()
    ).build()
) {
    init {
        val function = createFunction()
        val secret = createSecret()
        val table = createDynamoDbTable()
        val event = createCronEvent(function)

        secret.grantRead(function.role?.grantPrincipal!!)
        table.grantFullAccess(function.role?.grantPrincipal!!)
    }

    private fun createCronEvent(function: Function): Rule {
        return Rule.Builder.create(this, "findthesniper")
            .description("check reddit every 2 minutes")
            .schedule(Schedule.cron(CronOptions.builder().minute("*/3").build()))
            .build().also { it.addTarget(LambdaFunction(function)) }
    }

    private fun createFunction(): Function {
        val functionName = "$stackName-gridImage"

        return function(functionName, functionProps {
            runtime(PROVIDED)
            handler("not.used.by.quarkus.in.native.mode")
            code(Code.fromAsset("build/function.zip"))
            timeout(Duration.minutes(3))
            memorySize(1024)
            functionName(functionName)
            environment(
                mapOf(
                    "DISABLE_SIGNAL_HANDLERS" to "true",  // required for quarkus on graal native
                )
            )
        })
    }

    private fun createSecret(): ISecret {
        return Secret(this, "findthesniper-secrets", 
            SecretProps.builder().secretName("findthesniper-secrets").build()
        )
    }

    private fun createDynamoDbTable(): Table {
        val tableName = "RedditParsedPosts"
        return Table.Builder
            .create(this, tableName)
            .removalPolicy(RemovalPolicy.RETAIN)
            .tableName("${appName}-$tableName")
            .billingMode(BillingMode.PAY_PER_REQUEST)
            .partitionKey(Attribute.builder().name("id").type(AttributeType.STRING).build())
            .timeToLiveAttribute("ttl")
            .build()
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
 * @see SingletonFunction
 */
fun Construct.singletonFunction(
    id: String,
    props: SingletonFunctionProps,
    init: (SingletonFunction.() -> Unit)? = null
): SingletonFunction {
    val obj = SingletonFunction(this, id, props)
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

/**
 * @see FunctionProps.Builder
 */
fun singletonFunctionProps(init: SingletonFunctionProps.Builder.() -> Unit): SingletonFunctionProps {
    val builder = SingletonFunctionProps.Builder()
    builder.init()
    return builder.build()
}
