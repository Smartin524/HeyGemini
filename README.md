<div align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="artwork/icon-dark.png">
    <img src="artwork/icon-light.png" width="128" alt="HeyGemini icon">
  </picture>

  # HeyGemini

  在国行 OPPO / ColorOS 上快捷唤起 Gemini 浮层。

  *A no-root Gemini launcher for OPPO/ColorOS with Smart Sidebar support.*

  [![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com/about/versions/oreo)
  [![No Root](https://img.shields.io/badge/Root-Not%20Required-2ea44f)](#特点)
  [![Latest Release](https://img.shields.io/github/v/release/Smartin524/HeyGemini?display_name=tag)](https://github.com/Smartin524/HeyGemini/releases/latest)
</div>

## 简介

HeyGemini 是一个面向国行 OPPO / ColorOS 的极简 Android 启动器。点击图标后，它会通过
Android 系统语音交互接口直接打开 Gemini 浮层，适合放入智慧侧边栏，实现在任意页面快速
提问。

应用没有自己的界面，不需要 Root 或 Shizuku，也不会常驻后台。

## 特点

- 直接唤起 Gemini 浮层，而不是先打开 Google 或 Gemini 主界面
- 支持 ColorOS 智慧侧边栏等普通应用快捷入口
- 无窗口、无最近任务卡片、无启动动画
- 等待 140 ms 后唤起 Gemini，再延迟 20 ms 震动，使触感与唤起动画对齐
- 60 ms 震动反馈
- 自动适配亮色与暗色图标
- 默认助理不是 Google 时，自动打开数字助理设置页
- 默认助理是 Google 但其助理服务未运行时，仍尝试唤起，并提示切换到其他助理再切回 Google
- 提供 Logcat 与应用内滚动日志，方便排查偶发失败
- 无网络请求、无统计代码，仅申请振动权限

## 下载

前往 [Releases](https://github.com/Smartin524/HeyGemini/releases/latest) 下载最新版 APK。

当前包名：`dev.heygemini`

> 从 1.x 升级时请先卸载旧包 `dev.quickgemi`，并重新把 HeyGemini 加入智慧侧边栏。

## 使用条件

- Android 8.0 或更高版本
- 已安装并初始化 Google App 与 Gemini
- Google 已被设为系统默认数字助理
- 网络环境能够正常使用 Gemini

## 安装与设置

1. 从 [Releases](https://github.com/Smartin524/HeyGemini/releases/latest) 下载并安装 APK。
2. 打开 Google App 和 Gemini，完成账号登录与初始化。
3. 前往系统的“默认应用 / 数字助理应用”，选择 Google。
4. 首次启动 HeyGemini 时，允许 ColorOS 的跨应用启动确认。
5. 将 HeyGemini 加入智慧侧边栏。

请优先通过系统设置界面选择 Google。直接写入 `secure assistant` 并不总会同步 Android 的
Assistant Role，可能造成“设置值是 Google，但实际角色仍属于其他助理”的冲突。

排查已连接 ADB 的设备时，可以同时检查三项：

```shell
adb shell cmd role get-role-holders --user 0 android.app.role.ASSISTANT
adb shell settings get secure assistant
adb shell settings get secure voice_interaction_service
```

三项都应指向 Google。若 Role 不一致，请回到系统的“默认应用 / 数字助理应用”重新选择
Google；不要只修改后两项设置值。

## 工作原理

1. 无窗口 Activity 检查 Google 是否为默认助理，以及其助理服务是否正在运行（按包名匹配，不依赖服务类名）。
2. Activity 启动一个短生命周期 Service 后立即结束。
3. Service 等待 140 ms，让 ColorOS 智慧侧边栏完成收起动画。
4. 向 Google App 定向发送 `android.intent.action.VOICE_COMMAND`。
5. 再等待 20 ms，产生一次 60 ms 震动。
6. Service 立即停止。

短生命周期 Service 每次仅运行约 140 ms，不会常驻后台。

## 诊断日志

每次触发都会记录默认助理状态、延迟回调、震动、Intent 解析结果和异常。

读取 Logcat：

```shell
adb logcat -d -s HeyGemini:I '*:S'
```

读取应用内的 64 KB 滚动日志（超出时保留较新的一半）（调试版 APK）：

```shell
adb shell run-as dev.heygemini cat files/heygemini.log
```

- 完全没有 `Trigger received`：ColorOS 或快捷入口没有启动 HeyGemini。
- 有 `Trigger received`，但没有 `Delay elapsed`：短任务 Service 未成功执行。
- 有 `Assistant launch failed`：查看同一时间戳后的异常信息。

## 从源码构建

需要 JDK 17 和 Android SDK：

```shell
git clone https://github.com/Smartin524/HeyGemini.git
cd HeyGemini
./gradlew clean assembleDebug
```

APK 输出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

安装到已连接的 Android 设备：

```shell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 主要参数

- 应用 ID：`dev.heygemini`
- 最低版本：Android 8.0（API 26）
- 目标版本：API 36
- 侧边栏等待：140 ms
- 唤起后震动等待：20 ms
- 震动：60 ms，振幅 220
- 图标前景 inset：6%

## 已知限制

- HeyGemini 不提供“Hey Google”语音唤醒。
- 它不能绕过 ColorOS 对电源键、左侧快捷键或系统助理的厂商限制。
- Google 必须是系统当前激活的 `VoiceInteractionService`。
- 小布可以继续安装，但不能与 Google 同时成为当前激活的数字助理。
- 免打扰、手电筒等手机控制能力取决于 Gemini、Google App 与系统权限。

## 隐私

HeyGemini 不连接任何服务器，不收集或上传数据。诊断日志仅保存在 Android Logcat 和应用
私有目录中，内容限于唤起流程、组件名称与异常信息。
