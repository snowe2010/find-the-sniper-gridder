package com.tylerthrailkill.sniper.reddit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
data class Child(
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
