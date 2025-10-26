# **MSKills - Kill Reward Plugin**
A professional Minecraft kill reward system with advanced features like cooldowns, IP checking, sound effects, and multi-language support.

## 🚀 Features
* **Core Functionality**
* **Kill Rewards:** Customizable rewards for player kills

* **Cooldown System:** Player-specific and global cooldowns

* **IP Protection:** Prevent reward farming from same IP addresses

* **Multi-language:** Support for English and Turkish

* **Sound Effects:** Customizable sound notifications

* **ActionBar Messages:** Real-time kill notifications

## Advanced Features
* **Configurable Rewards:** Flexible reward system with commands and items

* **Time Format Options:** Multiple time display formats

* **Permission System:** Fine-grained permission control

* **Debug Mode:** Advanced debugging for troubleshooting

* **Real-time Configuration:** Reload configuration without restart

## 📦 Installation
Download the latest `MSKills.jar` from releases

Place the jar file in your server's `plugins` folder

Restart your server

Configure the plugin to your liking

Reload with `/mskills reload` or restart server

## ⚙️ Configuration
**Main Configuration (`config.yml`)**
```
# Plugin settings
settings:
language: "en"  # en or tr
debug: false

# Feature toggles
features:
rewards: true
ip-check: true
cooldown: true
sound: true
actionbar: true

# Cooldown settings (in seconds)
cooldowns:
player-cooldown: 300
global-cooldown: 60

# Sound settings
sounds:
kill-sound: "ENTITY_PLAYER_LEVELUP"
volume: 1.0
pitch: 1.0

# Reward system
rewards:
commands:
- "eco give %killer% 100"
- "broadcast &a%killer% &7eliminated &c%victim%&7!"
items:
- "DIAMOND:1"
- "GOLD_INGOT:5"
Message Configuration
The plugin supports multiple languages. Default messages are provided in English and Turkish.
```

## 🎮 Commands
|     Command     |   Permission  |      description     |
|:---------------:|:-------------:|:--------------------:|
| /mskills reload | mskills.admin | Reload configuration |
| /mskills status | mskills.admin | Show plugin status   |
| /mskills help   | mskills.admin | Show help menu       |
| /mskills debug  | mskills.admin | Toggle debug mode    |
## 🔧 Permissions
|     Command     |   Permission  |      description     |
|:---------------:|:-------------:|:--------------------:|
| /mskills reload | mskills.admin | Reload configuration |
| /mskills status | mskills.admin | Show plugin status   |
| /mskills help   | mskills.admin | Show help menu       |
| /mskills debug  | mskills.admin | Toggle debug mode    |
## 🌐 Language Support
### **Supported Languages**
**English (en) - Default**

**Turkish (tr) - Türkçe**

## 📋 Default Messages
### English Messages
```
reward-given: "&aYou received rewards for killing &e%victim%&a!"
cooldown: "&cYou must wait &e%time% &cbefore killing this player again!"
same-ip: "&cYou cannot receive rewards for killing players with the same IP!" 
```
### Turkish Messages
```
reward-given: "&a&e%victim%&a adlı oyuncuyu öldürerek ödül aldınız!"
cooldown: "&cBu oyuncuyu tekrar öldürmeden önce &e%time% &cbeklemelisiniz!"
same-ip: "&cAynı IP'ye sahip oyuncuları öldürerek ödül alamazsınız!"
```
## 🔄 Time Formats
**The plugin supports three time display formats:**

1. **Default:** 5 minutes 30 seconds
2. **Compact:** 5m 30s
3. **Detailed:** 5 minutes, 30 seconds

## 🐛 Troubleshooting

### **Common Issues**

**Q: Rewards aren't being given**

* Check if rewards are enabled in config
* Verify permissions
* Check console for errors

**Q: Cooldown not working**

* Verify cooldown settings in config
* Check if player has bypass permission

**Q: Messages not displaying**

* Verify language setting
* Check message configuration

## Debug Mode
**Enable debug mode for detailed logging:**

```
/mskills debug
```
## 📊 Metrics & Analytics
The plugin collects anonymous usage statistics to help improve performance and features. You can opt-out in the configuration.

## 🤝 Support
* [Documentation]()(soon)
* [Full Documentation]()(soon)
* [Configuration Examples]()(soon)

## Issue Reporting
**Found a bug? Please report it on our [GitHub Issues]() page with:**

* Server version
* Plugin version
* Error logs
* Steps to reproduce


## Feature Requests
Have an idea? Submit feature requests on our GitHub page!

## 🏆 Credits
Developer: Mask

Contributors: MinyusBey

Special Thanks: @wrusie


# 🔗 Links
[Download]()

[GitHub Repository]()

[Discord Support]()

[Wiki]()

Version: 1.0.0
Tested Minecraft Versions: 1.21.x
Dependencies: None
Soft Dependencies: Vault (for economy)
