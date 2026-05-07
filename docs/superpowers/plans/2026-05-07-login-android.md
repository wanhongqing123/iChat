# iChat 登录页 · Android 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 `UI/Android/` 落地 iChat 第一期登录页：手机号 → 6 位验证码 → mock 校验 → 进入空白主页占位。

**Architecture:** 单 Module Android Studio 工程，Jetpack Compose 声明式 UI，单 Activity + Compose Navigation 承载两屏。状态机 `LoginViewModel` 持有 `MockAuthRepository`，60s 倒计时用 `viewModelScope` 协程。无 DI 框架（YAGNI）。

**Tech Stack:** Kotlin 2.0+ · AGP 8.7+ · **Gradle 8.9+**（AGP 8.7 最低要求）· minSdk 26 / targetSdk 34 · Jetpack Compose BOM 2024.12+ · navigation-compose · kotlinx-coroutines · JUnit 4 · Compose UI Test

**Spec:** `docs/superpowers/specs/2026-05-07-login-screen-design.md`

**Working dir:** `UI/Android/` （从空目录开始）

---

## 文件结构

```
UI/Android/
├── settings.gradle.kts
├── build.gradle.kts                # root
├── gradle.properties
├── gradle/wrapper/                 # Gradle wrapper（gradle wrapper 命令生成）
├── gradlew, gradlew.bat
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro          # 默认空
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── kotlin/com/ichat/login/
        │   │   ├── MainActivity.kt
        │   │   ├── theme/
        │   │   │   ├── Color.kt
        │   │   │   ├── Type.kt
        │   │   │   ├── Shape.kt
        │   │   │   └── Theme.kt
        │   │   ├── data/
        │   │   │   ├── PhoneUtils.kt           # format / mask / isValid
        │   │   │   └── MockAuthRepository.kt
        │   │   ├── login/
        │   │   │   ├── LoginViewModel.kt
        │   │   │   ├── LoginNavGraph.kt
        │   │   │   ├── PhoneScreen.kt
        │   │   │   ├── CodeScreen.kt
        │   │   │   └── components/
        │   │   │       ├── PrimaryButton.kt
        │   │   │       ├── PhoneInput.kt
        │   │   │       └── CodeBoxes.kt
        │   │   └── home/
        │   │       └── HomePlaceholderScreen.kt
        │   └── res/values/strings.xml
        ├── test/kotlin/com/ichat/login/
        │   ├── PhoneUtilsTest.kt
        │   ├── MockAuthRepositoryTest.kt
        │   └── LoginViewModelTest.kt
        └── androidTest/kotlin/com/ichat/login/
            ├── PrimaryButtonTest.kt
            ├── CodeBoxesTest.kt
            └── LoginE2ETest.kt
```

---

## Task 1: Scaffold Android Gradle 工程

**Files:**
- Create: `UI/Android/settings.gradle.kts`
- Create: `UI/Android/build.gradle.kts`
- Create: `UI/Android/gradle.properties`
- Create: `UI/Android/app/build.gradle.kts`
- Create: `UI/Android/app/proguard-rules.pro`
- Create: `UI/Android/app/src/main/AndroidManifest.xml`
- Create: `UI/Android/app/src/main/kotlin/com/ichat/login/MainActivity.kt`
- Create: `UI/Android/app/src/main/res/values/strings.xml`

- [ ] **Step 1: 创建 `settings.gradle.kts`**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "iChatAndroid"
include(":app")
```

- [ ] **Step 2: 创建根 `build.gradle.kts`**

```kotlin
plugins {
    id("com.android.application") version "8.7.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
```

- [ ] **Step 3: 创建 `gradle.properties`**

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
android.useAndroidX=true
kotlin.code.style=official
```

- [ ] **Step 4: 创建 `app/build.gradle.kts`**

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.ichat.login"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ichat.login"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("main") { java.srcDirs("src/main/kotlin") }
        getByName("test") { java.srcDirs("src/test/kotlin") }
        getByName("androidTest") { java.srcDirs("src/androidTest/kotlin") }
    }

    buildFeatures { compose = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

- [ ] **Step 5: 创建 `app/proguard-rules.pro` （留空文件）**

```
# Add project specific ProGuard rules here.
```

- [ ] **Step 6: 创建 `app/src/main/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:allowBackup="true"
        android:label="iChat"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:screenOrientation="portrait">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 7: 创建 `strings.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">iChat</string>
</resources>
```

- [ ] **Step 8: 创建 `MainActivity.kt`（第一版，仅打印 "Hello iChat" 验证脚手架）**

```kotlin
package com.ichat.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                Text("Hello iChat")
            }
        }
    }
}
```

- [ ] **Step 9: 生成 Gradle wrapper**

Run（在 `UI/Android/` 下）：
```bash
gradle wrapper --gradle-version 8.9
```

注：AGP 8.7 要求 Gradle 最低 8.9。`gradle-8.7` 会在构建时报 "Minimum supported Gradle version is 8.9"。

Expected：生成 `gradle/wrapper/gradle-wrapper.jar`、`gradle-wrapper.properties`、`gradlew`、`gradlew.bat`。

如果本机没装 Gradle，备选：用 Android Studio 打开 `UI/Android/` 让其自动同步并生成 wrapper。

- [ ] **Step 10: 验证脚手架能编译**

Run：
```bash
cd UI/Android && ./gradlew :app:assembleDebug
```

Expected：`BUILD SUCCESSFUL`。

- [ ] **Step 11: 提交**

```bash
git add UI/Android
git commit -m "feat(android): scaffold Gradle + Compose project"
```

---

## Task 2: 设计 token 主题

**Files:**
- Create: `UI/Android/app/src/main/kotlin/com/ichat/login/theme/Color.kt`
- Create: `UI/Android/app/src/main/kotlin/com/ichat/login/theme/Type.kt`
- Create: `UI/Android/app/src/main/kotlin/com/ichat/login/theme/Shape.kt`
- Create: `UI/Android/app/src/main/kotlin/com/ichat/login/theme/Theme.kt`
- Modify: `UI/Android/app/src/main/kotlin/com/ichat/login/MainActivity.kt`

- [ ] **Step 1: 创建 `Color.kt`**

```kotlin
package com.ichat.login.theme

