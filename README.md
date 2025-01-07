# Hotkey Plugin

一个轻量级的 Bukkit/Spigot 快捷键插件，支持自定义组合键执行命令。

## 功能特性

### 支持的按键组合
- Shift + F (切换副手时)
- Shift + Q (丢弃物品时)
- Shift + 左键点击
- Shift + 右键点击
- 双击 Shift

### 每个快捷键可配置
- 执行多个命令（支持玩家执行和控制台执行）
- 独立的权限节点
- 单独的冷却时间
- 多种使用条件
- 自定义音效和粒子效果
- 自定义消息（支持聊天栏和动作栏）

### 条件系统
支持多种条件检查：
- 经验等级要求
- 饥饿值要求
- 游戏模式要求
- 物品要求
- 世界限制
- 时间段限制

### 其他功能
- 全局/单独冷却设置
- 命令变量替换 (%player%)
- 完整的权限管理

## 命令

- `/hotkey list` - 查看所有快捷键
- `/hotkey reload` - 重新加载配置

## 权限

- `hotkey.admin` - 管理员权限（重载配置）

## 安装

1. 下载最新版本的插件
2. 放入服务器的 plugins 文件夹
3. 重启服务器
4. 编辑生成的配置文件
5. 使用 `/hotkey reload` 重载配置

## 要求

- Bukkit/Spigot 1.13+
- Java 17+

## 贡献

欢迎提交 Pull Request 来改进插件。请确保遵循现有的代码风格。

## 许可证

本项目采用 GNU General Public License v3.0 开源许可证，并附加以下限制：

### 你可以：
- ✅ 修改 - 修改源代码
- ✅ 分发 - 分发软件
- ✅ 私人使用 - 私人使用软件

### 必须：
- ⚠️ 开源 - 如果你分发了修改后的版本，必须开源
- ⚠️ 许可证和版权声明 - 必须包含原始许可证和版权声明
- ⚠️ 相同许可证 - 修改后的代码必须使用相同的许可证
- ⚠️ 声明更改 - 必须声明你对代码做了哪些重要更改

### 明确禁止：
- ❌ 商业用途 - 禁止用于任何商业目的
- ❌ 更改许可证条款
- ❌ 闭源分发修改后的版本
- ❌ 删除或修改版权声明
- ❌ 销售插件或修改版本

### 免责声明
- 本软件按"原样"提供，不提供任何明示或暗示的保证
- 作者不对任何使用本软件造成的损失负责
- 仅供学习和研究使用

基于 GNU GPL v3.0 并附加上述商业限制条款。完整许可证文本请参阅：[GNU GPL v3.0](https://www.gnu.org/licenses/gpl-3.0.html)

## 作者
- Thexiaoyu

## 支持

如果遇到任何问题，欢迎：
- 提交 Issue
