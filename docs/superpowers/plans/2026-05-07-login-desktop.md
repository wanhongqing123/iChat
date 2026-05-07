# iChat 登录页 · Desktop（Qt 6 / C++）实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 `UI/Desktop/` 落地 iChat 第一期桌面端登录页（macOS / Windows / Linux 共用一套源码）：双栏 Hero 布局，左侧渐变品牌 + 右侧两阶段表单，mock 校验后切到主页占位。

**Architecture:** Qt 6 + C++17 + CMake，全 Qt Widgets（**不使用 QML**，按项目级别决策）。`LoginController : QObject` 持有状态、`MockAuthService : QObject` 用 `QTimer::singleShot` 模拟网络延迟，`signals/slots` 驱动 UI。Hero 渐变用 `QPainter::QLinearGradient` 在 `paintEvent` 中绘制；圆角和 hover 态用 `QSS` 样式表。

**Tech Stack:** Qt 6.5+ · C++17 · CMake 3.21+ · Qt Widgets · Qt Test · macOS / Windows / Linux

**Spec:** `docs/superpowers/specs/2026-05-07-login-screen-design.md`

**Working dir:** `UI/Desktop/`（从空目录开始）

---

## 文件结构

```
UI/Desktop/
├── CMakeLists.txt                 # 顶层
├── src/
│   ├── main.cpp
│   ├── theme/
│   │   ├── Tokens.h               # 所有色值 / 字号 / 间距常量
│   │   └── Style.h/.cpp           # 全局 QSS 字符串生成
│   ├── data/
│   │   ├── PhoneUtils.h/.cpp      # format / mask / isValid（无 QObject 依赖）
│   │   ├── AuthResult.h           # enum + struct
│   │   └── MockAuthService.h/.cpp # QObject，QTimer 模拟延迟
│   ├── login/
│   │   ├── LoginController.h/.cpp # QObject 状态机
│   │   ├── LoginWindow.h/.cpp     # QMainWindow，左右两栏
│   │   ├── HeroPanel.h/.cpp       # QWidget 自绘渐变 + 文案
│   │   ├── PhoneForm.h/.cpp       # QStackedWidget：phone / code
│   │   └── components/
│   │       ├── PrimaryButton.h/.cpp
│   │       ├── PhoneInput.h/.cpp
│   │       └── CodeBoxes.h/.cpp
│   └── home/
│       └── HomePlaceholderWidget.h/.cpp
└── tests/
    ├── CMakeLists.txt
    ├── PhoneUtilsTest.cpp
    ├── MockAuthServiceTest.cpp
    ├── LoginControllerTest.cpp
    └── LoginE2ETest.cpp
```

---

## Task 1: Scaffold CMake + Qt 工程

**Files:**
- Create: `UI/Desktop/CMakeLists.txt`
- Create: `UI/Desktop/src/main.cpp`

- [ ] **Step 1: 创建顶层 `CMakeLists.txt`**

```cmake
cmake_minimum_required(VERSION 3.21)
project(iChatDesktop LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTORCC ON)
set(CMAKE_AUTOUIC ON)

find_package(Qt6 6.5 REQUIRED COMPONENTS Widgets Test)

set(APP_SOURCES
    src/main.cpp
)

add_executable(iChatDesktop ${APP_SOURCES})
target_link_libraries(iChatDesktop PRIVATE Qt6::Widgets)
target_include_directories(iChatDesktop PRIVATE src)

enable_testing()
add_subdirectory(tests)
```

- [ ] **Step 2: 创建空的 `tests/CMakeLists.txt`**（占位，后续 Task 添加测试时填）

```cmake
# tests will be added incrementally
```

- [ ] **Step 3: 创建最小 `main.cpp`**

```cpp
#include <QApplication>
#include <QLabel>
#include <QMainWindow>

int main(int argc, char *argv[]) {
    QApplication app(argc, argv);
    QMainWindow w;
    w.setWindowTitle("iChat");
    w.resize(1080, 680);
    auto *label = new QLabel("Hello iChat", &w);
    label->setAlignment(Qt::AlignCenter);
    w.setCentralWidget(label);
    w.show();
    return app.exec();
}
```

- [ ] **Step 4: 配置并构建**

Run（在 `UI/Desktop/`）：

```bash
cmake -S . -B build -DCMAKE_PREFIX_PATH=$(brew --prefix qt6 2>/dev/null || echo /usr/local/qt6)
cmake --build build -j
```

注：Linux 用 `apt install qt6-base-dev`，Windows 用 `qt-online-installer` 安装 Qt 6.5+ 后将路径传入 `CMAKE_PREFIX_PATH`。

Expected：`build/iChatDesktop` 可执行文件生成。

- [ ] **Step 5: 运行验证**

```bash
./build/iChatDesktop
```

Expected：弹出 1080×680 窗口，标题 iChat，居中 "Hello iChat"。关闭窗口即可。

- [ ] **Step 6: 提交**

```bash
git add UI/Desktop/CMakeLists.txt UI/Desktop/src/main.cpp UI/Desktop/tests/CMakeLists.txt
git commit -m "feat(desktop): scaffold Qt6 + CMake project"
```

---

## Task 2: 设计 token 与全局样式

**Files:**
- Create: `UI/Desktop/src/theme/Tokens.h`
- Create: `UI/Desktop/src/theme/Style.h`
- Create: `UI/Desktop/src/theme/Style.cpp`
- Modify: `UI/Desktop/CMakeLists.txt`、`UI/Desktop/src/main.cpp`

- [ ] **Step 1: 创建 `Tokens.h`**

```cpp
#pragma once
#include <QColor>
#include <QString>

namespace tokens {
    // Colors
    inline const QColor BrandPrimary         = QColor("#4F86FF");
    inline const QColor BrandPrimaryPressed  = QColor("#3B6BE0");
    inline const QColor BrandPrimaryDisabled = QColor("#B7CDFA");

    inline const QColor BgPage    = QColor("#F2F6FF");
    inline const QColor BgSurface = QColor("#FFFFFF");

    inline const QColor BorderInput      = QColor("#DCE6FA");
    inline const QColor BorderInputFocus = BrandPrimary;

    inline const QColor TextPrimary   = QColor("#1A2233");
    inline const QColor TextSecondary = QColor("#7884A3");
    inline const QColor TextTertiary  = QColor("#9AA3B5");

    inline const QColor StateError = QColor("#E5484D");

    inline const QColor HeroFrom = QColor("#4F86FF");
    inline const QColor HeroTo   = QColor("#3B6BE0");

    // Sizes
    inline constexpr int InputHeight  = 48;
    inline constexpr int ButtonHeight = 48;
    inline constexpr int CodeBoxSize  = 48;
    inline constexpr int FormWidth    = 320;

    // Radii
    inline constexpr int InputRadius   = 14;
    inline constexpr int ButtonRadius  = 24;
    inline constexpr int CodeBoxRadius = 12;
}
```

- [ ] **Step 2: 创建 `Style.h`**

```cpp
#pragma once
#include <QString>

namespace style {
    /// 返回应用顶层 QSS（按钮 / 输入框 / 标签的全局样式）
    QString applicationQss();
}
```

- [ ] **Step 3: 创建 `Style.cpp`**

```cpp
#include "theme/Style.h"
#include "theme/Tokens.h"

QString style::applicationQss() {
    return QString(R"(
        QWidget#PageRoot { background-color: %1; }

        QLineEdit.iChatInput {
            background-color: %2;
            border: 1px solid %3;
            border-radius: %4px;
            padding: 0 16px;
            color: %5;
            font-size: 14px;
        }
        QLineEdit.iChatInput:focus { border: 1px solid %6; }
        QLineEdit.iChatInput.error  { border: 1px solid %7; }

        QPushButton.iChatPrimary {
            background-color: %8;
            color: %2;
            border: none;
            border-radius: %9px;
            font-size: 14px;
            font-weight: 600;
        }
        QPushButton.iChatPrimary:hover    { background-color: %10; }
        QPushButton.iChatPrimary:pressed  { background-color: %10; }
        QPushButton.iChatPrimary:disabled { background-color: %11; }

        QLabel.iChatTitle     { color: %5; font-size: 20px; font-weight: 600; }
        QLabel.iChatSubtitle  { color: %12; font-size: 13px; }
        QLabel.iChatCaption   { color: %13; font-size: 12px; }
        QLabel.iChatError     { color: %7;  font-size: 12px; }
        QLabel.iChatLink      { color: %8;  font-size: 12px; }
    )")
    .arg(tokens::BgPage.name())          // %1
    .arg(tokens::BgSurface.name())       // %2
    .arg(tokens::BorderInput.name())     // %3
    .arg(tokens::InputRadius)            // %4
    .arg(tokens::TextPrimary.name())     // %5
    .arg(tokens::BorderInputFocus.name())// %6
    .arg(tokens::StateError.name())      // %7
    .arg(tokens::BrandPrimary.name())    // %8
    .arg(tokens::ButtonRadius)           // %9
    .arg(tokens::BrandPrimaryPressed.name()) // %10
    .arg(tokens::BrandPrimaryDisabled.name())// %11
    .arg(tokens::TextSecondary.name())   // %12
    .arg(tokens::TextTertiary.name());   // %13
}
```

