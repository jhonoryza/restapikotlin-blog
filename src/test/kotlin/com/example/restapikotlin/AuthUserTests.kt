package com.example.restapikotlin

import com.example.restapikotlin.entity.User
import com.example.restapikotlin.repository.UserRepository
import com.example.restapikotlin.request.LoginRequest
import com.example.restapikotlin.request.RegisterRequest
import com.example.restapikotlin.util.BCrypt
import com.fasterxml.jackson.module.kotlin.jsonMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.Date
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class AuthUserTests() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
    }

    @Test
    fun testRegister() {
        val registerRequest = RegisterRequest(
            fullName = "John",
            email = "j@j.com",
            password = "123456"
        )
        mockMvc.post("/api/register") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper().writeValueAsString(registerRequest)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content {
                jsonPath("$.code") { value(200) }
                jsonPath("$.message") { value("register success") }
            }
        }
    }

    fun createRegisteredUser(token : String? = null) {
        val user = User(
            id = UUID.randomUUID().toString(),
            email = "j@j.com",
            password = BCrypt.hashpw("123456", BCrypt.gensalt()),
            fullName = "John",
            token = token,
            createdAt = Date(),
            updatedAt = Date()
        )
        userRepository.save(user)
    }
    @Test
    fun loginTest() {
        createRegisteredUser()

        val loginRequest = LoginRequest(
            email = "j@j.com",
            password = "123456"
        )
        mockMvc.post("/api/login") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper().writeValueAsString(loginRequest)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content {
                jsonPath("$.code") { value(200) }
                jsonPath("$.message") { value("login success") }
                jsonPath("$.data.email") { value("j@j.com") }
                jsonPath("$.data.token") { exists() }
            }
        }
    }

    @Test
    fun logoutTest() {
        createRegisteredUser(token = "12345678910")

        mockMvc.post("/api/logout") {
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            headers {
                header("Authorization", "12345678910")
            }
        }.andExpect {
            status { isOk() }
            content {
                jsonPath("$.code") { value(200) }
                jsonPath("$.message") { value("logout success") }
                jsonPath("$.data.email") { value( "j@j.com" ) }

            }
        }
    }
}
