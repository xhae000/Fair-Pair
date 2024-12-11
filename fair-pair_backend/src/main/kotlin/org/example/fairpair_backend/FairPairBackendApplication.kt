package org.example.fairpair_backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean

@SpringBootApplication
class FairPairBackendApplication


fun main(args: Array<String>) {
    runApplication<FairPairBackendApplication>(*args)
}
