package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.*
import java.util.*

class TokenManager(conf : HoconApplicationConfig) {
    val auidence = conf.property("jwt.audience").getString()
    val issuer = conf.property("jwt.issuer").getString()
    val realm = conf.property("jwt.realm").getString()
    val secret = conf.property("jwt.secret").getString()

    val algorithm = Algorithm.HMAC256(secret)

    fun createToken(username : String) : String {
        return JWT.create()
            .withAudience(auidence)
            .withIssuer(issuer)
            .withExpiresAt(Date(System.currentTimeMillis()+20000))
            .sign(algorithm)
    }
}