import androidx.compose.ui.graphics.Color

val BrandPrimary          = Color(0xFF4F86FF)
val BrandPrimaryPressed   = Color(0xFF3B6BE0)
val BrandPrimaryDisabled  = Color(0xFFB7CDFA)

val BgPage    = Color(0xFFF2F6FF)
val BgSurface = Color(0xFFFFFFFF)

val BorderInput      = Color(0xFFDCE6FA)
val BorderInputFocus = BrandPrimary

val TextPrimary   = Color(0xFF1A2233)
val TextSecondary = Color(0xFF7884A3)
val TextTertiary  = Color(0xFF9AA3B5)

val StateError = Color(0xFFE5484D)
```

- [ ] **Step 2: 创建 `Type.kt`**

```kotlin
package com.ichat.login.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val TitleLg   = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
val BodyMd    = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal,   color = TextPrimary)
val BodySm    = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal,   color = TextSecondary)
val Caption   = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal,   color = TextTertiary)
val CodeBox   = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)

val IChatTypography = Typography(
    titleLarge = TitleLg,
    bodyMedium = BodyMd,
    bodySmall  = BodySm,
)
```

- [ ] **Step 3: 创建 `Shape.kt`**

```kotlin
package com.ichat.login.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

val InputShape    = RoundedCornerShape(14.dp)
val ButtonShape   = RoundedCornerShape(24.dp)   // pill
val CodeBoxShape  = RoundedCornerShape(12.dp)
```

- [ ] **Step 4: 创建 `Theme.kt`**

```kotlin
package com.ichat.login.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val IChatColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = BgSurface,
    background = BgPage,
    onBackground = TextPrimary,
    surface = BgSurface,
    onSurface = TextPrimary,
    error = StateError,
)

@Composable
fun IChatTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = IChatColorScheme,
        typography = IChatTypography,
        content = content,
    )
}
```

- [ ] **Step 5: 修改 `MainActivity.kt` 应用主题**

```kotlin
package com.ichat.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ichat.login.theme.IChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IChatTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("iChat", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}
```

- [ ] **Step 6: 验证**

Run：`cd UI/Android && ./gradlew :app:assembleDebug`

Expected：BUILD SUCCESSFUL。

肉眼检查（启动模拟器后）：背景色为 `#F2F6FF`，居中显示 `iChat`。

- [ ] **Step 7: 提交**

```bash
git add UI/Android/app/src/main/kotlin/com/ichat/login/theme UI/Android/app/src/main/kotlin/com/ichat/login/MainActivity.kt
git commit -m "feat(android): add design tokens (color/type/shape/theme)"
```

---

## Task 3: 手机号工具函数（TDD）

**Files:**
- Create: `UI/Android/app/src/test/kotlin/com/ichat/login/PhoneUtilsTest.kt`
- Create: `UI/Android/app/src/main/kotlin/com/ichat/login/data/PhoneUtils.kt`

- [ ] **Step 1: 写失败测试 `PhoneUtilsTest.kt`**

```kotlin
package com.ichat.login

import com.ichat.login.data.PhoneUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneUtilsTest {

    @Test fun digitsOnly_removesNonDigits() {
        assertEquals("13800138000", PhoneUtils.digitsOnly("138 0013 8000"))
        assertEquals("13800138000", PhoneUtils.digitsOnly("138-0013-8000"))
        assertEquals("",            PhoneUtils.digitsOnly("abc"))
    }

    @Test fun format_appliesGroupedSpacing() {
        assertEquals("",              PhoneUtils.format(""))
        assertEquals("1",             PhoneUtils.format("1"))
        assertEquals("138",           PhoneUtils.format("138"))
        assertEquals("138 0",         PhoneUtils.format("1380"))
        assertEquals("138 0013",      PhoneUtils.format("1380013"))
        assertEquals("138 0013 8",    PhoneUtils.format("13800138"))
        assertEquals("138 0013 8000", PhoneUtils.format("13800138000"))
    }

    @Test fun format_truncatesAt11Digits() {
        assertEquals("138 0013 8000", PhoneUtils.format("13800138000999"))
    }

    @Test fun isValid_acceptsValidChineseMobile() {
        assertTrue(PhoneUtils.isValid("13800138000"))
        assertTrue(PhoneUtils.isValid("15912345678"))
        assertTrue(PhoneUtils.isValid("18800001111"))
    }

    @Test fun isValid_rejectsInvalid() {
        assertFalse(PhoneUtils.isValid(""))
        assertFalse(PhoneUtils.isValid("1380013800"))     // < 11
        assertFalse(PhoneUtils.isValid("138001380001"))   // > 11
        assertFalse(PhoneUtils.isValid("00000000000"))    // 不以 1 开头
        assertFalse(PhoneUtils.isValid("12345678901"))    // 第二位不在 3..9
        assertFalse(PhoneUtils.isValid("1a800138000"))    // 含字母
    }

    @Test fun mask_hidesMiddleFour() {
        assertEquals("138****8000", PhoneUtils.mask("13800138000"))
    }

    @Test fun mask_returnsAsIs_whenNot11Digits() {
        assertEquals("",         PhoneUtils.mask(""))
        assertEquals("12345",    PhoneUtils.mask("12345"))
    }
}
```

- [ ] **Step 2: 运行测试，确认失败**

Run：`cd UI/Android && ./gradlew :app:testDebugUnitTest --tests "com.ichat.login.PhoneUtilsTest"`

Expected：编译失败，`PhoneUtils` 未定义。

- [ ] **Step 3: 实现 `PhoneUtils.kt`**

