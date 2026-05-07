# iChat 登录界面设计

- **Date:** 2026-05-07
- **Scope:** 第一期登录界面（手机号 + 短信验证码），三端原生实现
- **Platforms:** Android (Kotlin / Jetpack Compose), iOS (Swift / SwiftUI), Desktop (Qt 6 + C++ Widgets · macOS / Windows / Linux)
- **Status:** Design — pending implementation plan

---

## 1. 目标与基线

### 1.1 目标

为 iChat 提供一个最小、统一、可在五个平台原生运行的登录界面。完成后用户可以从 App 启动 → 登录页 → 输入手机号 → 输入验证码 → 进入主页占位。

### 1.2 设计基线

- **简洁优先**：能简则简，避免冗余确认与堆叠功能。每屏聚焦一个动作。
- **五端 UI 视觉一致**：颜色 / 圆角 / 字号 / 间距使用同一套 token，跨端走查时差异肉眼不可辨。
- **登录即注册**：未注册手机号在验证通过后自动创建账号，没有独立的"注册"入口。

### 1.3 第一期边界

| In Scope | Out of Scope |
| --- | --- |
| 手机号 + 短信验证码登录（仅 +86）| Apple ID / Google / 微信登录 |
| 三端 UI + 状态机 + 本地 mock | 真实后端（短信通道、用户表、token） |
| 浅色主题、竖屏（移动端）| 暗色模式、横屏 / 平板专属布局 |
| 简体中文文案 | 国际化 / 多语言 |
| — | 启动页隐私协议弹窗（另立 spec） |

---

## 2. 信息架构与状态机

### 2.1 流程

```
[Phone] ──"获取验证码"──▶ [Code] ──6位齐 & 校验通过──▶ 进入 App
   ▲                       │
   └──── 返回 ─────────────┘
```

只有一个流程入口，移动端两屏（导航栈），桌面端两个表单状态（同窗口右栏内切换）。

### 2.2 状态

| 屏幕 | 状态 | 触发条件 / UI 表现 |
| --- | --- | --- |
| Phone | `idle` | 手机号 < 11 位，"获取验证码"按钮 disabled |
| Phone | `valid` | 手机号 = 11 位且格式合法，按钮高亮可点 |
| Phone | `submitting` | 发码请求中，按钮 loading（disabled + spinner） |
| Code | `waiting` | 倒计时中（默认 60s），"重新发送"不可点 |
| Code | `resendable` | 倒计时归零，"重新发送"可点 |
| Code | `verifying` | 6 格填满后自动校验中 |
| Code | `error` | 业务错（INVALID_CODE / EXPIRED_CODE）：6 格红边 + 错误文案，1.2s 后清空回第一格 |

### 2.3 接口约定（Mock，第二期接真后端）

```
POST /auth/sms/request   { phone }                    → 200 / 429
POST /auth/sms/verify    { phone, code }              → { token, user, isNewUser }
```

- Mock `requestCode`：800ms 延迟，固定成功
- Mock `verifyCode`：600ms 延迟。`code == "123456"` 返回成功 `{ token: "mock", user: {...}, isNewUser: false }`，否则返回 `INVALID_CODE`

---

## 3. 跨平台架构与文件结构

UI 各端原生，第一期不抽共享层。三端各自维护一个状态控制器，方法/状态字段命名严格对齐，便于第二期抽共享层时低成本迁移。

### 3.1 命名对齐约定

| 概念 | Android | iOS | Desktop |
| --- | --- | --- | --- |
| 状态控制器 | `LoginViewModel` | `LoginStore` (`ObservableObject`) | `LoginController` (`QObject`) |
| 发码方法 | `requestCode()` | `requestCode()` | `requestCode()` |
| 校验方法 | `verifyCode()` | `verifyCode()` | `verifyCode()` |
| 返回方法 | `goBack()` | `goBack()` | `goBack()` |
| 状态字段 | `phase / phone / code / countdown / error` | 同 | 同（属性 + signals） |

### 3.2 文件结构

