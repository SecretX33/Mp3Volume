package com.github.secretx33.kotlinplayground.java

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.Future

data class User(val id: Int, val name: String)

data class Entity(val id: Int, val name: String)

class DatabaseCompletableFuture {
    fun getUser(): CompletableFuture<User> = TODO()
    fun getEntities(): CompletableFuture<List<Entity>> = TODO()
}

class EntityService(private val database: DatabaseCompletableFuture) {
    fun fetchEntity(): Any {
        val result = database.getUser().thenComposeAsync { user ->
            if (user.name == "Álax") {
                // Tell user that he doesn't have access to this entity
            }
            database.getEntities().thenApplyAsync { entities ->
                val entity = entities.firstOrNull { it.id == 3 }
                if (entity == null) {
                    // Tell user that we couldn't find the right entity
                }
                println("User $user requested entity $entity")
                // Return entity
            }
        }
        return result.get() // <-- Bad, very bad
    }
}