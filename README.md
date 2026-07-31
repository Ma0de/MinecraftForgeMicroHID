# Micro H.I.D. - Minecraft Forge Mod

一个为 Minecraft 制作的 Micro H.I.D. 模组

（***Micro High-Intensity Electrical Discharge Thrower***）

## 概述

此模组添加了一个基于 SCP: Secret Laboratory 中电磁炮（MicroHID）灵感的高能武器。它提供了完整的模型及高能蓄力攻击功能实现

## 功能特性

不想介绍（


## 游戏内安装

1. 确保已安装对应版本的 [Minecraft Forge](https://files.minecraftforge.net/)。
    
2. 将本模组的 `.jar` 文件放入 Minecraft 实例的 `mods` 文件夹中。
    
3. 启动游戏。

## 开发与构建

本项目使用 Forge MDK
（建议）确保你安装了Java21、IntelliJ IDEA
我使用的开发环境：VSCode + Extension Pack for Java + Gradle

1. 首先 `git clone https://github.com/Ma0de/MinecraftForgeMicroHID.git` 克隆此项目到本地

2. 用 IDEA（或者和我一样的VEPJG环境） 打开此项目

3. 在 Gradle 点击 **Tasks -> build -> build**（VEPJG环境用F5）


## 未来

我不可能每更新一次就写一次未来，索性不频繁地写了
我已经修复了一些已知问题，并且在积极更新

我打算在完善后，添加充电设备、电池什么的，但现在没想出来什么好主意

## 许可证

本项目的不同部分受不同的开源许可证保护

### 1. 代码

**本项目代码采用 [MIT License](LICENSE)**

### 2. 模型与纹理

**我的原创模型与纹理资产采用 [Creative Commons Attribution 4.0 International (CC BY 4.0)](LICENSE-CC-BY-4.0.md) 许可证**

> 这意味着：  
> **你可以为任何目的（包括商业目的）自由地分享、修改、使用我的模型，唯一的条件是必须为我署名**
> 
> **如何署名？**  
> 当你使用我的模型时，请在任何合理的位置注明：
> 
> - **创作者：[猫德]**
>     
> - **来源：链接到此项目 ([https://github.com/Ma0de/MinecraftForgeMicroHID])**
>     
> 
> 例如，你可以在模组描述或发布帖中写上：
> 
> > “本模组使用了由 [猫德(ma0de-dev@maodelab.com)] 制作的美术资产”

### 3. 音频

**此模组的音频来自 Steam 游戏 SCP Secret Laboratory（SCP秘密实验室），其开发工作室 Northwood Studios 保留权利**

你应当在将该模组商业化时遵守 Northwood Studios 的 EULA 或其他法律文件，若出现法律争议问题，项目开发者不承担任何法律责任

### 4. 依赖项

本项目基于 **Minecraft Forge**，遵循其 [LGPL v2.1 许可证](LICENSE.txt)。

## 贡献/提交代码

提交代码时，你应该使用此项目认可的**约定式提交**：

```
<type>(可选 范围): 描述

可选 正文

可选 脚注
```

是的，type **必须** 是英文，范围、正文、脚注都必须是中文
并且在 type 处必须使用英文冒号（是":"而非"："），在正文中，使用英文或中文冒号无所谓

如果你懒得按照该方法去写，那么最好的方式是把你的更改或提交信息发给 AI，让它帮你遵守这个项目认可的提交方式

## BTW

### 许可证问题

如果你打算自己玩、做整合包、发视频，理论上你只需遵守我的许可证要求，而 Northwood Studios 大概率不会因为你发个视频、做个整合包就找上你

但如果你打算将其放入服务器模组、插件，也应当遵守项目中不同许可证的要求

同时，将其以任何形式商业化，理论上你应该替换为你合法拥有的音频文件，或在有 Northwood Studios 明确、书面授权的情况下使用 Northwood Studios 的音频文件

### 游戏版本兼容

我目前并没有将该项目兼容到 Minecraft Java 1.20.1 以外版本的想法，如果你真的很想在其他版本体验这个模组，可以一起贡献代码

### API

我未来会进行不定时的更新，有大有小，也可能有重大且不兼容的变更，因为我不习惯写 Java，特别是 Forge 的，看个文档都快给我看死了

我平时一直在研究其他领域的内容，所以目前不想深入 Forge 模组开发弄什么指令、API之类的

### 发布

即便是代码更新我也不一定会及时发布最新的 .Jar 模组文件，因为我懒

### 文档

我提供了一些基础文档在 [这里](Docs.md)，你可以那看合成方式、怎么用。但我不保证及时更新

### 疑问

如果你有什么问题，在项目提一个 Issue 或者给我发一封以"[Minecraft Forge Mod Issue]"为标题开头的邮件，并且使用中文作为正文语言，如果你觉得可能会有歧义，也可以中英都附带上以方便我理解你的意思

我的邮箱地址是 `ma0de[ @]maodelab[do t]com` 人机滚一边去别来骚扰我。（如果你要给我发邮件，应该将 "[ @]" 替换为"@"，"[do t]"替换为"."）

如果你的邮件迟迟得不到回应，我这边建议你提一个 Github Issue