```
UI/
├── Android/                        # Kotlin · Jetpack Compose · minSdk 26
│   └── login/
│       ├── LoginActivity.kt
│       ├── PhoneScreen.kt
│       ├── CodeScreen.kt
│       ├── LoginViewModel.kt
│       ├── data/
│       │   └── MockAuthRepository.kt
│       └── components/
│           ├── PhoneInput.kt
│           ├── CodeBoxes.kt
│           └── PrimaryButton.kt
│
├── iOS/                            # Swift · SwiftUI · iOS 16+
│   └── Login/
│       ├── LoginView.swift          # NavigationStack 容器
│       ├── PhoneView.swift
│       ├── CodeView.swift
│       ├── LoginStore.swift
│       ├── Service/
│       │   └── MockAuthService.swift
│       └── Components/
│           ├── PhoneField.swift
│           ├── CodeBoxes.swift
│           └── PrimaryButton.swift
│
└── Desktop/                        # Qt 6.5+ · C++17 · CMake · Qt Widgets
    ├── CMakeLists.txt
    └── login/
        ├── main.cpp
        ├── LoginWindow.h/.cpp        # QMainWindow，左右两栏 QHBoxLayout
        ├── HeroPanel.h/.cpp          # QWidget 子类，paintEvent 画渐变
        ├── PhoneForm.h/.cpp          # QStackedWidget：phone / code 切换
        ├── LoginController.h/.cpp    # QObject 状态机 + mock，signals 驱动 UI
        ├── service/
        │   └── MockAuthService.h/.cpp
        └── components/
            ├── PrimaryButton.h/.cpp
            ├── PhoneInput.h/.cpp
            └── CodeBoxes.h/.cpp
```

### 3.3 桌面端关键技术决策

- **不使用 QML**，UI 全部 C++ + Qt Widgets。理由：长期可维护性优先，避免 QML/C++ 双栈桥接复杂度。
- **Hero 渐变背景**：`HeroPanel::paintEvent` 中用 `QLinearGradient` 直接绘制（160° 渐变 `#4F86FF → #3B6BE0`）。
- **圆角 / Hover 态**：通过 `QSS` 样式表实现。
- **屏幕切换动画**：`QStackedWidget::setCurrentIndex` + `QPropertyAnimation` 200ms 淡入淡出。
- **降级**：设计稿中桌面 Hero 徽章的"玻璃拟态"在 Widgets 中降级为半透明纯色（`rgba(255,255,255,0.18)`），不做 backdrop blur。

---

## 4. 视觉规范（设计 token，三端共用）

### 4.1 色板

| Token | 值 | 用途 |
|---|---|---|
| `brand/primary` | `#4F86FF` | 主按钮、品牌 Logo、聚焦边框、链接文字 |
| `brand/primary-pressed` | `#3B6BE0` | 按钮按下/Hover 态 |
| `brand/primary-disabled` | `#B7CDFA` | 主按钮 disabled 态 |
| `bg/page` | `#F2F6FF` | 移动端整页背景 / 桌面端窗口外底色 |
| `bg/surface` | `#FFFFFF` | 输入框、桌面端右栏表单区 |
| `border/input` | `#DCE6FA` | 输入框默认描边 |
| `border/input-focus` | `#4F86FF` | 输入框聚焦描边（外加 3px `rgba(79,134,255,0.15)` 光圈）|
| `text/primary` | `#1A2233` | 标题、输入文字 |
| `text/secondary` | `#7884A3` | 副标题、占位、提示文案 |
| `text/tertiary` | `#9AA3B5` | meta（"未注册手机号将自动创建账号"）|
| `state/error` | `#E5484D` | 错误描边 + 错误文字 |
| `hero/gradient-from` | `#4F86FF` | 桌面 Hero 渐变起点 |
| `hero/gradient-to` | `#3B6BE0` | 桌面 Hero 渐变终点（160°）|

### 4.2 圆角

- 输入框、Logo 方块：`14px`
- 主按钮："药丸"形 → `24px`（高度的一半）
- 验证码 6 格盒：`12px`
- 桌面 Hero 徽章："药丸"形 `24px`

### 4.3 字号 / 字重

| Token | 大小 / 字重 | 用途 |
|---|---|---|
| `title/lg` | 20 / 600 | "欢迎使用 iChat" / "输入验证码" |
| `title/xl` | 32 / 700 | 桌面 Hero 主标题 |
| `body/md` | 14 / 400 | 输入框文字、按钮文字 |
| `body/sm` | 13 / 400 | 副标题、Hero 引言 |
| `caption` | 12 / 400 | meta、错误文字、倒计时提示 |
| `code-box` | 20 / 600 | 验证码 6 格内的数字 |

字体：移动端用系统默认（iOS = SF Pro / Android = Roboto + Noto Sans CJK）。桌面端 QSS 配 `-apple-system, "PingFang SC", "Microsoft YaHei", sans-serif`。

### 4.4 间距 / 尺寸

- 移动端水平内边距：`24px`
- 控件之间垂直间距：`20px`（Logo↔标题 / 标题↔副标题 间距 4px）
- 输入框高度：`48px`
- 主按钮高度：`48px`，宽度 100%（移动端）/ 表单宽（桌面）
- 验证码 6 格盒：高 `48px`，格间距 `8px`
- 桌面右栏表单宽度：`320px`，左右内边距 `64px`，垂直居中
- 桌面 Hero 内边距：`44px`
- 桌面默认窗口尺寸：`1080×680`，最小 `960×600`，宽 `< 720` 时单栏（隐藏 Hero）

