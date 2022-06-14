package com.tylerthrailkill.sniper.aws

import com.tylerthrailkill.sniper.helpers.appName
import com.tylerthrailkill.sniper.reddit.Child
import mu.KotlinLogging
import software.amazon.awssdk.http.async.SdkAsyncHttpClient
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Produces
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration


private val logger = KotlinLogging.logger {}


class AwsServiceClientProducer {
    @ApplicationScoped
    @Produces
    fun nettyClient(): SdkAsyncHttpClient = NettyNioAsyncHttpClient.builder()
        .connectionMaxIdleTime(5.seconds.toJavaDuration()) // https://github.com/aws/aws-sdk-java-v2/issues/1122
        .build()
}

@ApplicationScoped
class DynamoDbService(
    val dynamoDb: DynamoDbClient,
) {
    val tableName = "$appName-RedditParsedPosts"

    fun save(data: Child) {
        logger.info("Writing to $tableName")
        val itemMap = mutableMapOf<String, AttributeValue>(
            "id" to AttributeValue.builder().s(data.data.name).build(),
        )
        setTTL(itemMap, 30.days)

        logger.info("Persist uuid: ${data.data.name}")
        dynamoDb.putItem(
            PutItemRequest.builder().tableName(tableName).item(itemMap).build()
        ).also {
            logger.info("Response is ${it.attributes()}")
        }

//        val table = dynamoDbEnhancedClient.table(tableName, TableSchema.fromClass(Child::class.java))
//        table.putItem(data)
    }

    protected fun scanRequest(): ScanRequest? {
        return ScanRequest.builder().tableName(tableName)
            .attributesToGet("id").build()
    }

    fun getAll(): List<String> {
        // TODO can't use enhanced db yet with graalvm or quarkus
//        val table = dynamoDb.table(tableName, TableSchema.fromClass(Child::class.java))
//        val scan = table.scan()
//        return scan.items().toList()
        val scan = dynamoDb.scan(scanRequest())
        val items = scan.items()
        return items.mapNotNull { it["id"]?.s() }
    }

    private fun setTTL(itemMap: MutableMap<String, AttributeValue>, numberOfDays: Duration) {
        val ttlDate = numberOfDays.inWholeSeconds + System.currentTimeMillis() / 1000
        logger.info("TTL : $ttlDate")
        itemMap["ttl"] = AttributeValue.builder().n(ttlDate.toString()).build()
    }
}
