package com.example.plugins

import com.example.TokenManager
import com.example.module.User
import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import redis.clients.jedis.Jedis

fun Application.configureRouting() {
    val jedis = Jedis("localhost",6379)
    val obj = TokenManager(HoconApplicationConfig(ConfigFactory.load()))
    routing {
        post("/create-data") {
            val data = call.receive<User>()
            val jsonStrinf = Json.encodeToString(data)
            val userKey = "user:${data.name}"
            val expiration : Long = 10   //In setex it is calculated in seconds
            val result = jedis.setex(userKey,expiration,jsonStrinf)
            call.respond(HttpStatusCode.OK,"inserted")
        }

        get("/read/{user?}") {
            val user = call.parameters["user"] ?: return@get call.respondText("No user parameter found")
            val userid = "user:$user"
            val result = jedis.get(userid)
            if(result != null) {
                call.respond(HttpStatusCode.OK,result)
            }
            else {
                call.respond(HttpStatusCode.NotFound,"user not found")
            }
        }

        delete("/user/{id?}") {
            val id = call.parameters["id"]
            val userid = "user:$id"
            val res = jedis.del(userid)
            if(res > 0) {
                call.respond(HttpStatusCode.OK,"User deleted of id $userid")
            }
            else {
                call.respond(HttpStatusCode.NotFound,"user not found")
            }
        }

        put("/update/{id?}") {
            val id = call.parameters["id"]
            var userid = "user:$id"
            var jsonString = Json.decodeFromString<User>(jedis.get(userid))
            val updated = call.receive<User>()
              jsonString.name = updated.age
//            jsonString.age = 33.toString() //we can also give directly

            val encodeToString = Json.encodeToString(jsonString)

            jedis.set(userid,encodeToString)
            call.respond(HttpStatusCode.OK,"updated")
        }
        get("/token") {
            try {
                val token = obj.createToken("saipreetham@123")
                jedis.setex("Token",10,token)
                call.respond(HttpStatusCode.OK, mapOf("Token" to token))
            }
            catch (e : Exception) {
                call.respondText("message : ${e.message}")
            }
        }
        get("/getToken") {
            val token = jedis.get("Token")
            if(token != null) {
                call.respond(HttpStatusCode.OK,"Token not expired Token : $token")
            }
            else {
                call.respond(HttpStatusCode.OK,"Token not found maybe expired")
            }
        }
        delete("/deletetoken") {
           val res = jedis.del("Token")
            if(res > 0) {
                call.respond(HttpStatusCode.OK,"token deleted")
            }
            else {
                call.respond(HttpStatusCode.OK,"Token not found")
            }
        }

    }
}