### 4.5 输入与按钮规则

- 手机号输入框：仅数字键盘（iOS `.numberPad` / Android `inputType="phone"`），`maxLength=11`，按 `3-4-4` 视觉分组（提交时去空格）
- 验证码 6 格：每格 `maxLength=1`、仅数字、自动跳焦下一格、退格回上一格、支持粘贴 6 位串自动逐格填充
- iOS 6 格容器声明 `textContentType = .oneTimeCode`，启用短信 AutoFill
- Android 预留 SMS Retriever API 接入点（第一期 mock 不联调）
- "获取验证码"按钮：手机号 11 位才启用
- 倒计时 60 秒：发码后立即 disabled + 文案变 `xxs 后重新发送`，归零后变 `重新发送` 可点
- 验证码屏 **不需要"提交"按钮**，6 格填满即自动校验

### 4.6 错误态

- 验证码错：6 格全部 `state/error` 描边，下方 caption 红字 "验证码错误，请重新输入"，1.2s 后清空回第一格
- 手机号格式错：输入框描边变红，下方红字 "请输入正确的手机号"
- 频率限制（429）：toast / 桌面端顶部红色横条 4s 自动消失，文案 "请求过于频繁，请稍后再试"
- 网络异常：toast "网络异常，请检查后重试"，UI 不锁死可重试

---

## 5. 边界情况

### 5.1 频控 / 防重复

- 点击"获取验证码"立即 disabled + 进入倒计时
- 同一手机号 60s 内不允许重发；倒计时状态保留在 ViewModel/Store/Controller，UI 销毁不影响

### 5.2 返回 / 状态保留

- 验证码屏返回 → 回到手机号屏，**已填手机号保留**、**倒计时继续在后台计时**
- Android 系统返回键、iOS 滑动返回手势同上
- 桌面端用顶部 "‹ 返回修改" 文字链，行为同上

### 5.3 键盘 / 焦点

- 进入屏幕自动聚焦输入框 + 弹键盘，无需多一次点击
- 验证码屏第一格自动聚焦
- 键盘遮挡：核心控件位于屏幕上半部，底部辅助链接（"收不到验证码？"）用 flex 推到剩余空间底部

### 5.4 横竖屏 / 窗口

- 移动端仅竖屏锁定（manifest / Info.plist）
- 桌面双栏布局最小尺寸 `960×600`；宽 < `720` 自动切单栏（隐藏 Hero）

### 5.5 验证成功

- `verifyCode` 返回 `{ token, user, isNewUser }`：
  - `isNewUser=true` → 跳"完善资料"页（另立 spec）
  - `isNewUser=false` → 跳主页
- 第一期 mock 全部当 `isNewUser=false`，跳一个空白 placeholder 主页

---

## 6. 测试策略

每端三层：状态机单元测试 → UI 组件测试 → 端到端 happy path。

| 层级 | Android | iOS | Desktop |
|---|---|---|---|
| 状态机 | JUnit + Mockito 测 `LoginViewModel` | XCTest 测 `LoginStore` | Qt Test 测 `LoginController` |
| UI 组件 | Compose UI Test | ViewInspector / Snapshot | QSignalSpy + QTest |
| E2E happy path | Espresso | XCUITest | QTest GUI |

### 6.1 关键测试用例（三端通用）

1. 手机号 < 11 位 → 按钮 disabled
2. 手机号 11 位但非法（如 `00000000000`）→ 红边 + 错误文案
3. 发码成功 → 切到验证码屏 + 60s 倒计时 + 副标题脱敏显示手机号
4. 6 格填满后输错（非 `123456`）→ 红边 + 错误文案 + 1.2s 后清空回第一格
5. 6 格填满后输对（`123456`）→ 跳空白主页
6. 验证码屏返回 → 手机号保留，倒计时不重置
7. 倒计时中再回到验证码屏 → 倒计时继续显示剩余秒数
8. mock 注入网络错误 → toast 提示，UI 不锁死

### 6.2 验收标准

- 三端在各自平台模拟器/真机跑通上述 8 条测试用例 + happy path E2E
- 视觉走查：三端截图与本文档第 4 节 token 一致
- 性能：从启动 App 到登录页可交互 ≤ 2s（移动端中端机型）

---

## 7. 后续展开（不在第一期）

- 启动隐私协议弹窗（合规要求，独立 spec）
- 完善资料页（昵称 / 头像 / 性别 / 生日）
- Apple ID / Google 登录入口接入
- 真实后端：短信通道（Aliyun / Tencent SMS）、用户表、JWT 签发、风控
- 抽共享层（业务 + 网络）：可选 KMM / C++ core 方案，三端 Controller 统一调用
- 暗色模式