- [ ] **Step 4: 更新 `CMakeLists.txt` 添加这些源文件**

把 `APP_SOURCES` 改为：

```cmake
set(APP_SOURCES
    src/main.cpp
    src/theme/Style.cpp
)
```

- [ ] **Step 5: 在 `main.cpp` 应用 QSS**

```cpp
#include <QApplication>
#include <QLabel>
#include <QMainWindow>
#include "theme/Style.h"

int main(int argc, char *argv[]) {
    QApplication app(argc, argv);
    app.setStyleSheet(style::applicationQss());

    QMainWindow w;
    w.setWindowTitle("iChat");
    w.resize(1080, 680);
    auto *root = new QWidget(&w);
    root->setObjectName("PageRoot");
    auto *label = new QLabel("iChat", root);
    label->setAlignment(Qt::AlignCenter);
    label->setProperty("class", "iChatTitle");
    auto *layout = new QVBoxLayout(root);
    layout->addWidget(label);
    w.setCentralWidget(root);
    w.show();
    return app.exec();
}
```

补 `#include <QVBoxLayout>` 到 `main.cpp` 顶部。

- [ ] **Step 6: 构建并验证**

```bash
cmake --build build -j && ./build/iChatDesktop
```

Expected：窗口背景 `#F2F6FF`，居中显示 "iChat"。

- [ ] **Step 7: 提交**

```bash
git add UI/Desktop
git commit -m "feat(desktop): add design tokens + global QSS"
```

---

## Task 3: PhoneUtils（TDD）

**Files:**
- Create: `UI/Desktop/tests/PhoneUtilsTest.cpp`
- Create: `UI/Desktop/src/data/PhoneUtils.h`
- Create: `UI/Desktop/src/data/PhoneUtils.cpp`
- Modify: `UI/Desktop/tests/CMakeLists.txt`、`UI/Desktop/CMakeLists.txt`

- [ ] **Step 1: 写失败测试 `PhoneUtilsTest.cpp`**

```cpp
#include <QtTest>
#include "data/PhoneUtils.h"

class PhoneUtilsTest : public QObject {
    Q_OBJECT
private slots:

    void digitsOnly_removesNonDigits() {
        QCOMPARE(PhoneUtils::digitsOnly("138 0013 8000"), QString("13800138000"));
        QCOMPARE(PhoneUtils::digitsOnly("138-0013-8000"), QString("13800138000"));
        QCOMPARE(PhoneUtils::digitsOnly("abc"),           QString(""));
    }

    void format_appliesGroupedSpacing() {
        QCOMPARE(PhoneUtils::format(""),              QString(""));
        QCOMPARE(PhoneUtils::format("1"),             QString("1"));
        QCOMPARE(PhoneUtils::format("138"),           QString("138"));
        QCOMPARE(PhoneUtils::format("1380"),          QString("138 0"));
        QCOMPARE(PhoneUtils::format("13800013"),      QString("138 0013"));
        QCOMPARE(PhoneUtils::format("138001380"),     QString("138 0013 8"));
        QCOMPARE(PhoneUtils::format("13800138000"),   QString("138 0013 8000"));
    }

    void format_truncatesAt11Digits() {
        QCOMPARE(PhoneUtils::format("13800138000999"), QString("138 0013 8000"));
    }

    void isValid_acceptsValidChineseMobile() {
        QVERIFY(PhoneUtils::isValid("13800138000"));
        QVERIFY(PhoneUtils::isValid("15912345678"));
        QVERIFY(PhoneUtils::isValid("18800001111"));
    }

    void isValid_rejectsInvalid() {
        QVERIFY(!PhoneUtils::isValid(""));
        QVERIFY(!PhoneUtils::isValid("1380013800"));
        QVERIFY(!PhoneUtils::isValid("138001380001"));
        QVERIFY(!PhoneUtils::isValid("00000000000"));
        QVERIFY(!PhoneUtils::isValid("12345678901"));
        QVERIFY(!PhoneUtils::isValid("1a800138000"));
    }

    void mask_hidesMiddleFour() {
        QCOMPARE(PhoneUtils::mask("13800138000"), QString("138****8000"));
    }

    void mask_returnsAsIs_whenNot11Digits() {
        QCOMPARE(PhoneUtils::mask(""),      QString(""));
        QCOMPARE(PhoneUtils::mask("12345"), QString("12345"));
    }
};

QTEST_GUILESS_MAIN(PhoneUtilsTest)
#include "PhoneUtilsTest.moc"
```

- [ ] **Step 2: 更新 `tests/CMakeLists.txt`**

```cmake
add_executable(PhoneUtilsTest PhoneUtilsTest.cpp ../src/data/PhoneUtils.cpp)
target_include_directories(PhoneUtilsTest PRIVATE ../src)
target_link_libraries(PhoneUtilsTest PRIVATE Qt6::Test)
add_test(NAME PhoneUtilsTest COMMAND PhoneUtilsTest)
```

- [ ] **Step 3: 运行确认编译失败**

```bash
cmake --build build -j 2>&1 | tail -20
```

Expected：`PhoneUtils.h not found` 或 `PhoneUtils.cpp` 未实现。

- [ ] **Step 4: 实现 `PhoneUtils.h`**

```cpp
#pragma once
#include <QString>

namespace PhoneUtils {
    QString digitsOnly(const QString& input);
    QString format(const QString& raw);
    bool    isValid(const QString& phone);
    QString mask(const QString& phone);
}
```

- [ ] **Step 5: 实现 `PhoneUtils.cpp`**

```cpp
#include "data/PhoneUtils.h"

QString PhoneUtils::digitsOnly(const QString& input) {
    QString out; out.reserve(input.size());
    for (const QChar& c : input) if (c.isDigit()) out.append(c);
    return out;
}

QString PhoneUtils::format(const QString& raw) {
    QString d = digitsOnly(raw).left(11);
    if (d.size() <= 3) return d;
    if (d.size() <= 7) return d.left(3) + " " + d.mid(3);
    return d.left(3) + " " + d.mid(3, 4) + " " + d.mid(7);
}

bool PhoneUtils::isValid(const QString& phone) {
    QString d = digitsOnly(phone);
    if (d.size() != 11) return false;
    if (d[0] != '1') return false;
    QChar second = d[1];
    return second >= '3' && second <= '9';
}

QString PhoneUtils::mask(const QString& phone) {
    QString d = digitsOnly(phone);
    if (d.size() != 11) return d;
    return d.left(3) + "****" + d.right(4);
}
```

- [ ] **Step 6: 构建并跑测试**

```bash
cmake --build build -j
ctest --test-dir build -R PhoneUtilsTest --output-on-failure
```

Expected：`PhoneUtilsTest` PASS（7 个 slot 全部 pass）。

- [ ] **Step 7: 提交**

```bash
git add UI/Desktop/src/data/PhoneUtils.h UI/Desktop/src/data/PhoneUtils.cpp UI/Desktop/tests/PhoneUtilsTest.cpp UI/Desktop/tests/CMakeLists.txt
git commit -m "feat(desktop): add PhoneUtils with QtTest tests"
```

---

## Task 4: AuthResult + MockAuthService（TDD）

**Files:**
- Create: `UI/Desktop/src/data/AuthResult.h`
- Create: `UI/Desktop/src/data/MockAuthService.h`
- Create: `UI/Desktop/src/data/MockAuthService.cpp`
- Create: `UI/Desktop/tests/MockAuthServiceTest.cpp`
- Modify: `UI/Desktop/tests/CMakeLists.txt`、`UI/Desktop/CMakeLists.txt`

- [ ] **Step 1: 创建 `AuthResult.h`**

```cpp
#pragma once
#include <QString>

enum class AuthOutcome {
    RequestSuccess,
    VerifySuccess,
    InvalidCode,
    NetworkError,
};

struct AuthResult {
    AuthOutcome outcome;
    QString token;        // VerifySuccess 时有效
    bool isNewUser = false;
};
```

- [ ] **Step 2: 写失败测试 `MockAuthServiceTest.cpp`**

