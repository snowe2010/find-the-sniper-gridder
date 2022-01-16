package com.tylerthrailkill.sniper.helpers

import software.amazon.awscdk.core.*
import software.amazon.awscdk.services.dynamodb.Attribute
import software.amazon.awscdk.services.dynamodb.AttributeType
import software.amazon.awscdk.services.dynamodb.BillingMode
import software.amazon.awscdk.services.dynamodb.Table
import software.amazon.awscdk.services.lambda.Code
import software.amazon.awscdk.services.lambda.Function
import software.amazon.awscdk.services.lambda.FunctionProps
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
) : Stack(
    scope,
    appName,
    StackProps.builder().env(
        Environment.builder().account(System.getenv("AWS_ACCOUNT_ID") ?: "000000000000").build()
    ).build()
) {
    init {
        buildFunction()
    }

    private fun buildFunction(): Function {
        val functionName = "$stackName-gridImage"
        val function = function(functionName, functionProps {
            this.runtime(PROVIDED)
            this.handler("not.used.by.quarkus.in.native.mode")
            this.code(Code.fromAsset("build/function.zip"))
            this.timeout(Duration.seconds(30))
            this.memorySize(256)
            this.functionName(functionName)
            environment(
                mapOf(
                    "DISABLE_SIGNAL_HANDLERS" to "true",            // required by graal native
//                    "QUARKUS_LAMBDA_HANDLER" to "handler"  // https://quarkus.io/guides/amazon-lambda#choose
                )
            )
        })
        val awsAccountId = System.getenv("AWS_ACCOUNT_ID") ?: "000000000000" // default to localstack
        val secret = software.amazon.awscdk.services.secretsmanager.Secret.fromSecretCompleteArn(
            this, " findthesniper-secrets",
            "arn:aws:secretsmanager:us-west-1:$awsAccountId:secret:findthesniper-secrets-VHHBCq"
        )

        secret.grantRead(function.role?.grantPrincipal!!)
        val table = createDynamoDbTable()
        table.grantFullAccess(function.role?.grantPrincipal!!)

        return function
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
 * @see FunctionProps.Builder
 */
fun functionProps(init: FunctionProps.Builder.() -> Unit): FunctionProps {
    val builder = FunctionProps.Builder()
    builder.init()
    return builder.build()
}
