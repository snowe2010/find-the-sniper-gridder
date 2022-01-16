package com.tylerthrailkill.sniper.reddit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@Serializable
data class Listing(
    val kind: String,
    val data: Data,
)

@Serializable
data class Data(
    val children: List<Child>
)

@Serializable
//@DynamoDbBean
data class Child(
//    @get:DynamoDbAttribute("ttl")
//    val ttl: Long,
    val kind: String,
    val data: ChildData,
)

@Serializable
data class ChildData(
    val subreddit: String,
    val url: String,
    @SerialName("post_hint") val postHint: String? = null,
    val name: String, //fullname of post
//    @SerialName("crosspost_parent_list") val crosspostParentList: List<ChildData> = listOf(),
//    @SerialName("crosspost_parent") val crosspostParent: String? = null
)
