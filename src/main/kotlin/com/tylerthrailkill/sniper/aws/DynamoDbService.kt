package com.tylerthrailkill.sniper.aws

import com.amazonaws.http.apache.client.impl.SdkHttpClient
import com.tylerthrailkill.sniper.reddit.Child
import io.quarkus.arc.AlternativePriority
import mu.KotlinLogging
import org.eclipse.microprofile.config.inject.ConfigProperty
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.http.async.SdkAsyncHttpClient
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import java.time.Instant
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Produces
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private val logger = KotlinLogging.logger {}


class AwsServiceClientProducer {

    @ApplicationScoped
    @Produces
    fun nettyClient(): SdkAsyncHttpClient = NettyNioAsyncHttpClient.builder()
        .connectionMaxIdleTime(5.seconds.toJavaDuration()) // https://github.com/aws/aws-sdk-java-v2/issues/1122
        .build()


    @AlternativePriority(0)
    @ApplicationScoped
    @Produces
    fun dynamoDbAsyncClient(
        @ConfigProperty(name = "aws.region", defaultValue = "us-west-1") awsRegion: String,
        sdkAsyncHttpClient: SdkAsyncHttpClient
    ): DynamoDbAsyncClient {
        return DynamoDbAsyncClient.builder()
            .region(Region.of(awsRegion))
            .httpClient(sdkAsyncHttpClient)
            .build()
    }
}

@ApplicationScoped
class DynamoDbService(private var dynamoDbAsyncClient: DynamoDbAsyncClient) {

    private fun save(data: Child): Child {
        logger.info("Writing to RedditParsedPosts")
        val itemMap = mutableMapOf<String, AttributeValue>(
            "id" to AttributeValue.builder().s(data.data.name).build(),
        )
        setTTL(itemMap, 30.seconds)

        logger.info("Persist uuid: ${data.data.name}")
        dynamoDbAsyncClient.putItem(
            PutItemRequest.builder().tableName("RedditParsedPosts").item(itemMap).build()
        ).also {
            logger.info("Response is ${it.get().attributes()}")
            return data
        }
    }

    private fun setTTL(itemMap: MutableMap<String, AttributeValue>, numberOfDays: Duration) {
//        (Instant.now() + java.time.Duration.ofDays(30)).toEpochMilli() //numberOfDays.inWholeSeconds
        val ttlDate = numberOfDays.inWholeSeconds + System.currentTimeMillis()/1000
//        val ttlInSeconds = numberOfDays * 24 * 60 * 60
//        val ttlDate = (System.currentTimeMillis() / 1000L) + ttlInSeconds

        logger.info("TTL : $ttlDate")
        itemMap["ttl"] = AttributeValue.builder().n(ttlDate.toString()).build()
    }
}