```kotlin
package com.ichat.login.data

object PhoneUtils {

    fun digitsOnly(input: String): String = input.filter { it.isDigit() }

    fun format(raw: String): String {
        val digits = digitsOnly(raw).take(11)
        return when {
            digits.length <= 3 -> digits
            digits.length <= 7 -> "${digits.substring(0,3)} ${digits.substring(3)}"
            else               -> "${digits.substring(0,3)} ${digits.substring(3,7)} ${digits.substring(7)}"
        }
    }

    fun isValid(phone: String): Boolean {
        val d = digitsOnly(phone)
        return d.length == 11 && d[0] == '1' && d[1] in '3'..'9'
    }

    fun mask(phone: String): String {
        val d = digitsOnly(phone)
        return if (d.length == 11) "${d.substring(0,3)}****${d.substring(7)}" else d
    }
}
```

- [ ] **Step 4: 运行测试，确认通过**

Run：`./gradlew :app:testDebugUnitTest --tests "com.ichat.login.PhoneUtilsTest"`

Expected：6 tests passed。

- [ ] **Step 5: 提交**

```bash
git add UI/Android/app/src/main/kotlin/com/ichat/login/data/PhoneUtils.kt UI/Android/app/src/test/kotlin/com/ichat/login/PhoneUtilsTest.kt
git commit -m "feat(android): add PhoneUtils (format/isValid/mask) with tests"
```

---

## Task 4: MockAuthRepository（TDD）

**Files:**
- Create: `UI/Android/app/src/test/kotlin/com/ichat/login/MockAuthRepositoryTest.kt`
- Create: `UI/Android/app/src/main/kotlin/com/ichat/login/data/MockAuthRepository.kt`

- [ ] **Step 1: 写失败测试**

```kotlin
package com.ichat.login

import com.ichat.login.data.AuthResult
import com.ichat.login.data.MockAuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MockAuthRepositoryTest {

    @Test fun requestCode_returnsSuccess() = runTest {
        val repo = MockAuthRepository()
        assertEquals(AuthResult.RequestSuccess, repo.requestCode("13800138000"))
    }

    @Test fun verifyCode_correctCode_returnsSuccess() = runTest {
        val repo = MockAuthRepository()
        val result = repo.verifyCode("13800138000", "123456")
        assertTrue(result is AuthResult.VerifySuccess)
        result as AuthResult.VerifySuccess
        assertEquals("mock-token", result.token)
        assertEquals(false, result.isNewUser)
    }

    @Test fun verifyCode_wrongCode_returnsInvalidCode() = runTest {
        val repo = MockAuthRepository()
        val result = repo.verifyCode("13800138000", "000000")
        assertEquals(AuthResult.InvalidCode, result)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run：`./gradlew :app:testDebugUnitTest --tests "com.ichat.login.MockAuthRepositoryTest"`

Expected：编译失败（`AuthResult` / `MockAuthRepository` 未定义）。

- [ ] **Step 3: 实现 `MockAuthRepository.kt`**

```kotlin
package com.ichat.login.data

import kotlinx.coroutines.delay

sealed class AuthResult {
    data object RequestSuccess : AuthResult()
    data class VerifySuccess(val token: String, val isNewUser: Boolean) : AuthResult()
    data object InvalidCode : AuthResult()
    data object NetworkError : AuthResult()
}

class MockAuthRepository {

    suspend fun requestCode(phone: String): AuthResult {
        delay(800)
        return AuthResult.RequestSuccess
    }

