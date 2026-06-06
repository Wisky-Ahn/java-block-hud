plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    // 비모듈 JavaFX 앱의 jlink 런타임 + jpackage 네이티브 설치본 (DESIGN.md §6 Phase 7)
    id("org.beryx.runtime") version "1.13.1"
}

group = "io.github.wiskyahn"
// jpackage app-version은 숫자형만 허용하고 macOS는 첫 숫자가 1 이상이어야 함
version = "1.0.0"

java {
    toolchain {
        // 빌드/실행 모두 JDK 21로 강제 (DESIGN.md §2)
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

javafx {
    // JavaFX 플러그인이 OS별(win/mac/linux) 네이티브 모듈을 자동 해결 → 크로스플랫폼
    version = "21.0.4"
    modules = listOf("javafx.controls", "javafx.graphics")
}

dependencies {
    // 시스템 지표 — CPU/RAM/디스크/배터리/GPU (Phase 4, DESIGN.md §8.2)
    implementation("com.github.oshi:oshi-core:6.6.5")
    // JSON 영속화 — items.json / settings.json (Phase 1, DESIGN.md §4)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    // 로깅 (Diagnostics 대체)
    implementation("org.slf4j:slf4j-api:2.0.16")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.12")

    // 네이티브 창 Z-고정 (macOS NSWindow level / Windows User32) — DESIGN.md §8.1
    implementation("net.java.dev.jna:jna:5.14.0")

    testImplementation(platform("org.junit:junit-bom:5.11.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    // Launcher 분리: jpackage/fat-jar 배포 시 JavaFX 런타임 인식 문제 회피 (Phase 7)
    mainClass = "io.github.wiskyahn.blockhud.app.Launcher"
}

tasks.test {
    useJUnitPlatform()
}

// ── 패키징: jlink 커스텀 런타임 + jpackage OS별 설치본 (.dmg/.msi/.deb) ──
runtime {
    // 커스텀 JRE 압축
    options.set(listOf("--strip-debug", "--no-header-files", "--no-man-pages", "--compress", "2"))
    // 앱·의존성이 쓰는 JDK 모듈 (JavaFX는 클래스패스로 로드되므로 jlink 비포함)
    modules.set(listOf(
        "java.base", "java.desktop", "java.logging", "java.naming",
        "java.net.http", "java.scripting", "java.xml", "java.management",
        "jdk.unsupported", "java.sql"
    ))

    jpackage {
        imageName = "JavaBlockHud"
        installerName = "JavaBlockHud"
        // jpackage --app-version은 숫자형만 허용 (SNAPSHOT 불가)
        val osName = System.getProperty("os.name").lowercase()
        installerOptions = listOf("--vendor", "Wisky-Ahn")
        when {
            osName.contains("mac") -> installerType = "dmg"
            osName.contains("win") -> installerType = "msi"
            else -> installerType = "deb"
        }
    }
}