```cpp
#include <QtTest>
#include <QSignalSpy>
#include "data/MockAuthService.h"

class MockAuthServiceTest : public QObject {
    Q_OBJECT
private slots:

    void requestCode_emitsRequestSuccess() {
        MockAuthService svc;
        svc.setRequestDelayMs(10);   // 加速测试
        QSignalSpy spy(&svc, &MockAuthService::requestCodeFinished);
        svc.requestCode("13800138000");
        QVERIFY(spy.wait(500));
        const auto args = spy.takeFirst();
        const auto result = args.at(0).value<AuthResult>();
        QCOMPARE(result.outcome, AuthOutcome::RequestSuccess);
    }

    void verifyCode_correct_emitsVerifySuccess() {
        MockAuthService svc;
        svc.setVerifyDelayMs(10);
        QSignalSpy spy(&svc, &MockAuthService::verifyCodeFinished);
        svc.verifyCode("13800138000", "123456");
        QVERIFY(spy.wait(500));
        const auto result = spy.takeFirst().at(0).value<AuthResult>();
        QCOMPARE(result.outcome, AuthOutcome::VerifySuccess);
        QCOMPARE(result.token, QString("mock-token"));
        QCOMPARE(result.isNewUser, false);
    }

    void verifyCode_wrong_emitsInvalidCode() {
        MockAuthService svc;
        svc.setVerifyDelayMs(10);
        QSignalSpy spy(&svc, &MockAuthService::verifyCodeFinished);
        svc.verifyCode("13800138000", "000000");
        QVERIFY(spy.wait(500));
        QCOMPARE(spy.takeFirst().at(0).value<AuthResult>().outcome, AuthOutcome::InvalidCode);
    }
};

Q_DECLARE_METATYPE(AuthResult)

QTEST_MAIN(MockAuthServiceTest)
#include "MockAuthServiceTest.moc"
```

- [ ] **Step 3: 实现 `MockAuthService.h`**

```cpp
#pragma once
#include <QObject>
#include "data/AuthResult.h"

class MockAuthService : public QObject {
    Q_OBJECT
public:
    explicit MockAuthService(QObject* parent = nullptr);

    void setRequestDelayMs(int ms) { requestDelayMs_ = ms; }
    void setVerifyDelayMs(int ms)  { verifyDelayMs_  = ms; }

public slots:
    /// 异步：requestDelayMs 后 emit requestCodeFinished({RequestSuccess})。
    /// 子类可 override 以注入失败。
    virtual void requestCode(const QString& phone);

    /// 异步：verifyDelayMs 后 emit verifyCodeFinished({VerifySuccess|InvalidCode})。
    virtual void verifyCode(const QString& phone, const QString& code);

signals:
    void requestCodeFinished(AuthResult result);
    void verifyCodeFinished(AuthResult result);

private:
    int requestDelayMs_ = 800;
    int verifyDelayMs_  = 600;
};
```

- [ ] **Step 4: 实现 `MockAuthService.cpp`**

```cpp
#include "data/MockAuthService.h"
#include <QTimer>

MockAuthService::MockAuthService(QObject* parent) : QObject(parent) {
    qRegisterMetaType<AuthResult>("AuthResult");
}

void MockAuthService::requestCode(const QString& /*phone*/) {
    QTimer::singleShot(requestDelayMs_, this, [this] {
        emit requestCodeFinished(AuthResult{AuthOutcome::RequestSuccess, {}, false});
    });
}

void MockAuthService::verifyCode(const QString& /*phone*/, const QString& code) {
    QTimer::singleShot(verifyDelayMs_, this, [this, code] {
        AuthResult r;
        if (code == "123456") {
            r.outcome = AuthOutcome::VerifySuccess;
            r.token = "mock-token";
            r.isNewUser = false;
        } else {
            r.outcome = AuthOutcome::InvalidCode;
        }
        emit verifyCodeFinished(r);
    });
}
```

- [ ] **Step 5: 更新 `tests/CMakeLists.txt`**

```cmake
add_executable(PhoneUtilsTest
    PhoneUtilsTest.cpp
    ../src/data/PhoneUtils.cpp
)
target_include_directories(PhoneUtilsTest PRIVATE ../src)
target_link_libraries(PhoneUtilsTest PRIVATE Qt6::Test)
add_test(NAME PhoneUtilsTest COMMAND PhoneUtilsTest)

add_executable(MockAuthServiceTest
    MockAuthServiceTest.cpp
    ../src/data/MockAuthService.cpp
)
target_include_directories(MockAuthServiceTest PRIVATE ../src)
target_link_libraries(MockAuthServiceTest PRIVATE Qt6::Test Qt6::Core)
add_test(NAME MockAuthServiceTest COMMAND MockAuthServiceTest)
```

- [ ] **Step 6: 更新顶层 `CMakeLists.txt` 把新源文件纳入 app**

```cmake
set(APP_SOURCES
    src/main.cpp
    src/theme/Style.cpp
    src/data/PhoneUtils.cpp
    src/data/MockAuthService.cpp
)
```

- [ ] **Step 7: 构建并跑测试**

```bash
cmake --build build -j
ctest --test-dir build -R MockAuthServiceTest --output-on-failure
```

Expected：3 tests pass。

- [ ] **Step 8: 提交**

```bash
git add UI/Desktop
git commit -m "feat(desktop): add MockAuthService with QSignalSpy tests"
```

---

## Task 5: LoginController 状态机（TDD）

**Files:**
- Create: `UI/Desktop/src/login/LoginController.h`
- Create: `UI/Desktop/src/login/LoginController.cpp`
- Create: `UI/Desktop/tests/LoginControllerTest.cpp`
- Modify: 顶层 `CMakeLists.txt`、`tests/CMakeLists.txt`

覆盖 spec §6.1 测试用例 #1, #2, #3, #4, #5, #6, #7, #8。

- [ ] **Step 1: 写失败测试 `LoginControllerTest.cpp`**

```cpp
#include <QtTest>
#include <QSignalSpy>
#include "login/LoginController.h"

class LoginControllerTest : public QObject {
    Q_OBJECT
private slots:
    void initTestCase()      { qRegisterMetaType<AuthResult>("AuthResult"); }

    void phoneTooShort_canRequestCodeFalse() {
        LoginController c;
        c.onPhoneChange("1380013");
        QVERIFY(!c.canRequestCode());
    }

    void phoneInvalidFormat_canRequestCodeFalse() {
        LoginController c;
        c.onPhoneChange("00000000000");
        QVERIFY(!c.canRequestCode());
    }

    void phoneValid_canRequestCodeTrue() {
        LoginController c;
        c.onPhoneChange("13800138000");
        QVERIFY(c.canRequestCode());
    }

    void requestCode_movesToCodePhaseAndStartsCountdown() {
        LoginController c;
        c.service()->setRequestDelayMs(10);
        c.setCountdownIntervalMs(10);
        QSignalSpy phaseSpy(&c, &LoginController::phaseChanged);
        c.onPhoneChange("13800138000");
        c.requestCode();
        QVERIFY(phaseSpy.wait(500));
        QCOMPARE(c.phase(), LoginPhase::Code);
        QCOMPARE(c.countdown(), 60);
    }

    void countdown_decrementsOverTime() {
        LoginController c;
        c.service()->setRequestDelayMs(1);
        c.setCountdownIntervalMs(10);
        c.onPhoneChange("13800138000");
        c.requestCode();
        QTRY_COMPARE(c.phase(), LoginPhase::Code);
        QTRY_VERIFY(c.countdown() <= 57);   // tick=10ms，几次 tick 后应当 ≤ 57
        QVERIFY(c.countdown() >= 50);
    }

    void verifyCode_correct_emitsLoginSuccess() {
        LoginController c;
        c.service()->setRequestDelayMs(1);
        c.service()->setVerifyDelayMs(1);
        c.setCountdownIntervalMs(10);
        QSignalSpy success(&c, &LoginController::loginSucceeded);
        c.onPhoneChange("13800138000");
        c.requestCode();
        QTRY_COMPARE(c.phase(), LoginPhase::Code);
        c.onCodeChange("123456");
        QVERIFY(success.wait(500));
    }

    void verifyCode_wrong_setsErrorThenClears() {
        LoginController c;
        c.service()->setRequestDelayMs(1);
        c.service()->setVerifyDelayMs(1);
        c.setCountdownIntervalMs(10);
        c.setErrorClearDelayMs(50);
        c.onPhoneChange("13800138000");
        c.requestCode();
        QTRY_COMPARE(c.phase(), LoginPhase::Code);
        c.onCodeChange("000000");
        QTRY_COMPARE(c.errorMessage(), QString("验证码错误，请重新输入"));
        QTRY_VERIFY(c.errorMessage().isEmpty());
        QCOMPARE(c.code(), QString(""));
    }

    void goBack_preservesPhoneAndCountdown() {
        LoginController c;
        c.service()->setRequestDelayMs(1);
        c.setCountdownIntervalMs(10);
        c.onPhoneChange("13800138000");
        c.requestCode();
        QTRY_COMPARE(c.phase(), LoginPhase::Code);
        QTest::qWait(50);
        const int saved = c.countdown();
        c.goBack();
        QCOMPARE(c.phase(), LoginPhase::Phone);
        QCOMPARE(c.phone(), QString("13800138000"));
        QCOMPARE(c.countdown(), saved);
    }

    void requestCode_duringCountdown_skipsRefetchAndReentersCode() {
        LoginController c;
        c.service()->setRequestDelayMs(1);
        c.setCountdownIntervalMs(10);
        QSignalSpy reqSpy(c.service(), &MockAuthService::requestCodeFinished);
        c.onPhoneChange("13800138000");
        c.requestCode();
        QVERIFY(reqSpy.wait(200));
        c.goBack();
        c.requestCode();
        QCOMPARE(c.phase(), LoginPhase::Code);
        // 没有再次发码：reqSpy.count() 仍为 1
        QTest::qWait(20);
        QCOMPARE(reqSpy.count(), 1);
    }
};

QTEST_MAIN(LoginControllerTest)
#include "LoginControllerTest.moc"
```

