# 图标素材

图标由 `generate_icon.py` 用代码生成，所有变体共用同一份几何参数：

- `app/src/main/res/drawable/ic_launcher_foreground.xml`：四角星前景（亮暗共用）。
- `app/src/main/res/drawable[-night]/ic_launcher_background.xml`：亮色白底；暗色为取自
  ColorOS 系统图标的中性深灰渐变。
- `app/src/main/res/drawable/ic_launcher_monochrome.xml`：系统主题图标单色层。
- `icon-light.png` / `icon-dark.png`：README 预览图。

调整尺寸、颜色或星形后，在项目根目录重新运行：

```bash
python3 artwork/generate_icon.py
```
