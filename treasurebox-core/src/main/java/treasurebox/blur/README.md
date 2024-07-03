# 高斯模糊实现方案——BlurFrameLayout

## 使用方法

BlurFrameLayout集成了两种高斯模糊的实现，分别通过RenderScript或OpenGL实现高斯模糊。在xml中通过 `app:blur_render_type` 声明要使用的实现，默认使用的是RenderScript。

```xml
<treasurebox.blur.BlurFrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:blur_render_type="render_script" />
```

BlurFrameLayout支持模糊图片和作为背景蒙层模糊。在xml中通过 `app:blur_src` 声明要模糊的图片资源。

```xml
<treasurebox.blur.BlurFrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:blur_bitmap="@mipmap/ic_launcher" />
```

在xml中通过 `app:blur_overlay_color` 声明要模糊的图片资源。通过 `app:blur_radius` 声明高斯模糊的模糊半径。模糊半径默认为10。

```xml
<treasurebox.blur.BlurFrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:blur_radius="25"
        app:blur_overlay_color="@color/white" />
```

在xml中通过 `app:corner_radius` 声明轮廓圆角大小。通过 `app:border_width` 和 `app:border_color` 声明边框的宽度和颜色。

```xml
<treasurebox.blur.BlurFrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:blur_overlay_color="@color/white"
        app:blur_radius="25"
        app:border_color="@color/black"
        app:border_width="2dp"
        app:corner_radius="20dp" />
```

以上这些属性设置在代码中都有对应函数实现可调用。

> 当BlurFrameLayout作为背景蒙层模糊时，需要将BlurFrameLayout作为父布局。当蒙层下的发生更改时，需要调用 `refreshBlurOverlay` 函数刷新蒙层。