- [ ] **Step 2: 实现 `LoginController.h`**

```cpp
#pragma once
#include <QObject>
#include <QString>
#include <QTimer>
#include "data/MockAuthService.h"

enum class LoginPhase { Phone, Code };

class LoginController : public QObject {
    Q_OBJECT
public:
    explicit LoginController(QObject* parent = nullptr);

    LoginPhase phase()        const { return phase_; }
    QString    phone()        const { return phone_; }
    QString    code()         const { return code_; }
    int        countdown()    const { return countdown_; }
    bool       isSubmitting() const { return isSubmitting_; }
    QString    errorMessage() const { return errorMessage_; }
    bool       canRequestCode() const;

    MockAuthService* service() { return &service_; }
    void setCountdownIntervalMs(int ms) { countdownIntervalMs_ = ms; }
    void setErrorClearDelayMs(int ms)   { errorClearDelayMs_ = ms; }

public slots:
    void onPhoneChange(const QString& raw);
    void onCodeChange(const QString& raw);
    void requestCode();
    void resendCode();
    void goBack();

signals:
    void phaseChanged(LoginPhase phase);
    void phoneChanged(const QString& phone);
    void codeChanged(const QString& code);
    void countdownChanged(int seconds);
    void errorMessageChanged(const QString& message);
    void isSubmittingChanged(bool submitting);
    void loginSucceeded();

private slots:
    void onRequestCodeFinished(AuthResult r);
    void onVerifyCodeFinished(AuthResult r);

private:
    void setPhase(LoginPhase p);
    void setError(const QString& msg);
    void clearError();
    void startCountdown();

    MockAuthService service_;
    QTimer countdownTimer_;
    QTimer errorClearTimer_;

    LoginPhase phase_ = LoginPhase::Phone;
    QString phone_;
    QString code_;
    int countdown_ = 0;
    bool isSubmitting_ = false;
    QString errorMessage_;
    int countdownIntervalMs_ = 1000;
    int errorClearDelayMs_   = 1200;
};
```

- [ ] **Step 3: 实现 `LoginController.cpp`**

```cpp
#include "login/LoginController.h"
#include "data/PhoneUtils.h"

LoginController::LoginController(QObject* parent) : QObject(parent), service_(this) {
    countdownTimer_.setSingleShot(false);
    errorClearTimer_.setSingleShot(true);

    connect(&service_, &MockAuthService::requestCodeFinished,
            this, &LoginController::onRequestCodeFinished);
    connect(&service_, &MockAuthService::verifyCodeFinished,
            this, &LoginController::onVerifyCodeFinished);

    connect(&countdownTimer_, &QTimer::timeout, this, [this]{
        if (countdown_ > 0) {
            countdown_--;
            emit countdownChanged(countdown_);
            if (countdown_ == 0) countdownTimer_.stop();
        } else {
            countdownTimer_.stop();
        }
    });

    connect(&errorClearTimer_, &QTimer::timeout, this, [this]{
        code_.clear();
        emit codeChanged(code_);
        clearError();
    });
}

bool LoginController::canRequestCode() const {
    return PhoneUtils::isValid(phone_) && !isSubmitting_;
}

void LoginController::onPhoneChange(const QString& raw) {
    phone_ = PhoneUtils::digitsOnly(raw).left(11);
    emit phoneChanged(phone_);
}

void LoginController::onCodeChange(const QString& raw) {
    QString digits;
    for (const QChar& c : raw) if (c.isDigit() && digits.size() < 6) digits.append(c);
    code_ = digits;
    emit codeChanged(code_);
    if (!errorMessage_.isEmpty()) clearError();
    if (code_.size() == 6) {
        isSubmitting_ = true;
        emit isSubmittingChanged(true);
        service_.verifyCode(phone_, code_);
    }
}

void LoginController::requestCode() {
    if (!PhoneUtils::isValid(phone_) || isSubmitting_) return;
    if (countdown_ > 0) {
        clearError();
        setPhase(LoginPhase::Code);
        return;
    }
    isSubmitting_ = true;
    emit isSubmittingChanged(true);
    clearError();
    service_.requestCode(phone_);
}

void LoginController::resendCode() {
    if (countdown_ > 0) return;
    service_.requestCode(phone_);
}

void LoginController::goBack() {
    setPhase(LoginPhase::Phone);
    clearError();
}

void LoginController::onRequestCodeFinished(AuthResult r) {
    isSubmitting_ = false;
    emit isSubmittingChanged(false);
    if (r.outcome == AuthOutcome::RequestSuccess) {
        code_.clear();
        emit codeChanged(code_);
        countdown_ = 60;
        emit countdownChanged(countdown_);
        setPhase(LoginPhase::Code);
        startCountdown();
    } else {
        setError("网络异常，请检查后重试");
    }
}

void LoginController::onVerifyCodeFinished(AuthResult r) {
    isSubmitting_ = false;
    emit isSubmittingChanged(false);
    switch (r.outcome) {
        case AuthOutcome::VerifySuccess: emit loginSucceeded(); break;
        case AuthOutcome::InvalidCode:
            setError("验证码错误，请重新输入");
            errorClearTimer_.start(errorClearDelayMs_);
            break;
        case AuthOutcome::NetworkError:
            setError("网络异常，请检查后重试");
            break;
        default: break;
    }
}

void LoginController::setPhase(LoginPhase p) {
    if (phase_ == p) return;
    phase_ = p;
    emit phaseChanged(p);
}

void LoginController::setError(const QString& msg) {
    if (errorMessage_ == msg) return;
    errorMessage_ = msg;
    emit errorMessageChanged(msg);
}

void LoginController::clearError() {
    if (errorMessage_.isEmpty()) return;
    errorMessage_.clear();
    emit errorMessageChanged(QString{});
}

void LoginController::startCountdown() {
    countdownTimer_.start(countdownIntervalMs_);
}
```

- [ ] **Step 4: 更新 `tests/CMakeLists.txt` 添加 LoginControllerTest**

```cmake
add_executable(LoginControllerTest
    LoginControllerTest.cpp
    ../src/login/LoginController.cpp
    ../src/data/MockAuthService.cpp
    ../src/data/PhoneUtils.cpp
)
target_include_directories(LoginControllerTest PRIVATE ../src)
target_link_libraries(LoginControllerTest PRIVATE Qt6::Test Qt6::Core)
add_test(NAME LoginControllerTest COMMAND LoginControllerTest)
```

- [ ] **Step 5: 把 `LoginController.cpp` 加入顶层 `APP_SOURCES`**

```cmake
set(APP_SOURCES
    src/main.cpp
    src/theme/Style.cpp
    src/data/PhoneUtils.cpp
    src/data/MockAuthService.cpp
    src/login/LoginController.cpp
)
```

- [ ] **Step 6: 构建并跑全部测试**

```bash
cmake --build build -j
ctest --test-dir build -R LoginControllerTest --output-on-failure
```

Expected：9 tests pass。

- [ ] **Step 7: 提交**

```bash
git add UI/Desktop
git commit -m "feat(desktop): add LoginController state machine with full unit tests"
```

---

## Task 6: PrimaryButton 组件

**Files:**
- Create: `UI/Desktop/src/login/components/PrimaryButton.h`
- Create: `UI/Desktop/src/login/components/PrimaryButton.cpp`
- Modify: 顶层 `CMakeLists.txt`

只是一个 `QPushButton` 子类，应用 `iChatPrimary` class。

- [ ] **Step 1: 实现 `PrimaryButton.h`**

```cpp
#pragma once
#include <QPushButton>

class PrimaryButton : public QPushButton {
    Q_OBJECT
public:
    explicit PrimaryButton(const QString& text, QWidget* parent = nullptr);
};
```

- [ ] **Step 2: 实现 `PrimaryButton.cpp`**

```cpp
#include "login/components/PrimaryButton.h"
#include "theme/Tokens.h"

PrimaryButton::PrimaryButton(const QString& text, QWidget* parent) : QPushButton(text, parent) {
    setProperty("class", "iChatPrimary");
    setMinimumHeight(tokens::ButtonHeight);
    setCursor(Qt::PointingHandCursor);
}
```

- [ ] **Step 3: 加入 `APP_SOURCES`**