    suspend fun verifyCode(phone: String, code: String): AuthResult {
        delay(600)
        return if (code == "123456") {
            AuthResult.VerifySuccess(token = "mock-token", isNewUser = false)
        } else {
            AuthResult.InvalidCode
        }
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

Run：`./gradlew :app:testDebugUnitTest --tests "com.ichat.login.MockAuthRepositoryTest"`

Expected：3 tests passed。

- [ ] **Step 5: 提交**

```bash
git add UI/Android/app/src/main/kotlin/com/ichat/login/data/MockAuthRepository.kt UI/Android/app/src/test/kotlin/com/ichat/login/MockAuthRepositoryTest.kt
git commit -m "feat(android): add MockAuthRepository with tests"
```

---

## Task 5: LoginViewModel 状态机（TDD）

**Files:**
- Create: `UI/Android/app/src/test/kotlin/com/ichat/login/LoginViewModelTest.kt`
- Create: `UI/Android/app/src/main/kotlin/com/ichat/login/login/LoginViewModel.kt`

覆盖测试用例 #1, #3, #4, #5, #6, #7, #8（spec §6.1）。

- [ ] **Step 1: 写失败测试**

```kotlin
package com.ichat.login

import com.ichat.login.data.AuthResult
import com.ichat.login.data.MockAuthRepository
import com.ichat.login.login.LoginPhase
import com.ichat.login.login.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp()  { Dispatchers.setMain(dispatcher) }
    @After  fun tearDown(){ Dispatchers.resetMain() }

    private fun stubRepo(
        request: suspend (String) -> AuthResult = { AuthResult.RequestSuccess },
        verify:  suspend (String, String) -> AuthResult =
            { _, c -> if (c == "123456") AuthResult.VerifySuccess("t", false) else AuthResult.InvalidCode },
    ) = object : MockAuthRepository() {
        override suspend fun requestCode(phone: String) = request(phone)
        override suspend fun verifyCode(phone: String, code: String) = verify(phone, code)
    }

    @Test fun phoneTooShort_buttonDisabled() = runTest {
        val vm = LoginViewModel(stubRepo())
        vm.onPhoneChange("1380013")
        assertEquals(false, vm.state.value.canRequestCode)
    }

    @Test fun phoneInvalidFormat_buttonDisabled() = runTest {
        val vm = LoginViewModel(stubRepo())
        vm.onPhoneChange("00000000000")
        assertEquals(false, vm.state.value.canRequestCode)
    }

    @Test fun phoneValid_buttonEnabled() = runTest {
        val vm = LoginViewModel(stubRepo())
        vm.onPhoneChange("13800138000")
        assertEquals(true, vm.state.value.canRequestCode)
    }

    @Test fun requestCode_movesToCodePhaseAndStartsCountdown() = runTest {
        val vm = LoginViewModel(stubRepo())
        vm.onPhoneChange("13800138000")
        vm.requestCode()
        advanceUntilIdle()
        assertEquals(LoginPhase.Code, vm.state.value.phase)
        assertEquals(60, vm.state.value.countdown)
    }

    @Test fun countdown_decrementsEverySecond() = runTest {
        val vm = LoginViewModel(stubRepo())
        vm.onPhoneChange("13800138000"); vm.requestCode()
        advanceUntilIdle()
        assertEquals(60, vm.state.value.countdown)
        advanceTimeBy(3_000); runCurrent()
        assertEquals(57, vm.state.value.countdown)
    }

    @Test fun verifyCode_correct_emitsLoginSuccess() = runTest {
        val vm = LoginViewModel(stubRepo())
        vm.onPhoneChange("13800138000"); vm.requestCode(); advanceUntilIdle()
        vm.onCodeChange("123456")
        advanceUntilIdle()
        assertTrue(vm.events.replayCache.contains(com.ichat.login.login.LoginEvent.LoginSuccess))
    }

    @Test fun verifyCode_wrong_setsErrorThenClearsAfterDelay() = runTest {
        val vm = LoginViewModel(stubRepo())
        vm.onPhoneChange("13800138000"); vm.requestCode(); advanceUntilIdle()
        vm.onCodeChange("000000")
        advanceUntilIdle()
        assertEquals("验证码错误，请重新输入", vm.state.value.errorMessage)
        advanceTimeBy(1_200); runCurrent()
        assertEquals("",     vm.state.value.code)
        assertNull(vm.state.value.errorMessage)
    }

    @Test fun goBack_preservesPhoneAndCountdown() = runTest {
        val vm = LoginViewModel(stubRepo())
        vm.onPhoneChange("13800138000"); vm.requestCode(); advanceUntilIdle()
        advanceTimeBy(5_000); runCurrent()           // countdown should be ~55
        vm.goBack()
        assertEquals(LoginPhase.Phone, vm.state.value.phase)
        assertEquals("13800138000", vm.state.value.phone)
        assertEquals(55, vm.state.value.countdown)   // 不重置
    }

    @Test fun requestCode_duringCountdown_skipsRefetchAndReentersCodeScreen() = runTest {
        var requestCount = 0
        val vm = LoginViewModel(stubRepo(request = {
            requestCount++
            AuthResult.RequestSuccess
        }))
        vm.onPhoneChange("13800138000"); vm.requestCode(); advanceUntilIdle()
        assertEquals(1, requestCount)
        advanceTimeBy(5_000); runCurrent()
        vm.goBack()
        assertEquals(LoginPhase.Phone, vm.state.value.phase)
        // 用例 #7：倒计时中再次点击 → 直接切回 Code 屏，不重新请求
        vm.requestCode(); advanceUntilIdle()
        assertEquals(LoginPhase.Code, vm.state.value.phase)
        assertEquals(55, vm.state.value.countdown)
        assertEquals(1, requestCount)               // 没有再次发码
    }

    @Test fun networkError_onRequestCode_keepsPhonePhaseAndShowsError() = runTest {
        val vm = LoginViewModel(stubRepo(request = { AuthResult.NetworkError }))
        vm.onPhoneChange("13800138000")
        vm.requestCode(); advanceUntilIdle()
        assertEquals(LoginPhase.Phone, vm.state.value.phase)
        assertEquals("网络异常，请检查后重试", vm.state.value.errorMessage)
    }

    private fun runCurrent() = dispatcher.scheduler.runCurrent()
}
```

- [ ] **Step 2: 运行测试确认失败**

Run：`./gradlew :app:testDebugUnitTest --tests "com.ichat.login.LoginViewModelTest"`

Expected：编译失败（`LoginViewModel` 等未定义）。

- [ ] **Step 3: 把 `MockAuthRepository` 的方法改为 `open`（让 stubRepo 能继承）**

修改 `app/src/main/kotlin/com/ichat/login/data/MockAuthRepository.kt`：

```kotlin
open class MockAuthRepository {
    open suspend fun requestCode(phone: String): AuthResult { /* 同前 */ }
    open suspend fun verifyCode(phone: String, code: String): AuthResult { /* 同前 */ }
}
```

- [ ] **Step 4: 实现 `LoginViewModel.kt`**

```kotlin
package com.ichat.login.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ichat.login.data.AuthResult
import com.ichat.login.data.MockAuthRepository
import com.ichat.login.data.PhoneUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LoginPhase { Phone, Code }

data class LoginUiState(
    val phase: LoginPhase = LoginPhase.Phone,
    val phone: String = "",
    val code: String = "",
    val countdown: Int = 0,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) {
    val canRequestCode: Boolean get() = PhoneUtils.isValid(phone) && !isSubmitting
}

sealed class LoginEvent {
    data object LoginSuccess : LoginEvent()
}

class LoginViewModel(
    private val repo: MockAuthRepository = MockAuthRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>(replay = 1)
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    private var countdownJob: Job? = null
    private var errorClearJob: Job? = null

    fun onPhoneChange(raw: String) {
        _state.update { it.copy(phone = PhoneUtils.digitsOnly(raw).take(11)) }
    }

    fun onCodeChange(raw: String) {
        val digits = raw.filter { it.isDigit() }.take(6)
        _state.update { it.copy(code = digits, errorMessage = null) }
        if (digits.length == 6) verifyCode()
    }

    fun requestCode() {
        if (!PhoneUtils.isValid(_state.value.phone) || _state.value.isSubmitting) return
        // 用例 #7：倒计时还没归零 → 不重新请求，直接切回 Code 屏
        if (_state.value.countdown > 0) {
            _state.update { it.copy(phase = LoginPhase.Code, errorMessage = null) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, errorMessage = null) }
            when (repo.requestCode(_state.value.phone)) {
                is AuthResult.RequestSuccess -> {
                    _state.update { it.copy(phase = LoginPhase.Code, code = "", isSubmitting = false, countdown = 60) }
                    startCountdown()
                }
                else -> _state.update {
                    it.copy(isSubmitting = false, errorMessage = "网络异常，请检查后重试")
                }
            }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (_state.value.countdown > 0) {
                delay(1_000)
                _state.update { it.copy(countdown = (it.countdown - 1).coerceAtLeast(0)) }
            }
        }
    }

    fun resendCode() {
        if (_state.value.countdown > 0) return
        viewModelScope.launch {
            when (repo.requestCode(_state.value.phone)) {
                is AuthResult.RequestSuccess -> { _state.update { it.copy(countdown = 60) }; startCountdown() }
                else -> _state.update { it.copy(errorMessage = "网络异常，请检查后重试") }
            }
        }
    }

    private fun verifyCode() {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            val result = repo.verifyCode(_state.value.phone, _state.value.code)
            _state.update { it.copy(isSubmitting = false) }
            when (result) {
                is AuthResult.VerifySuccess -> _events.emit(LoginEvent.LoginSuccess)
                AuthResult.InvalidCode      -> showCodeError()
                AuthResult.NetworkError     -> _state.update { it.copy(errorMessage = "网络异常，请检查后重试") }
                else                        -> Unit
            }
        }
    }

    private fun showCodeError() {
        _state.update { it.copy(errorMessage = "验证码错误，请重新输入") }
        errorClearJob?.cancel()
        errorClearJob = viewModelScope.launch {
            delay(1_200)
            _state.update { it.copy(code = "", errorMessage = null) }
        }
    }

    fun goBack() {
        _state.update { it.copy(phase = LoginPhase.Phone, errorMessage = null) }
    }
}
```

- [ ] **Step 5: 运行测试确认通过**

Run：`./gradlew :app:testDebugUnitTest --tests "com.ichat.login.LoginViewModelTest"`

Expected：10 tests passed。

- [ ] **Step 6: 提交**

```bash
git add UI/Android/app/src/main/kotlin/com/ichat/login UI/Android/app/src/test/kotlin/com/ichat/login/LoginViewModelTest.kt UI/Android/app/src/main/kotlin/com/ichat/login/data/MockAuthRepository.kt
git commit -m "feat(android): add LoginViewModel state machine with full unit tests"
```

---

## Task 6: PrimaryButton 组件（TDD with Compose UI Test）

**Files:**
- Create: `UI/Android/app/src/androidTest/kotlin/com/ichat/login/PrimaryButtonTest.kt`
- Create: `UI/Android/app/src/main/kotlin/com/ichat/login/login/components/PrimaryButton.kt`

- [ ] **Step 1: 写失败 Compose UI 测试**

```kotlin
package com.ichat.login

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ichat.login.login.components.PrimaryButton
import com.ichat.login.theme.IChatTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PrimaryButtonTest {
    @get:Rule val rule = createComposeRule()

    @Test fun renders_text_and_handles_click_when_enabled() {
        val clicked = mutableStateOf(false)
        rule.setContent {
            IChatTheme {
                PrimaryButton(text = "获取验证码", enabled = true) { clicked.value = true }
            }
        }
        rule.onNodeWithText("获取验证码").assertHasClickAction().assertIsEnabled().performClick()
        assertTrue(clicked.value)
    }

    @Test fun shows_disabled_when_enabled_false() {
        rule.setContent {
            IChatTheme {
                PrimaryButton(text = "获取验证码", enabled = false) {}
            }
        }
        rule.onNodeWithText("获取验证码").assertIsNotEnabled()
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run：`./gradlew :app:connectedDebugAndroidTest --tests "com.ichat.login.PrimaryButtonTest"`

Expected：编译失败（`PrimaryButton` 未定义）。需先启动 Android 模拟器。

- [ ] **Step 3: 实现 `PrimaryButton.kt`**

```kotlin
package com.ichat.login.login.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ichat.login.theme.BgSurface
import com.ichat.login.theme.BrandPrimary
import com.ichat.login.theme.BrandPrimaryDisabled
import com.ichat.login.theme.ButtonShape

@Composable
fun PrimaryButton(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = ButtonShape,
        modifier = modifier.fillMaxWidth().height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandPrimary,
            contentColor = BgSurface,
            disabledContainerColor = BrandPrimaryDisabled,
            disabledContentColor = BgSurface,
        ),
    ) {
        Text(text)
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

Run：`./gradlew :app:connectedDebugAndroidTest --tests "com.ichat.login.PrimaryButtonTest"`

Expected：2 tests passed。

- [ ] **Step 5: 提交**

```bash
git add UI/Android/app/src/main/kotlin/com/ichat/login/login/components/PrimaryButton.kt UI/Android/app/src/androidTest/kotlin/com/ichat/login/PrimaryButtonTest.kt
git commit -m "feat(android): add PrimaryButton component with Compose UI tests"
```

---

## Task 7: PhoneInput 组件

**Files:**
- Create: `UI/Android/app/src/main/kotlin/com/ichat/login/login/components/PhoneInput.kt`

无独立 androidTest（格式化逻辑已在 `PhoneUtilsTest` 覆盖；UI 行为在 Task 12 E2E 中覆盖）。YAGNI。

- [ ] **Step 1: 实现 `PhoneInput.kt`**

```kotlin
package com.ichat.login.login.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ichat.login.data.PhoneUtils
import com.ichat.login.theme.BgSurface
import com.ichat.login.theme.BodyMd
import com.ichat.login.theme.BorderInput
import com.ichat.login.theme.InputShape
import com.ichat.login.theme.TextSecondary

@Composable
fun PhoneInput(
    rawDigits: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val display = PhoneUtils.format(rawDigits)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(BgSurface, InputShape)
            .border(1.dp, BorderInput, InputShape)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("+86", style = BodyMd.copy(color = TextSecondary))
        Text("|", style = BodyMd.copy(color = BorderInput))
        BasicTextField(
            value = display,
            onValueChange = { onValueChange(PhoneUtils.digitsOnly(it).take(11)) },
            singleLine = true,
            textStyle = BodyMd,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            decorationBox = { inner ->
                if (display.isEmpty()) Text("请输入手机号", style = BodyMd.copy(color = TextSecondary))
                inner()
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
```

- [ ] **Step 2: 验证编译通过**

Run：`./gradlew :app:assembleDebug`

Expected：BUILD SUCCESSFUL。

- [ ] **Step 3: 提交**

```bash
git add UI/Android/app/src/main/kotlin/com/ichat/login/login/components/PhoneInput.kt
git commit -m "feat(android): add PhoneInput component"
```

---

## Task 8: CodeBoxes 组件（TDD with Compose UI Test）

**Files:**
- Create: `UI/Android/app/src/androidTest/kotlin/com/ichat/login/CodeBoxesTest.kt`
- Create: `UI/Android/app/src/main/kotlin/com/ichat/login/login/components/CodeBoxes.kt`

覆盖：自动跳焦、退格回上、粘贴 6 位自动填充、错误态描边变红。

- [ ] **Step 1: 写失败 Compose UI 测试**

```kotlin
package com.ichat.login

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.ichat.login.login.components.CodeBoxes
import com.ichat.login.theme.IChatTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CodeBoxesTest {
    @get:Rule val rule = createComposeRule()

    @Test fun typing_appends_digits_and_calls_onChange() {
        val state = mutableStateOf("")
        rule.setContent {
            IChatTheme {
                CodeBoxes(value = state.value, onValueChange = { state.value = it }, isError = false)
            }
        }
        rule.onNodeWithTag("code-input").performTextInput("8")
        assertEquals("8", state.value)
        rule.onNodeWithTag("code-input").performTextInput("3")
        assertEquals("83", state.value)
    }

    @Test fun pasting_six_digits_fills_all_boxes() {
        val state = mutableStateOf("")
        rule.setContent {
            IChatTheme {
                CodeBoxes(value = state.value, onValueChange = { state.value = it }, isError = false)
            }
        }
        rule.onNodeWithTag("code-input").performTextReplacement("123456")
        assertEquals("123456", state.value)
    }

    @Test fun ignores_non_digit_input() {
        val state = mutableStateOf("")
        rule.setContent {
            IChatTheme {
                CodeBoxes(value = state.value, onValueChange = { state.value = it }, isError = false)
            }
        }
        rule.onNodeWithTag("code-input").performTextInput("a8b3")
        assertEquals("83", state.value)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run：`./gradlew :app:connectedDebugAndroidTest --tests "com.ichat.login.CodeBoxesTest"`

Expected：编译失败（`CodeBoxes` 未定义）。

- [ ] **Step 3: 实现 `CodeBoxes.kt`**

策略：用一个不可见的 `BasicTextField` 接收键盘输入，6 个 `Box` 显示当前值的对应位（焦点效果由值的长度推算）。这样自动跳焦、退格、粘贴全部由系统输入处理，不需要 6 个独立 TextField 联动。

```kotlin
package com.ichat.login.login.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ichat.login.theme.BgSurface
import com.ichat.login.theme.BorderInput
import com.ichat.login.theme.BorderInputFocus
import com.ichat.login.theme.CodeBox
import com.ichat.login.theme.CodeBoxShape
import com.ichat.login.theme.StateError

@Composable
fun CodeBoxes(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    Box(modifier = modifier.fillMaxWidth().height(48.dp)) {
        // 6 个可视格子
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(6) { i ->
                val ch = value.getOrNull(i)?.toString().orEmpty()
                val borderColor = when {
                    isError              -> StateError
                    i == value.length    -> BorderInputFocus
                    else                 -> BorderInput
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(BgSurface, CodeBoxShape)
                        .border(1.dp, borderColor, CodeBoxShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(ch, style = CodeBox)
                }
            }
        }
        // 不可见 TextField 接收输入
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.filter { c -> c.isDigit() }.take(6)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .alpha(0f)
                .testTag("code-input")
                .focusRequester(focus)
                .focusable(),
        )
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

Run：`./gradlew :app:connectedDebugAndroidTest --tests "com.ichat.login.CodeBoxesTest"`

Expected：3 tests passed。

- [ ] **Step 5: 提交**

```bash
git add UI/Android/app/src/main/kotlin/com/ichat/login/login/components/CodeBoxes.kt UI/Android/app/src/androidTest/kotlin/com/ichat/login/CodeBoxesTest.kt
git commit -m "feat(android): add CodeBoxes with auto-focus / paste support"
```

---

## Task 9: PhoneScreen

**Files:**
- Create: `UI/Android/app/src/main/kotlin/com/ichat/login/login/PhoneScreen.kt`

- [ ] **Step 1: 实现 `PhoneScreen.kt`**

```kotlin
package com.ichat.login.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ichat.login.login.components.PhoneInput
import com.ichat.login.login.components.PrimaryButton
import com.ichat.login.theme.BgPage
import com.ichat.login.theme.BgSurface
import com.ichat.login.theme.BodySm
import com.ichat.login.theme.Caption
import com.ichat.login.theme.TitleLg

@Composable
fun PhoneScreen(vm: LoginViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPage)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(Modifier.height(24.dp))
        // Logo
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(BgSurface),
            contentAlignment = Alignment.Center,
        ) { Text("💬") }

        Spacer(Modifier.height(20.dp))
        Text("欢迎使用 iChat", style = TitleLg)
        Spacer(Modifier.height(4.dp))
        Text("输入手机号，开启陪伴", style = BodySm, textAlign = TextAlign.Center)

        Spacer(Modifier.height(20.dp))
        PhoneInput(
            rawDigits = state.phone,
            onValueChange = vm::onPhoneChange,
            modifier = Modifier.testTag("phone-input"),
        )

        Spacer(Modifier.height(20.dp))
        PrimaryButton(
            text = if (state.isSubmitting) "发送中…" else "获取验证码",
            enabled = state.canRequestCode,
            modifier = Modifier.testTag("get-code-btn"),
            onClick = vm::requestCode,
        )

        if (state.errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(state.errorMessage!!, style = Caption.copy(color = com.ichat.login.theme.StateError))
        }

        Spacer(Modifier.weight(1f))
        Text("未注册手机号将自动创建账号", style = Caption)
    }
}
```

- [ ] **Step 2: 验证编译**

Run：`./gradlew :app:assembleDebug`

Expected：BUILD SUCCESSFUL。

- [ ] **Step 3: 提交**

```bash
git add UI/Android/app/src/main/kotlin/com/ichat/login/login/PhoneScreen.kt
git commit -m "feat(android): add PhoneScreen"
```

---

## Task 10: CodeScreen

**Files:**
- Create: `UI/Android/app/src/main/kotlin/com/ichat/login/login/CodeScreen.kt`

- [ ] **Step 1: 实现 `CodeScreen.kt`**

```kotlin
package com.ichat.login.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ichat.login.data.PhoneUtils
import com.ichat.login.login.components.CodeBoxes
import com.ichat.login.theme.BgPage
import com.ichat.login.theme.BodySm
import com.ichat.login.theme.BrandPrimary
import com.ichat.login.theme.Caption
import com.ichat.login.theme.StateError
import com.ichat.login.theme.TitleLg

@Composable
fun CodeScreen(vm: LoginViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPage)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        // 顶部返回栏
        Box(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                "‹",
                style = TitleLg,
                modifier = Modifier
                    .size(44.dp)
                    .clickable { vm.goBack() }
                    .testTag("back-btn"),
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(8.dp))
        Text("输入验证码", style = TitleLg)
        Spacer(Modifier.height(4.dp))
        Text("已发送至 +86 ${PhoneUtils.mask(state.phone)}", style = BodySm)

        Spacer(Modifier.height(24.dp))
        CodeBoxes(
            value = state.code,
            onValueChange = vm::onCodeChange,
            isError = state.errorMessage != null,
            modifier = Modifier.testTag("code-boxes"),
        )

        if (state.errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(state.errorMessage!!, style = Caption.copy(color = StateError))
        }

        Spacer(Modifier.height(16.dp))
        if (state.countdown > 0) {
            Text("${state.countdown}s 后重新发送", style = Caption.copy(color = BrandPrimary))
        } else {
            Text(
                "重新发送",
                style = Caption.copy(color = BrandPrimary),
                modifier = Modifier.clickable { vm.resendCode() }.testTag("resend-btn"),
            )
        }

        Spacer(Modifier.weight(1f))
        Text("收不到验证码？", style = Caption.copy(color = BrandPrimary))
    }
}
```

- [ ] **Step 2: 验证编译**

Run：`./gradlew :app:assembleDebug`

Expected：BUILD SUCCESSFUL。

- [ ] **Step 3: 提交**

```bash
git add UI/Android/app/src/main/kotlin/com/ichat/login/login/CodeScreen.kt
git commit -m "feat(android): add CodeScreen"
```

---

## Task 11: 导航 + MainActivity + Home 占位

**Files:**
- Create: `UI/Android/app/src/main/kotlin/com/ichat/login/home/HomePlaceholderScreen.kt`
- Create: `UI/Android/app/src/main/kotlin/com/ichat/login/login/LoginNavGraph.kt`
- Modify: `UI/Android/app/src/main/kotlin/com/ichat/login/MainActivity.kt`

- [ ] **Step 1: 创建 `HomePlaceholderScreen.kt`**

```kotlin
package com.ichat.login.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ichat.login.theme.BgPage
import com.ichat.login.theme.TitleLg

@Composable
fun HomePlaceholderScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(BgPage).testTag("home-screen"),
        contentAlignment = Alignment.Center,
    ) {
        Text("登录成功 · iChat", style = TitleLg)
    }
}
```

- [ ] **Step 2: 创建 `LoginNavGraph.kt`**

```kotlin
package com.ichat.login.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ichat.login.home.HomePlaceholderScreen
import kotlinx.coroutines.flow.collectLatest

object Routes {
    const val Phone = "phone"
    const val Code  = "code"
    const val Home  = "home"
}

@Composable
fun LoginNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    val vm: LoginViewModel = viewModel()
    val state by vm.state.collectAsState()

    // 监听登录成功事件
    LaunchedEffect(Unit) {
        vm.events.collectLatest { event ->
            if (event == LoginEvent.LoginSuccess) {
                navController.navigate(Routes.Home) {
                    popUpTo(Routes.Phone) { inclusive = true }
                }
            }
        }
    }

    // 跟随 phase 切屏
    LaunchedEffect(state.phase) {
        when (state.phase) {
            LoginPhase.Phone -> if (navController.currentDestination?.route != Routes.Phone) {
                navController.popBackStack(Routes.Phone, inclusive = false)
            }
            LoginPhase.Code  -> if (navController.currentDestination?.route != Routes.Code) {
                navController.navigate(Routes.Code)
            }
        }
    }

    NavHost(navController = navController, startDestination = Routes.Phone) {
        composable(Routes.Phone) { PhoneScreen(vm) }
        composable(Routes.Code)  { CodeScreen(vm)  }
        composable(Routes.Home)  { HomePlaceholderScreen() }
    }
}
```

- [ ] **Step 3: 更新 `MainActivity.kt`**

```kotlin
package com.ichat.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ichat.login.login.LoginNavGraph
import com.ichat.login.theme.IChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IChatTheme { LoginNavGraph() }
        }
    }
}
```

- [ ] **Step 4: 验证编译**

Run：`./gradlew :app:assembleDebug`

Expected：BUILD SUCCESSFUL。

- [ ] **Step 5: 在模拟器上跑一遍 happy path（手动）**

启动模拟器，安装 APK：

```bash
./gradlew :app:installDebug
adb shell am start -n com.ichat.login/.MainActivity
```

肉眼检查：
1. 进入 PhoneScreen，背景 `#F2F6FF`，按钮 disabled
2. 输入 `13800138000` → 按钮高亮
3. 点 "获取验证码" → 800ms 后切到 CodeScreen，倒计时 60s → 59s → ...
4. 输入 `123456` → 600ms 后跳到 HomePlaceholderScreen 显示 "登录成功 · iChat"
5. 重启 → 输入手机号 → 进 CodeScreen → 输 `000000` → 红边 + 错误提示 → 1.2s 后清空回第一格

- [ ] **Step 6: 提交**

```bash
git add UI/Android/app/src/main/kotlin/com/ichat/login
git commit -m "feat(android): wire navigation + Main + home placeholder"
```

---

## Task 12: 端到端测试（Espresso / Compose UI Test on activity）

**Files:**
- Create: `UI/Android/app/src/androidTest/kotlin/com/ichat/login/LoginE2ETest.kt`

覆盖 spec §6.1 测试用例 #1, #3, #5, #6, #8。

- [ ] **Step 1: 写 E2E 测试**

```kotlin
package com.ichat.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class LoginE2ETest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun happy_path_phone_to_code_to_home() {
        // 用例 #1：未满 11 位 → 按钮 disabled
        rule.onNodeWithTag("get-code-btn").assertIsNotEnabled()

        // 输入手机号
        rule.onNodeWithTag("phone-input").performTextInput("13800138000")
        rule.onNodeWithTag("get-code-btn").assertIsEnabled()

        // 用例 #3：发码 → 切到验证码屏
        rule.onNodeWithTag("get-code-btn").performClick()
        rule.waitUntil(timeoutMillis = 2_000) {
            rule.onAllNodesWithText("输入验证码").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("已发送至 +86 138****8000").assertIsDisplayed()

        // 用例 #5：输入正确验证码 → 跳主页
        rule.onNodeWithTag("code-input").performTextInput("123456")
        rule.waitUntil(timeoutMillis = 2_000) {
            rule.onAllNodesWithTag("home-screen").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("登录成功 · iChat").assertIsDisplayed()
    }

    @Test fun back_from_code_keeps_phone_and_countdown() {
        rule.onNodeWithTag("phone-input").performTextInput("13800138000")
        rule.onNodeWithTag("get-code-btn").performClick()
        rule.waitUntil(2_000) {
            rule.onAllNodesWithText("输入验证码").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("back-btn").performClick()
        // 用例 #6：返回后手机号保留
        rule.onNodeWithTag("get-code-btn").assertIsEnabled() // 11 位还在
    }

    @Test fun wrong_code_shows_error_then_clears() {
        rule.onNodeWithTag("phone-input").performTextInput("13800138000")
        rule.onNodeWithTag("get-code-btn").performClick()
        rule.waitUntil(2_000) {
            rule.onAllNodesWithText("输入验证码").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("code-input").performTextInput("000000")
        // 等待 600ms 校验完
        rule.waitUntil(2_000) {
            rule.onAllNodesWithText("验证码错误，请重新输入").fetchSemanticsNodes().isNotEmpty()
        }
        // 1.2s 后自动清空
        rule.waitUntil(2_000) {
            rule.onAllNodesWithText("验证码错误，请重新输入").fetchSemanticsNodes().isEmpty()
        }
    }
}
```

- [ ] **Step 2: 运行 E2E 测试**

Run（需 Android 模拟器在运行）：

```bash
cd UI/Android && ./gradlew :app:connectedDebugAndroidTest --tests "com.ichat.login.LoginE2ETest"
```

Expected：3 tests passed。

- [ ] **Step 3: 提交**

```bash
git add UI/Android/app/src/androidTest/kotlin/com/ichat/login/LoginE2ETest.kt
git commit -m "test(android): add E2E happy path + back + wrong code"
```

---

## Task 13: 跑完所有测试 + 视觉走查

- [ ] **Step 1: 跑全部单元测试**

```bash
cd UI/Android && ./gradlew :app:testDebugUnitTest
```

Expected：19 tests passed（PhoneUtilsTest 6 + MockAuthRepositoryTest 3 + LoginViewModelTest 10）。

- [ ] **Step 2: 跑全部 instrumentation 测试**

```bash
./gradlew :app:connectedDebugAndroidTest
```

Expected：8 tests passed（PrimaryButtonTest 2 + CodeBoxesTest 3 + LoginE2ETest 3）。

- [ ] **Step 3: 视觉走查（在模拟器上对照截图）**

启动 App，对照 `.superpowers/brainstorm/final-design.html` 的移动端三张图：

1. **PhoneScreen**：背景 `#F2F6FF`、Logo 圆形白底带阴影、标题"欢迎使用 iChat"、副标题"输入手机号，开启陪伴"、输入框圆角 14、按钮 pill 圆角 24、底部"未注册手机号将自动创建账号"
2. **CodeScreen 等待态**：左上返回箭头、6 个验证码格子（首格高亮蓝边）、副标题脱敏手机号、"60s 后重新发送"
3. **CodeScreen 错误态**：6 格全部红边、下方"验证码错误，请重新输入"

任意视觉差异（颜色色值、圆角、间距）回到 `theme/Color.kt`、`theme/Shape.kt` 或对应 Screen 调整后重新跑测试再次走查。

- [ ] **Step 4: 最终提交（如有视觉调整）**

```bash
git add -A
git commit -m "chore(android): visual walkthrough adjustments"
```

否则跳过此步。

---

## Done When

- [ ] 所有单元测试通过（18 个）
- [ ] 所有 UI / E2E 测试通过（8 个）
- [ ] 模拟器手动走 happy path：启动 → 输入 `13800138000` → 获取验证码 → 输入 `123456` → 进入"登录成功"占位
- [ ] 视觉与 spec §4 token 一致（肉眼对比无明显差异）
- [ ] 性能：从 launcher 点击图标到 PhoneScreen 可交互 ≤ 2s（在中端设备 / API 30 模拟器）

---

## 已知不在第一期范围（Out of Scope，参见 spec §1.3）

- "收不到验证码？" 链接为静态文字，无点击行为
- 真实后端联调
- SMS Retriever API 实际接入（结构留好，数据来自 mock）
- 暗色模式
- 横屏 / 平板布局
- 启动隐私协议弹窗
