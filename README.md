![Banner](https://github.com/AigeStudio/WheelPicker/blob/master/Previews/main/Banner.jpg)

***

# Overview
[![Maven Central](https://img.shields.io/maven-central/v/dev.aige.pub/WheelPicker)](https://central.sonatype.com/artifact/dev.aige.pub/WheelPicker) [![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://android-arsenal.com/api?level=21) [![License](https://img.shields.io/badge/License-Apache%202-blue.svg)](https://github.com/AigeStudio/WheelPicker)

# Contact
[![QQ](https://img.shields.io/badge/QQ-1994099479-red.svg)](http://sighttp.qq.com/authd?IDKEY=404d62c783d5c76e312f4c9fa65819d75ce648bff94b8cd6) [![QQGroup](https://img.shields.io/badge/QQ%E7%BE%A4-361739851-blue.svg)](http://shang.qq.com/wpa/qunwpa?idkey=a62502df9a8f2f24f20f978070a9c93238c1fe91db8888dda78214cb83dc6002) [![Mail](https://img.shields.io/badge/mail-aigestudio%40qq.com-orange.svg)](http://mail.qq.com/cgi-bin/qm_share?t=qm_mailme&email=i_ri7O74--7v4uTL_vql6OTm) [![Sina](https://img.shields.io/badge/Sina-%40AigeStudio-red.svg)](http://weibo.com/aigestudio) [![Twitter](https://img.shields.io/badge/Twitter-%40AigeStudio-blue.svg)](https://twitter.com/AigeStudio)

# Preview
![Preview](https://github.com/AigeStudio/WheelPicker/blob/master/Previews/main/Preview.gif)

# Demo
[WheelPicke.APK](https://github.com/AigeStudio/WheelPicker/blob/master/APK/Demo.apk)

# Include
### Compile
```Gradle
implementation 'dev.aige.pub:WheelPicker:1.2.0'
```
or
```Maven
<dependency>
  <groupId>dev.aige.pub</groupId>
  <artifactId>WheelPicker</artifactId>
  <version>1.2.0</version>
  <type>pom</type>
</dependency>
```

# Usage
[WIKI](https://github.com/AigeStudio/WheelPicker/wiki/WIKI) | [帮助文档](https://github.com/AigeStudio/WheelPicker/wiki/%E5%B8%AE%E5%8A%A9%E6%96%87%E6%A1%A3)

# Versions
### 1.0.0 beta
* Preview for WheelPicker and support few function.

### 1.0.1 beta
* BugFix:Cache in WheelCuredPicker can not clean...a stupid mistake.

### 1.0.2 beta
* BugFix:MotionEvent lost when point outside of view in WheelCurvedPicker

### 1.0.3 beta
* BugFix:Error parameter after scroll using setXXX method

### 1.1.0 Stable
* Refactor project fix all known bug and some function additions
* I will create a tag for old beta version but not be updated anymore
* 重构项目发布稳定版修复测试版所有BUG以及新增功能
* Beta版本会打上TAG方便老版本过渡但不再更新

### 1.1.1
* BugFix:Scroll automatically when touch but not move on WheelPicker in some low resolution phone
* BugFix:Scroll range incorrect when invoke setData twice and set data source's length less than last
* BugFix:Wheel state do not refresh when invoke setData twice and set data source's length-1 less than last selected position
* BugFix:Switch between click and scroll event
* BugFix:Call OnItemSelectedListener more time when user want to scroll continuously
* BugFix:Scroll range incorrect when set current selected item again
* BugFix:Scroll will be triggered when click WheelPicker
* Function:All the parameters of WheelPicker will be reset when you setData
* ADD WheelYearPicker, WheelMonthPicker, WheelDayPicker
* ADD WheelDatePicker
* 修复某些低分辨率手机触摸不动时自滑动问题
* 修复第二次调用setData设置长度比上次小的数据时滑动范围不改变的问题
* 修复第二次调用setData设置长度位置小于上次选中位置时滚轮状态不刷新问题
* 修复点击与滑动事件切换的问题
* 修复当用户想连续滑动时出现多次回调的问题
* 修复重新设置选择的数据项位置后滑动范围错位问题
* 修复点击后触发滚动的问题
* 重新设置数据源后会重置滚轮选择器相关参数
* 新增年份、月份、日期选择器
* 新增三级联动的日期选择器

### 1.1.2
* BugFix:WheelPicker can not get the height in some layout
* Support Android Nougat

### 1.2.0
* Feature: Support Jetpack Compose
* Feature: Migrate to Maven Central (New Group ID: dev.aige.pub)
* Feature: Upgrade to Kotlin & Material 3
* Optimization: Remove Handler for animation
* Chore: Upgrade target SDK to 36
* 新增 Jetpack Compose 支持
* 迁移至 Maven Central (新 Group ID: dev.aige.pub)
* 全面迁移至 Kotlin 并适配 Material 3
* 移除 Handler 动画实现以优化性能
* 升级 Target SDK 至 36

# Function
* Data display circulation
* Set visible item count
* Get the current item data straight in stationary
* Monitor status of scroll get selected item data and other parameter when wheel stop
* Dynamic update data
* Set text color of selected or non-selected item
* Set item space
* Support display indicator and set the indicator's size and color
* Support display curtain and set the curtain's color
* Enable atmospheric effect
* Enable perspective effect
* Curl the items base on mathematic models
* Support item align when perspective or atmospheric enable
* 循环显示数据项
* 设置可见数据项条数
* 在滚轮静止状态直接获取选中数据项
* 滚动监听获取滚轮停止后选中项以及滚动各项参数
* 动态更新数据源
* 设置当前选中项文本颜色和非选中项文本颜色
* 设置数据项之间间隔
* 支持绘制指示器以及指定指示器颜色、尺寸
* 支持绘制幕布以及指定幕布颜色
* 可开启数据项空气感模拟
* 可开启数据项模拟真实透视效果
* 根据严格数学建模模拟滚轮弯曲效果
* 在开启透视或弯曲效果后支持让数据项左右对齐

# Widgets
## WheelDatePicker
![WheelDatePicker](https://github.com/AigeStudio/WheelPicker/blob/master/Previews/main/WheelDatePicker.gif)
### Method
* setVisibleItemCount
* setCyclic
* setSelectedItemTextColor
* setItemTextColor
* setItemTextSize
* setItemSpace
* setIndicator
* setIndicatorColor
* setIndicatorSize
* setCurtain
* setCurtainColor
* setAtmospheric
* setCurved
* setItemAlignYear
* setItemAlignDay
* setYearFrame
* setSelectedYear
* setSelectedMonth
* setSelectedDay
* etc...

## WheelYearPicker
![WheelYearPicker](https://github.com/AigeStudio/WheelPicker/blob/master/Previews/main/WheelYearPicker.gif)
### Method
* All method of WheelPicker
* setYearFrame
* set/getYearStart
* set/getYearEnd
* set/getSelectedYear
* getCurrentYear

## WheelMonthPicker
![WheelMonthPicker](https://github.com/AigeStudio/WheelPicker/blob/master/Previews/main/WheelMonthPicker.gif)
### Method
* All method of WheelPicker
* set/getSelectedMonth
* getCurrentMonth

## WheelDayPicker
![WheelDayPicker](https://github.com/AigeStudio/WheelPicker/blob/master/Previews/main/WheelDayPicker.gif)
### Method
* All method of WheelPicker
* set/getSelectedDay
* getCurrentDay
* setYearAndMonth
* set/getYear
* set/getMonth

## WheelAreaPicker
中国行政区域划分根据国家统计局最新数据[国家统计局行政区域划分](http://www.stats.gov.cn/tjsj/tjbz/xzqhdm/201504/t20150415_712722.html)

***

# Donation
如果您觉得该项目帮助了您那不妨赏小弟一杯咖啡钱 :)

![Pay](https://github.com/AigeStudio/WheelPicker/blob/master/Previews/main/Pay.png)

You can support the project and thank the author for his hard work :)

* PayPal:xiaoaibaby@vip.qq.com

***

# LICENSE
Copyright 2015-2025 [AigeStudio](https://github.com/AigeStudio)

Licensed under the Apache License, Version 2.0 (the "License");you may not use this file except in compliance with the License.

You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