```cmake
set(APP_SOURCES
    src/main.cpp
    src/theme/Style.cpp
    src/data/PhoneUtils.cpp
    src/data/MockAuthService.cpp
    src/login/LoginController.cpp
    src/login/components/PrimaryButton.cpp
)
```

- [ ] **Step 4: 验证编译**

```bash
cmake --build build -j
```

Expected：BUILD SUCCESSFUL。

- [ ] **Step 5: 提交**

```bash
git add UI/Desktop/src/login/components/PrimaryButton.h UI/Desktop/src/login/components/PrimaryButton.cpp UI/Desktop/CMakeLists.txt
git commit -m "feat(desktop): add PrimaryButton component"
```

---

## Task 7: PhoneInput 组件

**Files:**
- Create: `UI/Desktop/src/login/components/PhoneInput.h`
- Create: `UI/Desktop/src/login/components/PhoneInput.cpp`
- Modify: 顶层 `CMakeLists.txt`

- [ ] **Step 1: 实现 `PhoneInput.h`**

```cpp
#pragma once
#include <QLineEdit>
#include <QWidget>

class QHBoxLayout;
class QLabel;

class PhoneInput : public QWidget {
    Q_OBJECT
public:
    explicit PhoneInput(QWidget* parent = nullptr);
    QString rawDigits() const { return rawDigits_; }
    void setRawDigits(const QString& digits);

signals:
    void rawDigitsChanged(const QString& digits);

private slots:
    void onTextEdited(const QString& displayed);

private:
    QLineEdit* edit_;
    QString rawDigits_;
};
```

- [ ] **Step 2: 实现 `PhoneInput.cpp`**

```cpp
#include "login/components/PhoneInput.h"
#include "data/PhoneUtils.h"
#include "theme/Tokens.h"
#include <QHBoxLayout>
#include <QLabel>
#include <QFrame>

PhoneInput::PhoneInput(QWidget* parent) : QWidget(parent) {
    setFixedHeight(tokens::InputHeight);
    auto *layout = new QHBoxLayout(this);
    layout->setContentsMargins(16, 0, 16, 0);
    layout->setSpacing(12);

    auto *prefix = new QLabel("+86", this);
    prefix->setStyleSheet(QString("color:%1;font-size:14px;").arg(tokens::TextSecondary.name()));

    auto *sep = new QFrame(this);
    sep->setFrameShape(QFrame::VLine);
    sep->setFixedWidth(1);
    sep->setStyleSheet(QString("color:%1;").arg(tokens::BorderInput.name()));

    edit_ = new QLineEdit(this);
    edit_->setPlaceholderText("请输入手机号");
    edit_->setProperty("class", "iChatInput");
    edit_->setFrame(false);
    edit_->setStyleSheet("border:none;background:transparent;");
    edit_->setMaxLength(13);  // "138 0013 8000" = 13 chars
    connect(edit_, &QLineEdit::textEdited, this, &PhoneInput::onTextEdited);

    layout->addWidget(prefix);
    layout->addWidget(sep);
    layout->addWidget(edit_, 1);

    // 容器自身负责圆角 + 边框（QSS .iChatInput 用于 QLineEdit；这里手动套）
    setStyleSheet(QString(
        "PhoneInput { background:%1; border:1px solid %2; border-radius:%3px; }"
    ).arg(tokens::BgSurface.name(), tokens::BorderInput.name()).arg(tokens::InputRadius));
}

void PhoneInput::setRawDigits(const QString& digits) {
    rawDigits_ = PhoneUtils::digitsOnly(digits).left(11);
    QSignalBlocker block(edit_);
    edit_->setText(PhoneUtils::format(rawDigits_));
}

void PhoneInput::onTextEdited(const QString& displayed) {
    QString cleaned = PhoneUtils::digitsOnly(displayed).left(11);
    if (cleaned == rawDigits_) return;
    rawDigits_ = cleaned;
    QSignalBlocker block(edit_);
    edit_->setText(PhoneUtils::format(rawDigits_));
    edit_->setCursorPosition(edit_->text().size());
    emit rawDigitsChanged(rawDigits_);
}
```

- [ ] **Step 3: 加入 `APP_SOURCES` 并构建**

```cmake
src/login/components/PhoneInput.cpp
```

```bash
cmake --build build -j
```

Expected：BUILD SUCCESSFUL。

- [ ] **Step 4: 提交**

```bash
git add UI/Desktop/src/login/components/PhoneInput.h UI/Desktop/src/login/components/PhoneInput.cpp UI/Desktop/CMakeLists.txt
git commit -m "feat(desktop): add PhoneInput component"
```

---

## Task 8: CodeBoxes 组件

**Files:**
- Create: `UI/Desktop/src/login/components/CodeBoxes.h`
- Create: `UI/Desktop/src/login/components/CodeBoxes.cpp`
- Modify: 顶层 `CMakeLists.txt`

策略：6 个 `QLabel` 显示，一个不可见 `QLineEdit` 接收所有输入；`QLineEdit::textEdited` → 过滤数字 → 截断 6 位 → 触发外部 signal 并刷新 6 个 label。

- [ ] **Step 1: 实现 `CodeBoxes.h`**

```cpp
#pragma once
#include <QWidget>
#include <QVector>

class QLineEdit;
class QLabel;

class CodeBoxes : public QWidget {
    Q_OBJECT
public:
    explicit CodeBoxes(QWidget* parent = nullptr);
    QString value() const { return value_; }
    void    setValue(const QString& v);
    void    setError(bool err);
    void    focusInput();

signals:
    void valueChanged(const QString& v);

private slots:
    void onTextEdited(const QString& s);

private:
    void refresh();

    QLineEdit* edit_;
    QVector<QLabel*> boxes_;   // size 6
    QString value_;
    bool isError_ = false;
};
```

- [ ] **Step 2: 实现 `CodeBoxes.cpp`**

```cpp
#include "login/components/CodeBoxes.h"
#include "theme/Tokens.h"
#include <QHBoxLayout>
#include <QLabel>
#include <QLineEdit>
#include <QStackedLayout>

CodeBoxes::CodeBoxes(QWidget* parent) : QWidget(parent) {
    setFixedHeight(tokens::CodeBoxSize);

    auto *stack = new QStackedLayout(this);
    stack->setStackingMode(QStackedLayout::StackAll);

    // 可视层：6 个 QLabel
    auto *boxesContainer = new QWidget(this);
    auto *row = new QHBoxLayout(boxesContainer);
    row->setContentsMargins(0, 0, 0, 0);
    row->setSpacing(8);
    for (int i = 0; i < 6; ++i) {
        auto *label = new QLabel(boxesContainer);
        label->setAlignment(Qt::AlignCenter);
        label->setFixedSize(tokens::CodeBoxSize, tokens::CodeBoxSize);
        label->setStyleSheet(QString(
            "background:%1; border:1px solid %2; border-radius:%3px; color:%4; "
            "font-size:20px; font-weight:600;"
        ).arg(tokens::BgSurface.name(), tokens::BorderInput.name())
         .arg(tokens::CodeBoxRadius)
         .arg(tokens::TextPrimary.name()));
        boxes_.append(label);
        row->addWidget(label);
    }
    stack->addWidget(boxesContainer);

    // 输入层：不可见 QLineEdit
    edit_ = new QLineEdit(this);
    edit_->setMaxLength(6);
    edit_->setStyleSheet("background:transparent; border:none; color:transparent;");
    edit_->setObjectName("CodeBoxesInput");
    edit_->setFocusPolicy(Qt::StrongFocus);
    connect(edit_, &QLineEdit::textEdited, this, &CodeBoxes::onTextEdited);
    stack->addWidget(edit_);

    refresh();
}

void CodeBoxes::setValue(const QString& v) {
    QString cleaned;
    for (const QChar& c : v) if (c.isDigit() && cleaned.size() < 6) cleaned.append(c);
    if (cleaned == value_) return;
    value_ = cleaned;
    QSignalBlocker block(edit_);
    edit_->setText(value_);
    refresh();
    emit valueChanged(value_);
}

void CodeBoxes::setError(bool err) {
    if (isError_ == err) return;
    isError_ = err;
    refresh();
}

void CodeBoxes::focusInput() { edit_->setFocus(); }

void CodeBoxes::onTextEdited(const QString& s) {
    QString cleaned;
    for (const QChar& c : s) if (c.isDigit() && cleaned.size() < 6) cleaned.append(c);
    if (cleaned == value_) return;
    value_ = cleaned;
    QSignalBlocker block(edit_);
    edit_->setText(value_);
    refresh();
    emit valueChanged(value_);
}

void CodeBoxes::refresh() {
    for (int i = 0; i < 6; ++i) {
        QChar ch = (i < value_.size()) ? value_[i] : QChar();
        boxes_[i]->setText(ch.isNull() ? QString{} : QString(ch));
        QString border = isError_ ? tokens::StateError.name()
                       : (i == value_.size() ? tokens::BorderInputFocus.name() : tokens::BorderInput.name());
        boxes_[i]->setStyleSheet(QString(
            "background:%1; border:1px solid %2; border-radius:%3px; color:%4; "
            "font-size:20px; font-weight:600;"
        ).arg(tokens::BgSurface.name(), border).arg(tokens::CodeBoxRadius).arg(tokens::TextPrimary.name()));
    }
}
```

