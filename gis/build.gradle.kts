/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

plugins {
    kotlin("multiplatform")
}


val ktorVersion = project.extra["ktor_version"] as String
val kotlinxDatetimeVersion = project.extra["kotlinx.datetime.version"] as String
val kotlinLoggingVersion = project.extra["kotlinLogging_version"] as String
val hamcrestVersion = project.extra["hamcrest_version"] as String
val mockitoVersion = project.extra["mockito_version"] as String
val assertjVersion = project.extra["assertj_version"] as String

kotlin {
    jvm()
    js() {
        browser {}
    }

    sourceSets {
        commonMain {
            dependencies {
                compileOnly("io.ktor:ktor-client-core:$ktorVersion")
                compileOnly(project(":commons"))
            }
        }

        jvmMain {
            dependencies {
                compileOnly("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }

        named("jsMain") {
            dependencies {
                compileOnly("io.ktor:ktor-client-js:$ktorVersion")
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":commons"))
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetimeVersion")
            }
        }

        jvmTest {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("org.hamcrest:hamcrest-core:$hamcrestVersion")
                implementation("org.hamcrest:hamcrest-library:$hamcrestVersion")
                implementation("org.mockito:mockito-core:$mockitoVersion")
                implementation("org.assertj:assertj-core:$assertjVersion")
            }
        }

        // Fix for 'Could not find "io.github.microutils:kotlin-logging"...' and
        // 'Could not find "io.ktor:ktor-client-js"...'build errors (Kotlin 1.9.xx versions):
        named("jsTest") {
            dependencies {
                implementation("io.ktor:ktor-client-js:$ktorVersion")
                implementation("io.github.microutils:kotlin-logging-js:$kotlinLoggingVersion")
            }
        }
    }
}
