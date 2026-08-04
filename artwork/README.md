# 图标素材

- `hey-gemini-light-expanded-540.png`：最终亮色图标资源。
- `hey-gemini-dark-expanded-540.png`：最终暗色图标资源。
- `hey-gemini-light-resource-432-backup.png`：亮色原始主体，用于重新调整留白。
- `hey-gemini-dark-resource-432-backup.png`：暗色原始主体，用于重新合成。
- `hey-gemini-dark-generated-background.png`：Image 2 生成的无接缝暗色背景。
- `hey-gemini-dark-star-mask.png`：暗色星形合成蒙版。

最终的 540 × 540 图片复制到
`app/src/main/res/drawable[-night]-xxxhdpi/ic_launcher_art.png`。Android 根据系统主题
自动选择普通或 `night` 资源；自适应图标的额外缩放在
`app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` 中设置。