- [ ] **Step 3: 加入 `APP_SOURCES` 并构建**

```cmake
src/login/components/CodeBoxes.cpp
```

```bash
cmake --build build -j
```

Expected：BUILD SUCCESSFUL。

- [ ] **Step 4: 提交**

```bash
git add UI/Desktop/src/login/components/CodeBoxes.h UI/Desktop/src/login/components/CodeBoxes.cpp UI/Desktop/CMakeLists.txt
git commit -m "feat(desktop): add CodeBoxes component"
```

---

## Task 9: HeroPanel（左栏渐变 + 文案）

**Files:**
- Create: `UI/Desktop/src/login/HeroPanel.h`
- Create: `UI/Desktop/src/login/HeroPanel.cpp`
- Modify: 顶层 `CMakeLists.txt`

- [ ] **Step 1: 实现 `HeroPanel.h`**

```cpp
#pragma once
#include <QWidget>

class HeroPanel : public QWidget {
    Q_OBJECT
public:
    explicit HeroPanel(QWidget* parent = nullptr);

protected:
    void paintEvent(QPaintEvent* event) override;
};
```

- [ ] **Step 2: 实现 `HeroPanel.cpp`**

```cpp
#include "login/HeroPanel.h"
#include "theme/Tokens.h"
#include <QPainter>
#include <QLinearGradient>
#include <QLabel>
#include <QVBoxLayout>

HeroPanel::HeroPanel(QWidget* parent) : QWidget(parent) {
    setMinimumWidth(360);

    auto *layout = new QVBoxLayout(this);
    layout->setContentsMargins(44, 44, 44, 44);
    layout->setSpacing(0);

    // 顶部徽章（半透明背景，简化 backdrop blur）
    auto *badge = new QLabel("💬  iChat · 陪伴每一刻", this);
    badge->setStyleSheet(QString(
        "background:rgba(255,255,255,0.18); color:white; "
        "border:1px solid rgba(255,255,255,0.30); border-radius:24px; "
        "padding:6px 14px; font-size:12px;"
    ));
    badge->setFixedHeight(28);
    badge->setAttribute(Qt::WA_TranslucentBackground);

    // 主标题
    auto *title = new QLabel("每个人都值得\n一份温柔的陪伴", this);
    title->setStyleSheet("color:white; font-size:32px; font-weight:700;");
    title->setWordWrap(true);

    // 引言
    auto *quote = new QLabel("\"在这里，可以慢一点说话，\n也可以只是静静待着。\"", this);
    quote->setStyleSheet("color:rgba(255,255,255,0.85); font-size:14px;");
    quote->setWordWrap(true);

    layout->addWidget(badge, 0, Qt::AlignLeft);
    layout->addStretch(1);
    layout->addWidget(title);
    layout->addStretch(1);
    layout->addWidget(quote);
}

void HeroPanel::paintEvent(QPaintEvent* /*e*/) {
    QPainter p(this);
    p.setRenderHint(QPainter::Antialiasing);
    QLinearGradient g(0, 0, width(), height());
    // 160° gradient: from top-left toward bottom-right
    g.setColorAt(0.0, tokens::HeroFrom);
    g.setColorAt(1.0, tokens::HeroTo);
    p.fillRect(rect(), g);
}
```

- [ ] **Step 3: 加入 `APP_SOURCES`**

```cmake
src/login/HeroPanel.cpp
```

- [ ] **Step 4: 构建并验证**

```bash
cmake --build build -j
```

Expected：BUILD SUCCESSFUL。

- [ ] **Step 5: 提交**

```bash
git add UI/Desktop/src/login/HeroPanel.h UI/Desktop/src/login/HeroPanel.cpp UI/Desktop/CMakeLists.txt
git commit -m "feat(desktop): add HeroPanel with gradient paintEvent"
```

---

## Task 10: PhoneForm（右栏：phone / code 切换）

**Files:**
- Create: `UI/Desktop/src/login/PhoneForm.h`
- Create: `UI/Desktop/src/login/PhoneForm.cpp`
- Modify: 顶层 `CMakeLists.txt`

`QStackedWidget` 持两页：phone page、code page；接 `LoginController` 的 signals。

- [ ] **Step 1: 实现 `PhoneForm.h`**

```cpp
#pragma once
#include <QWidget>

class QStackedWidget;
class QLabel;
class LoginController;
class PhoneInput;
class CodeBoxes;
class PrimaryButton;

class PhoneForm : public QWidget {
    Q_OBJECT
public:
    explicit PhoneForm(LoginController* controller, QWidget* parent = nullptr);

private slots:
    void onPhaseChanged();
    void onPhoneChanged();
    void onCountdownChanged(int s);
    void onErrorMessageChanged(const QString& msg);
    void onIsSubmittingChanged(bool s);

private:
    QWidget* buildPhonePage();
    QWidget* buildCodePage();

    LoginController* controller_;
    QStackedWidget*  stack_;

    // phone page widgets
    PhoneInput*    phoneInput_  = nullptr;
    PrimaryButton* getCodeBtn_  = nullptr;
    QLabel*        phoneError_  = nullptr;

    // code page widgets
    QLabel*        codeSubtitle_ = nullptr;
    CodeBoxes*     codeBoxes_    = nullptr;
    QLabel*        codeError_    = nullptr;
    QLabel*        countdownLbl_ = nullptr;
};
```

- [ ] **Step 2: 实现 `PhoneForm.cpp`**

