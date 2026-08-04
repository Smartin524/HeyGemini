# HeyGemini

HeyGemini 是一个为国行 OPPO / ColorOS 制作的极简 Android 启动器。点击图标后，
它通过系统语音交互接口直接唤起 Gemini 浮层，不显示自己的界面，也不常驻后台。

当前版本：`1.6.6`

## 工作方式

1. 检查当前系统数字助理是否为 Google 的 `GsaVoiceInteractionService`。
2. 无窗口 Activity 启动一个短生命周期 Service 后立即结束。
3. Service 保持进程约 140 ms，让 ColorOS 智慧侧边栏完成收起动画。
4. 产生一次 60 ms 震动，紧接着向 Google App 定向发送
   `android.intent.action.VOICE_COMMAND`，然后 Service 自行停止。
5. 如果 Google 不是默认助理，则打开系统数字助理设置页。

应用使用 `Theme.NoDisplay`，不会创建窗口或出现在最近任务中。它没有网络请求、统计代码
或持久进程；短生命周期 Service 每次仅运行约 140 ms，并且仅申请振动权限。

## 使用条件

- Google App：`com.google.android.googlequicksearchbox`
- Gemini 已完成初始化
- Google 已设为默认数字助理应用
- ColorOS 首次跨应用启动确认已允许

可在手机设置中手动选择默认数字助理，也可以在已连接 ADB 时执行：

```shell
adb shell settings put secure assistant \
  com.google.android.googlequicksearchbox/com.google.android.voiceinteraction.GsaVoiceInteractionService
adb shell settings put secure voice_interaction_service \
  com.google.android.googlequicksearchbox/com.google.android.voiceinteraction.GsaVoiceInteractionService
```

设置完成后，可将 HeyGemini 加入智慧侧边栏或其他支持启动普通应用的快捷入口。

## 当前参数

- 侧边栏收起等待：140 ms
- 启动震动：60 ms，振幅 220
- 图标：自动适配亮色/暗色主题，前景 inset 为 6%
- 工程名称与代码命名空间：`HeyGemini` / `dev.heygemini`
- 应用 ID：`dev.quickgemi`（仅为兼容已经安装的早期版本，后续可直接覆盖升级）
- 最低 Android 版本：Android 8.0（API 26）
- 目标 Android 版本：API 36

延迟和震动参数位于：

```text
app/src/main/java/dev/heygemini/MainActivity.kt
```

图标缩放参数位于：

```text
app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
```

## 诊断日志

每次触发都会记录默认助理状态、140 ms 回调、震动请求、Intent 解析结果、启动结果及异常。
日志同时写入 Android Logcat 和应用私有的 64 KB 滚动文件。

读取 Logcat：

```shell
adb logcat -d -s HeyGemini:I '*:S'
```

读取持久日志（当前调试版 APK 支持 `run-as`）：

```shell
adb shell run-as dev.quickgemi cat files/heygemini.log
```

如果一次点击完全没有产生 `Trigger received`，说明 ColorOS 或快捷入口没有启动
HeyGemini；如果有该记录但后续失败，则可根据同一组时间戳判断是默认助理、震动还是
`ACTION_VOICE_COMMAND` 阶段的问题。

## 构建

需要 JDK 17 和 Android SDK。在工程根目录运行：

```shell
./gradlew clean assembleDebug
```

生成的 APK 位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

安装到已连接的手机：

```shell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 工程结构

```text
app/src/main/java/dev/heygemini/MainActivity.kt   无窗口入口
app/src/main/java/dev/heygemini/GeminiLaunchService.kt  延迟、震动与唤起
app/src/main/java/dev/heygemini/AssistantSupport.kt     助理检查与设置回退
app/src/main/java/dev/heygemini/LaunchLogger.kt         Logcat 与滚动日志
app/src/main/AndroidManifest.xml                  应用声明与振动权限
app/src/main/res/                                 主题、文字和亮/暗色图标
artwork/                                          最终图标及可复现所需素材
```

## 已知边界

- HeyGemini 只是启动入口，不提供“Hey Google”语音唤醒。
- 它不能绕过 ColorOS 对电源键、左侧快捷键或系统助理的厂商限制。
- 是否能控制免打扰、手电筒等系统功能，取决于 Gemini、Google App 和系统权限。
- Google App 必须是系统当前激活的数字助理；小布可以保留安装，但不能同时成为激活的
  `VoiceInteractionService`。
- 应用本身不需要在后台运行；系统可能为当前默认助理保留 Google 的语音交互进程。

## 恢复未配置状态

```shell
adb shell settings delete secure assistant
adb shell settings delete secure voice_interaction_service
```
