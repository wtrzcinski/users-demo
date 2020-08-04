package com.codete.user.registry

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

class UsersSpec: Spek({
    Feature("users") {
        beforeEachScenario {
            Users.deleteAll()
            Emails.deleteAll()
        }

        Scenario("Create") {
            Given("Non existing user") {
                assertThat(Users.findAll()).isEmpty()
            }

            When("User is created") {
                Users.create(username = "username1", email = "email123@email123.com", password = "password")
            }

            Then("User exists") {
                assertThat(Users.findByUsername("username1")?.email).isEqualTo("email123@email123.com")
            }

            And("Notification is sent") {
                val emails = Emails.findByEmail("email123@email123.com")
                assertThat(emails).hasSize(1)
                assertThat(emails?.first()?.subject).contains("Account created")
                assertThat(emails?.first()?.text).contains("Registered user with username 'username1' and email 'email123@email123.com")
            }
        }

        Scenario("Update") {
            Given("Existing user") {
                Users.create(username = "username2", email = "email555@email555.com", password = "password")
            }

            When("User is updated") {
                Users.updateByUsername(username = "username2", firstName = "first name")
            }

            Then("User is updated") {
                val user = Users.findByUsername("username2")
                assertThat(user?.firstName).isEqualTo("first name")
                assertThat(user?.email).isEqualTo("email555@email555.com")
            }
        }

        Scenario("Delete") {
            Given("Existing user") {
                Users.create(username = "username3", email = "email555@email555.com", password = "password")
            }

            When("User is deleted") {
                Users.deleteByUsername(username = "username3")
            }

            Then("User is deleted") {
                assertThat(Users.findByUsername("username3")).isNull()
            }
        }

        Scenario("Bulk operations") {
            lateinit var user4: String
            lateinit var user5: String

            Given("Existing users") {
                user4 = Users.create(username = "username4", email = "email555@email777.com", password = "password")
                user5 = Users.create(username = "username5", email = "email555@email666.com", password = "password")
            }

            When("Users are updated with bulk") {
                Users.updateBulk(listOf(
                        mapOf(
                                "type" to "create",
                                "username" to "username6",
                                "password" to "password",
                                "email" to "email123123@email123123.com"
                        ),
                        mapOf(
                                "type" to "patch",
                                "id" to user4,
                                "firstName" to "first name"
                        ),
                        mapOf(
                                "type" to "delete",
                                "id" to user5
                        )
                ))
            }

            Then("Users should be updated") {
                assertThat(Users.findByUsername("username4")?.firstName).isEqualTo("first name")
                assertThat(Users.findByUsername("username5")).isNull()
                assertThat(Users.findByUsername("username6")?.email).isEqualTo("email123123@email123123.com")
            }

            And("Notification is sent") {
                val emails = Emails.findByEmail("email123123@email123123.com")
                assertThat(emails).hasSize(1)
                assertThat(emails?.first()?.subject).contains("Account created")
                assertThat(emails?.first()?.text).contains("Registered user with username 'username6' and email 'email123123@email123123.com")
            }
        }
    }
})