```cpp
#include "login/PhoneForm.h"
#include "login/LoginController.h"
#include "login/components/PhoneInput.h"
#include "login/components/CodeBoxes.h"
#include "login/components/PrimaryButton.h"
#include "data/PhoneUtils.h"
#include "theme/Tokens.h"
#include <QStackedWidget>
#include <QLabel>
#include <QVBoxLayout>
#include <QMouseEvent>

namespace {
QLabel* makeLabel(const QString& cls, const QString& text, QWidget* parent) {
    auto *l = new QLabel(text, parent);
    l->setProperty("class", cls);
    return l;
}
}

PhoneForm::PhoneForm(LoginController* controller, QWidget* parent)
    : QWidget(parent), controller_(controller) {
    setObjectName("PhoneForm");
    setStyleSheet(QString("QWidget#PhoneForm { background:%1; }").arg(tokens::BgSurface.name()));
    setMinimumWidth(tokens::FormWidth + 64 * 2);

    stack_ = new QStackedWidget(this);
    stack_->addWidget(buildPhonePage());   // index 0
    stack_->addWidget(buildCodePage());    // index 1

    auto *outer = new QVBoxLayout(this);
    outer->setContentsMargins(64, 0, 64, 0);
    outer->addStretch(1);
    outer->addWidget(stack_);
    outer->addStretch(1);

    connect(controller_, &LoginController::phaseChanged,         this, &PhoneForm::onPhaseChanged);
    connect(controller_, &LoginController::phoneChanged,         this, &PhoneForm::onPhoneChanged);
    connect(controller_, &LoginController::countdownChanged,    this, &PhoneForm::onCountdownChanged);
    connect(controller_, &LoginController::errorMessageChanged, this, &PhoneForm::onErrorMessageChanged);
    connect(controller_, &LoginController::isSubmittingChanged, this, &PhoneForm::onIsSubmittingChanged);
}

QWidget* PhoneForm::buildPhonePage() {
    auto *page = new QWidget(this);
    auto *col = new QVBoxLayout(page);
    col->setSpacing(20);
    col->setContentsMargins(0, 0, 0, 0);

    // 头部 logo + 标题
    auto *logoBox = new QLabel("i", page);
    logoBox->setAlignment(Qt::AlignCenter);
    logoBox->setFixedSize(40, 40);
    logoBox->setStyleSheet(QString(
        "background:%1; color:white; border-radius:12px; font-weight:700; font-size:18px;"
    ).arg(tokens::BrandPrimary.name()));

    auto *header = new QHBoxLayout();
    header->setSpacing(10);
    auto *headerLbl = makeLabel("iChatTitle", "登录 / 注册", page);
    header->addWidget(logoBox);
    header->addWidget(headerLbl);
    header->addStretch(1);

    phoneInput_ = new PhoneInput(page);
    connect(phoneInput_, &PhoneInput::rawDigitsChanged,
            controller_, &LoginController::onPhoneChange);

    getCodeBtn_ = new PrimaryButton("获取验证码", page);
    getCodeBtn_->setEnabled(false);
    connect(getCodeBtn_, &QPushButton::clicked, controller_, &LoginController::requestCode);

    phoneError_ = makeLabel("iChatError", "", page);
    phoneError_->setVisible(false);

    auto *meta = makeLabel("iChatCaption", "未注册的手机号将自动创建账号", page);

    col->addLayout(header);
    col->addSpacing(8);
    col->addWidget(phoneInput_);
    col->addWidget(getCodeBtn_);
    col->addWidget(phoneError_);
    col->addWidget(meta);
    return page;
}

QWidget* PhoneForm::buildCodePage() {
    auto *page = new QWidget(this);
    auto *col = new QVBoxLayout(page);
    col->setSpacing(20);
    col->setContentsMargins(0, 0, 0, 0);

    // 用 flat QPushButton 实现"链接"，QLabel 不响应 click 处理麻烦
    auto makeLinkButton = [&](const QString& text) {
        auto *b = new QPushButton(text, page);
        b->setFlat(true);
        b->setCursor(Qt::PointingHandCursor);
        b->setStyleSheet(QString(
            "QPushButton { color:%1; border:none; padding:0; font-size:12px; text-align:left; background:transparent; }"
        ).arg(tokens::BrandPrimary.name()));
        return b;
    };

    auto *backBtn = makeLinkButton("‹ 返回修改");
    connect(backBtn, &QPushButton::clicked, controller_, &LoginController::goBack);

    auto *title = makeLabel("iChatTitle", "输入验证码", page);
    codeSubtitle_ = makeLabel("iChatSubtitle", "已发送至 +86", page);

    codeBoxes_ = new CodeBoxes(page);
    connect(codeBoxes_, &CodeBoxes::valueChanged,
            controller_, &LoginController::onCodeChange);

    codeError_ = makeLabel("iChatError", "", page);
    codeError_->setVisible(false);

    countdownLbl_ = makeLabel("iChatLink", "60s 后重新发送", page);

    auto *resendBtn = makeLinkButton("重新发送");
    resendBtn->setObjectName("ResendBtn");
    resendBtn->setVisible(false);
    connect(resendBtn, &QPushButton::clicked, controller_, &LoginController::resendCode);

    auto *cantReceive = makeLabel("iChatLink", "收不到验证码？", page);

    col->addWidget(backBtn);
    col->addWidget(title);
    col->addWidget(codeSubtitle_);
    col->addSpacing(4);
    col->addWidget(codeBoxes_);
    col->addWidget(codeError_);
    col->addWidget(countdownLbl_);
    col->addWidget(resendBtn);
    col->addStretch(1);
    col->addWidget(cantReceive);

    return page;
}

void PhoneForm::onPhaseChanged() {
    stack_->setCurrentIndex(controller_->phase() == LoginPhase::Phone ? 0 : 1);
    if (controller_->phase() == LoginPhase::Code) {
        codeBoxes_->setValue("");
        codeBoxes_->setError(false);
        codeBoxes_->focusInput();
        codeSubtitle_->setText("已发送至 +86 " + PhoneUtils::mask(controller_->phone()));
    }
}

void PhoneForm::onPhoneChanged() {
    if (phoneInput_->rawDigits() != controller_->phone()) {
        phoneInput_->setRawDigits(controller_->phone());
    }
    getCodeBtn_->setEnabled(controller_->canRequestCode());
}

void PhoneForm::onCountdownChanged(int s) {
    auto *resendBtn = stack_->widget(1)->findChild<QPushButton*>("ResendBtn");
    if (s > 0) {
        countdownLbl_->setText(QString("%1s 后重新发送").arg(s));
        countdownLbl_->setVisible(true);
        if (resendBtn) resendBtn->setVisible(false);
    } else {
        countdownLbl_->setVisible(false);
        if (resendBtn) resendBtn->setVisible(true);
    }
}

void PhoneForm::onErrorMessageChanged(const QString& msg) {
    if (controller_->phase() == LoginPhase::Phone) {
        phoneError_->setText(msg);
        phoneError_->setVisible(!msg.isEmpty());
    } else {
        codeError_->setText(msg);
        codeError_->setVisible(!msg.isEmpty());
        codeBoxes_->setError(!msg.isEmpty());
    }
}

void PhoneForm::onIsSubmittingChanged(bool /*s*/) {
    getCodeBtn_->setEnabled(controller_->canRequestCode());
    getCodeBtn_->setText(controller_->isSubmitting() ? "发送中…" : "获取验证码");
}
```

- [ ] **Step 3: 加入 `APP_SOURCES` 并构建**

```cmake
src/login/PhoneForm.cpp
```

```bash
cmake --build build -j
```

Expected：BUILD SUCCESSFUL（如果有未使用变量 warning 可忽略）。

- [ ] **Step 4: 提交**

```bash
git add UI/Desktop/src/login UI/Desktop/CMakeLists.txt
git commit -m "feat(desktop): add PhoneForm with QStackedWidget for phone/code"
```

---

## Task 11: LoginWindow + Home + 装配 main

**Files:**
- Create: `UI/Desktop/src/login/LoginWindow.h`
- Create: `UI/Desktop/src/login/LoginWindow.cpp`
- Create: `UI/Desktop/src/home/HomePlaceholderWidget.h`
- Create: `UI/Desktop/src/home/HomePlaceholderWidget.cpp`
- Modify: `UI/Desktop/src/main.cpp`、顶层 `CMakeLists.txt`

- [ ] **Step 1: 实现 `HomePlaceholderWidget.h`**

```cpp
#pragma once
#include <QWidget>

class HomePlaceholderWidget : public QWidget {
    Q_OBJECT
public:
    explicit HomePlaceholderWidget(QWidget* parent = nullptr);
};
```

- [ ] **Step 2: 实现 `HomePlaceholderWidget.cpp`**

```cpp
#include "home/HomePlaceholderWidget.h"
#include "theme/Tokens.h"
#include <QLabel>
#include <QVBoxLayout>

HomePlaceholderWidget::HomePlaceholderWidget(QWidget* parent) : QWidget(parent) {
    setObjectName("HomePlaceholder");
    setStyleSheet(QString("QWidget#HomePlaceholder { background:%1; }").arg(tokens::BgPage.name()));
    auto *l = new QLabel("登录成功 · iChat", this);
    l->setAlignment(Qt::AlignCenter);
    l->setStyleSheet(QString("color:%1; font-size:20px; font-weight:600;").arg(tokens::TextPrimary.name()));
    auto *col = new QVBoxLayout(this);
    col->addWidget(l);
}
```

- [ ] **Step 3: 实现 `LoginWindow.h`**

```cpp
#pragma once
#include <QMainWindow>
#include "login/LoginController.h"

class HeroPanel;
class PhoneForm;

class LoginWindow : public QMainWindow {
    Q_OBJECT
public:
    explicit LoginWindow(QWidget* parent = nullptr);

protected:
    void resizeEvent(QResizeEvent* e) override;

private:
    void switchToHome();

    LoginController controller_;
    HeroPanel*  hero_  = nullptr;
    PhoneForm*  form_  = nullptr;
    QWidget*    central_ = nullptr;
};
```

- [ ] **Step 4: 实现 `LoginWindow.cpp`**

```cpp
#include "login/LoginWindow.h"
#include "login/HeroPanel.h"
#include "login/PhoneForm.h"
#include "home/HomePlaceholderWidget.h"
#include <QHBoxLayout>
#include <QResizeEvent>

LoginWindow::LoginWindow(QWidget* parent) : QMainWindow(parent) {
    setWindowTitle("iChat");
    resize(1080, 680);
    setMinimumSize(960, 600);

    central_ = new QWidget(this);
    auto *row = new QHBoxLayout(central_);
    row->setContentsMargins(0, 0, 0, 0);
    row->setSpacing(0);

    hero_ = new HeroPanel(central_);
    form_ = new PhoneForm(&controller_, central_);

    row->addWidget(hero_, 11);   // 左：右 = 1.1 : 1
    row->addWidget(form_, 10);

    setCentralWidget(central_);

    connect(&controller_, &LoginController::loginSucceeded,
            this, &LoginWindow::switchToHome);
}

void LoginWindow::resizeEvent(QResizeEvent* e) {
    QMainWindow::resizeEvent(e);
    // 宽 < 720 时单栏（隐藏 Hero）
    if (hero_) hero_->setVisible(width() >= 720);
}

void LoginWindow::switchToHome() {
    auto *home = new HomePlaceholderWidget(this);
    setCentralWidget(home);   // 自动 delete 旧 central_
}
```

- [ ] **Step 5: 修改 `main.cpp` 启动 `LoginWindow`**

```cpp
#include <QApplication>
#include "login/LoginWindow.h"
#include "theme/Style.h"

int main(int argc, char *argv[]) {
    QApplication app(argc, argv);
    app.setStyleSheet(style::applicationQss());

    LoginWindow w;
    w.show();
    return app.exec();
}
```

- [ ] **Step 6: 加入 `APP_SOURCES` 并构建**

