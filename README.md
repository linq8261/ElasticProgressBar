# ElasticProgressBar
原项目[Tibolte/ElasticDownload](https://github.com/Tibolte/ElasticDownload)，精简只保留进度条。

## 效果图：

![成功效果](https://github.com/AudienL/ElasticProgressBar/blob/master/document/success.gif)
![失败效果](https://github.com/AudienL/ElasticProgressBar/blob/master/document/failed.gif)

## 使用：

### 一、在 project 根目录的 build.gradle 中添加：

```groovy
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```

### 二、在 module 根目录的 build.gradle 中添加：

其中最后版本在 release 中查看，如：1.0
```groovy
dependencies {
    compile 'com.github.AudienL:ElasticProgressBar:最后版本'
}
```

### 三、使用

#### 1> 

```xml
<com.audienl.elasticprogressbarcore.ElasticProgressBar
    android:id="@+id/elastic_progress_bar"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="#000000"
    app:back_line_color="#95A5A6"
    app:bubble_color="#F1C40F"
    app:front_line_color="#F1C40F"
    app:text_color="#FFFFFF" />
```

#### 2> 

```java
@Bind(R.id.elastic_progress_bar) ElasticProgressBar mElasticProgressBar;

// 开始动画，这里实际是setProgress(0)
mElasticProgressBar.start();

// 设置进度
mElasticProgressBar.setProgress(progress);

// 调用成功动画
mElasticProgressBar.success();

// 调用失败动画
mElasticProgressBar.fail();
```