# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目简介

iChat 是一款专注于陪伴的社交应用（详见 `README.md`）。目标覆盖五个平台：**iOS、Android、macOS、Windows、Linux**。

## UI 层技术栈

各平台 UI 用各自的原生 / 首选技术，**不采用统一的跨平台 UI 框架**。三端共享同一套设计 token（颜色 / 圆角 / 字号 / 间距，参见 spec §4），但代码独立：

| 平台 | UI 技术 | 工程位置 |
| --- | --- | --- |
| Android | Kotlin · Jetpack Compose · Gradle | `UI/Android/` |
| iOS | Swift · SwiftUI · xcodegen | `UI/iOS/` |
| macOS / Windows / Linux | **Qt 6 + C++17 Widgets**（不用 QML）· CMake | `UI/Desktop/` |

**Qt 用 C++ 不用 QML** 是项目级决策，长期可维护性优先；任何"用 QML 更容易"的视觉效果（如 backdrop blur）应主动降级为 Widgets 可实现的近似形式。

跨平台共享层（业务逻辑 / 网络 / 数据模型）**尚未引入**——三端各自维护一份 ViewModel/Store/Controller，方法名和状态字段刻意对齐（`requestCode / verifyCode / goBack`、`phase / phone / code / countdown / errorMessage / isSubmitting`），便于未来抽共享层时低成本迁移。

## 当前实现状态

| 平台 | 状态 | 测试 |
| --- | --- | --- |
| Android | ✅ 第一期登录页 + launcher icon 完成 | 20 单测 + 8 instrumentation/E2E |
| iOS | ✅ 第一期登录页 + AppIcon 完成 | 20 XCTest + 3 XCUITest |
| Desktop (Qt) | ⏳ 未开始（plan 已写） | — |

第一期登录功能：手机号 + 短信验证码，**仅 +86**，登录即注册（首次手机号自动建账号），mock 后端校验码 `123456` 通过、其他返回错误。

## 文档约定

- **设计 spec**：`docs/superpowers/specs/2026-05-07-login-screen-design.md` — 信息架构、视觉 token、状态机、边界处理、测试策略
- **实现 plan**（每端一份）：
  - `docs/superpowers/plans/2026-05-07-login-android.md`
  - `docs/superpowers/plans/2026-05-07-login-ios.md`
  - `docs/superpowers/plans/2026-05-07-login-desktop.md`
- 视觉 mockup（不入 git，路径 `.superpowers/brainstorm/`）：登录界面方向、桌面布局、应用图标方案

## 常用命令

### Android（`UI/Android/`）

```bash
# 必须先设置 JAVA_HOME（系统 JDK 是 11，AGP 8.7 需要 17+；Android Studio 自带 JDK 21）
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

# 构建
./gradlew :app:assembleDebug

# 全部单元测试
./gradlew :app:testDebugUnitTest

# 单个测试类（unit）
./gradlew :app:testDebugUnitTest --tests "com.ichat.login.LoginViewModelTest"

# 全部 instrumentation 测试（需运行中模拟器）
./gradlew :app:connectedDebugAndroidTest

# 单个 instrumentation 测试类（注意：不能用 --tests 过滤，要用 -P 参数）
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.ichat.login.LoginE2ETest
```

启动 Android 模拟器：`$ANDROID_HOME/emulator/emulator -avd Pixel_9 -no-snapshot-save -gpu host &`

### iOS（`UI/iOS/`）

工程文件 `iChatLogin.xcodeproj/` 由 xcodegen 从 `project.yml` 生成，**不入 git**。新增源目录后需要重生成。

```bash
# 重新生成 .xcodeproj（每次新增目录都跑）
xcodegen generate

# 构建
xcodebuild -scheme iChatLogin -destination 'platform=iOS Simulator,name=iPhone 17 Pro' build

# 全部测试（unit + UI）
xcodebuild test -scheme iChatLogin -destination 'platform=iOS Simulator,name=iPhone 17 Pro'

# 单个测试类
xcodebuild test -scheme iChatLogin \
  -destination 'platform=iOS Simulator,name=iPhone 17 Pro' \
  -only-testing:iChatLoginTests/LoginStoreTests
```

启动 iPhone 模拟器：`xcrun simctl boot "iPhone 17 Pro" && open -a Simulator`

### Desktop / Qt（`UI/Desktop/`，待实现）

未开始；按 plan 走 CMake：

```bash
cmake -S . -B build -DCMAKE_PREFIX_PATH=$(brew --prefix qt6)
cmake --build build -j
ctest --test-dir build --output-on-failure
./build/iChatDesktop
```

## 跨端命名对齐（重要）

三端的状态控制器命名必须一致——这是为未来抽共享层做的预投资：

| 概念 | Android | iOS | Desktop |
| --- | --- | --- | --- |
| 控制器类 | `LoginViewModel` | `LoginStore` (`ObservableObject`) | `LoginController` (`QObject`) |
| 公开方法 | 三端均为：`onPhoneChange / onCodeChange / requestCode / resendCode / verifyCode / goBack` |
| 状态字段 | 三端均为：`phase / phone / code / countdown / errorMessage / isSubmitting` |

修改任一端时，如果调整了方法签名/状态字段，**必须同步评估另外两端**，避免命名漂移。

## 大坑/易踩雷点

- **Android：AGP 8.7 要求 Gradle ≥ 8.9**（`gradle-wrapper.properties` 里 8.7 会报 "Minimum supported Gradle version is 8.9"）
- **Android：Compose BOM 2024.12 拉的 Espresso 3.5.0 在 API 37 模拟器崩溃**（`NoSuchMethodException: InputManager.getInstance`），必须显式声明 `androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")`
- **Android：`connectedDebugAndroidTest` 不接受 `--tests` 过滤**，要用 `-Pandroid.testInstrumentationRunnerArguments.class=<FQN>`
- **iOS：xcodegen 生成的 test target 必须加 `GENERATE_INFOPLIST_FILE: YES`** 否则签名失败
- **iOS：Xcode 26 不再支持 iOS 16 deployment target**（项目用 iOS 17）
- **iOS：XCUITest 无法 `typeText` 到 `opacity 0.001` 的隐藏 TextField**，要点屏幕键盘 `app.keys[digit].tap()` 模拟
- **简洁优先**：用户对设计/实现选项的取舍，永远偏好更简洁的那个，避免冗余确认/堆叠功能（参见 memory `feedback_design_philosophy.md`）

## 设计哲学（来自 user feedback）

- **整体追求简洁**：能简则简，避免冗余确认 / 双保险式方案 / 信息密集型设计
- **每屏聚焦一个动作**：登录两屏（手机号 → 验证码）而非单页填一切
- **登录即注册**：未注册手机号自动创建账号，没有独立"注册"入口