```cmake
src/login/LoginWindow.cpp
src/home/HomePlaceholderWidget.cpp
```

```bash
cmake --build build -j
```

Expected：BUILD SUCCESSFUL。

- [ ] **Step 7: 手动 happy path**

```bash
./build/iChatDesktop
```

肉眼检查：
1. 窗口 1080×680，左侧蓝色渐变 Hero（"每个人都值得 / 一份温柔的陪伴"）+ 右侧白底登录表单
2. 输入 `13800138000` → 按钮高亮
3. 点 "获取验证码" → 800ms 后右栏切到验证码页（左栏不变），显示 "已发送至 +86 138****8000"
4. 输入 `123456` → 600ms 后整窗口切到 "登录成功 · iChat"
5. 重启 → 输入手机号 → 进验证码页 → 输 `000000` → 6 格红边 + "验证码错误，请重新输入" → 1.2s 后清空回第一格
6. 拖动窗口 < 720 宽 → Hero 隐藏（单栏）

- [ ] **Step 8: 提交**

```bash
git add UI/Desktop/src UI/Desktop/CMakeLists.txt
git commit -m "feat(desktop): wire LoginWindow + Hero + PhoneForm + Home placeholder"
```

---

## Task 12: 端到端测试（Qt Test GUI 自动化）

**Files:**
- Create: `UI/Desktop/tests/LoginE2ETest.cpp`
- Modify: `UI/Desktop/tests/CMakeLists.txt`

覆盖 spec §6.1 测试用例 #1, #3, #5, #6。

- [ ] **Step 1: 写 E2E 测试**

```cpp
#include <QtTest>
#include <QSignalSpy>
#include <QPushButton>
#include "login/LoginWindow.h"
#include "login/LoginController.h"
#include "login/components/PhoneInput.h"
#include "login/components/CodeBoxes.h"
#include "login/components/PrimaryButton.h"

class LoginE2ETest : public QObject {
    Q_OBJECT
private:
    template<typename T> T* find(QWidget* root, const QString& name = QString()) {
        return name.isEmpty() ? root->findChild<T*>() : root->findChild<T*>(name);
    }

private slots:

    void happy_path() {
        LoginWindow w;
        w.show();
        QVERIFY(QTest::qWaitForWindowExposed(&w));

        auto *phoneInput = find<PhoneInput>(&w);
        QVERIFY(phoneInput);
        auto *getCodeBtn = find<PrimaryButton>(&w);
        QVERIFY(getCodeBtn);

        // 用例 #1：未输入 → 按钮 disabled
        QVERIFY(!getCodeBtn->isEnabled());

        phoneInput->setRawDigits("13800138000");
        QTest::qWait(50);
        QVERIFY(getCodeBtn->isEnabled());

        // 用例 #3：发码 → 切到 Code 页
        QTest::mouseClick(getCodeBtn, Qt::LeftButton);
        QTRY_VERIFY_WITH_TIMEOUT(find<CodeBoxes>(&w) != nullptr, 2000);
        auto *codeBoxes = find<CodeBoxes>(&w);

        // 用例 #5：输入正确验证码 → 跳主页
        codeBoxes->setValue("123456");
        QTRY_VERIFY_WITH_TIMEOUT(w.findChild<QWidget*>("HomePlaceholder") != nullptr, 2000);
    }

    void back_from_code_keeps_phone() {
        LoginWindow w;
        w.show();
        QVERIFY(QTest::qWaitForWindowExposed(&w));

        auto *phoneInput = find<PhoneInput>(&w);
        phoneInput->setRawDigits("13800138000");
        QTest::qWait(50);
        QTest::mouseClick(find<PrimaryButton>(&w), Qt::LeftButton);
        QTRY_VERIFY_WITH_TIMEOUT(find<CodeBoxes>(&w) != nullptr, 2000);

        // 点击 "‹ 返回修改"
        auto buttons = w.findChildren<QPushButton*>();
        QPushButton* backBtn = nullptr;
        for (auto *b : buttons) if (b->text().contains("返回修改")) { backBtn = b; break; }
        QVERIFY(backBtn);
        QTest::mouseClick(backBtn, Qt::LeftButton);

        // 用例 #6：手机号保留
        QCOMPARE(phoneInput->rawDigits(), QString("13800138000"));
        QVERIFY(find<PrimaryButton>(&w)->isEnabled());
    }
};

QTEST_MAIN(LoginE2ETest)
#include "LoginE2ETest.moc"
```

- [ ] **Step 2: 更新 `tests/CMakeLists.txt`**

注：不要把 `../src/main.cpp` 加进 LoginE2ETest 源列表——`QTEST_MAIN` 已经定义了 `main`，会和 app 的 `main()` 冲突。

```cmake
add_executable(LoginE2ETest
    LoginE2ETest.cpp
    ../src/theme/Style.cpp
    ../src/data/PhoneUtils.cpp
    ../src/data/MockAuthService.cpp
    ../src/login/LoginController.cpp
    ../src/login/HeroPanel.cpp
    ../src/login/PhoneForm.cpp
    ../src/login/LoginWindow.cpp
    ../src/login/components/PrimaryButton.cpp
    ../src/login/components/PhoneInput.cpp
    ../src/login/components/CodeBoxes.cpp
    ../src/home/HomePlaceholderWidget.cpp
)
target_include_directories(LoginE2ETest PRIVATE ../src)
target_link_libraries(LoginE2ETest PRIVATE Qt6::Test Qt6::Widgets)
add_test(NAME LoginE2ETest COMMAND LoginE2ETest)
```

- [ ] **Step 3: 跑 E2E**

```bash
cmake --build build -j
ctest --test-dir build -R LoginE2ETest --output-on-failure
```

Expected：2 tests pass。注：在无显示器的 CI 上需要 `xvfb-run` 包裹 ctest。

- [ ] **Step 4: 提交**

```bash
git add UI/Desktop/tests/LoginE2ETest.cpp UI/Desktop/tests/CMakeLists.txt
git commit -m "test(desktop): add Qt Test E2E (happy path + back)"
```

---

## Task 13: 跑完所有测试 + 三平台 + 视觉走查

- [ ] **Step 1: 跑全部测试**

```bash
cd UI/Desktop && ctest --test-dir build --output-on-failure
```

Expected：全部 4 个 test executable PASS（PhoneUtilsTest, MockAuthServiceTest, LoginControllerTest, LoginE2ETest）。

总单测/集成断言数：约 23（PhoneUtils 7 slots + MockAuthService 3 + LoginController 9 + E2E 2）。

- [ ] **Step 2: macOS 视觉走查**

启动 `./build/iChatDesktop`，对照 `.superpowers/brainstorm/final-design.html` 桌面端图：

1. 1080×680 窗口，左：右 ≈ 11:10
2. 左侧 160° 蓝色渐变 `#4F86FF → #3B6BE0`，顶部"💬 iChat · 陪伴每一刻"半透明徽章，主标题白色 32px，引言半透明白
3. 右侧白底，垂直居中表单：logo `i`、"登录 / 注册"标题、+86 输入框、蓝色 pill 按钮、"未注册的手机号将自动创建账号" 灰字
4. 切到验证码页：返回链接、"输入验证码"标题、脱敏副标题、6 格、倒计时
5. 拖窗到 < 720 宽 → Hero 消失

- [ ] **Step 3: 在 Windows 与 Linux 上重复构建并运行**

```bash
# Linux：
sudo apt install qt6-base-dev cmake build-essential
cmake -S . -B build && cmake --build build -j && ./build/iChatDesktop

# Windows（PowerShell，假设已装 Qt 6.5+ via qt-online-installer）：
cmake -S . -B build -DCMAKE_PREFIX_PATH="C:/Qt/6.5.3/msvc2022_64"
cmake --build build --config Release
.\build\Release\iChatDesktop.exe
```

肉眼检查：在 Linux/Windows 下界面与 macOS 一致（字体可能略不同，但布局、颜色、间距应当对齐）。

- [ ] **Step 4: 最终提交（如有视觉调整）**

```bash
git add -A && git commit -m "chore(desktop): visual walkthrough adjustments"
```

否则跳过。

---

## Done When

- [ ] 所有单元 / 集成测试通过（4 个 test executable，约 23 个断言）
- [ ] macOS / Windows / Linux 三个平台均能 build 并启动
- [ ] 三平台手动走 happy path：启动 → 输 `13800138000` → 获取验证码 → 输 `123456` → 进入"登录成功 · iChat"
- [ ] 视觉与 spec §4 token 一致；三平台表现一致（字体差异除外）
- [ ] 窗口宽 < 720 时 Hero 自动隐藏（单栏）

---

## 已知不在第一期范围（Out of Scope，参见 spec §1.3）

- "收不到验证码？" 链接为静态文字，无点击行为
- 真实后端联调
- 玻璃拟态 backdrop blur（已降级为半透明纯色）
- 暗色模式
- 国际化 / 多语言
- 启动隐私协议弹窗
