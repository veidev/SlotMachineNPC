# 🎰 SlotMachineNPC

A fully-featured slot machine plugin for Paper/Spigot servers using Villager NPCs. Players can gamble with emeralds in a realistic 5-reel slot machine with weighted symbols, animated spinning, and balanced payouts.

![Slot Machine Preview](https://img.shields.io/badge/Minecraft-1.21.11-brightgreen)
![Paper API](https://img.shields.io/badge/Paper_API-1.21.11-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

## ✨ Features

- **🎲 Realistic Slot Machine** - 5-reel slot machine with weighted random symbols
- **🎨 Animated Spinning** - Staggered reel stops with sound effects
- **💰 Balanced Payouts** - Carefully tuned odds for fair yet challenging gameplay
- **💎 Multiple Bet Options** - Bet 1, 2, 5, 10, 20, or 50 emeralds per spin
- **🎯 Weighted Symbols** - 7 different symbols with varying rarity and payouts
- **💣 Bomb Penalty** - 3+ bombs = total loss, adds risk and excitement
- **🔄 Partial Returns** - 2 matches always pay 1.5x bet
- **👤 Villager NPC** - Simple, clean villager NPC as the slot machine
- **💾 NPC Persistence** - NPCs save locations and respawn after server restart
- **📊 Transparent Odds** - Spawn chances and multipliers shown in GUI
- **🔊 Sound Effects** - Different sounds for wins, losses, and spins
- **🎮 Easy Commands** - Simple admin commands to spawn/remove NPCs

## 🎮 Gameplay

### Symbols & Payouts

| Symbol | Spawn Chance | 5x Match | 4x Match | 3x Match | 2x Match |
|--------|--------------|---------|---------|---------|---------|
| 🔷 Netherite | 8.5% | 80x | 16x | 6x | 1.5x |
| 💎 Diamond | 10.6% | 40x | 8x | 3x | 1.5x |
| 🥇 Gold | 12.8% | 20x | 4x | 2x | 1.5x |
| 🥈 Iron | 14.9% | 8x | 3x | 2x | 1.5x |
| 🔴 Redstone | 14.9% | 4x | 3x | 2x | 1.5x |
| ⬛ Coal | 17.0% | 2x | 2x | 2x | 1.5x |
| 💣 Bomb | 26.6% | No payout | No payout | No payout | No payout |

### Payout Tiers

- **🏆 JACKPOT** - 5 matching symbols (very rare, up to 80x bet)
- **💎 Big Win** - 4 matching symbols (rare)
- **✨ Win** - 3 matching symbols (moderate)
- **🔄 Partial** - 2 matching symbols (always 1.5x bet)
- **💣 Bomb Penalty** - 3+ bombs = total loss
- **❌ Loss** - No matches

### Balance

The slot machine is balanced for realistic gameplay:
- **Bomb chance**: ~26.6% (1 in 4 spins has a bomb)
- **Max win**: 80x bet (extremely rare, ~1/1000 spins)
- **House edge**: Built-in through bomb penalties and weighted odds
- **Fair RNG**: True random with weighted probabilities

## 📦 Installation

1. Download the latest `SlotMachineNPC.jar` from [Releases](https://github.com/yourusername/SlotMachineNPC/releases)
2. Place the JAR file in your server's `plugins/` folder
3. Restart your server or reload plugins
4. Configure permissions (see below)

## ⚙️ Configuration

### Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `slotnpc.admin` | Spawn/remove slot machine NPCs | OP |
| `slotnpc.play` | Play the slot machine | Everyone |

### Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/slotnpc` | Spawn a slot machine NPC at your location | `slotnpc.admin` |
| `/slotnpcremove` | Remove the nearest slot machine NPC (within 10 blocks) | `slotnpc.admin` |

### How to Play

1. Find a Slot Machine NPC (villager named "Slot Machine")
2. Right-click the NPC to open the slot machine GUI
3. Adjust your bet using the +/- buttons (1, 2, 5, 10, 20, 50 emeralds)
4. Click **SPIN!** to play
5. Watch the reels spin and see if you win!

## 🔧 Development

### Building from Source

```bash
git clone https://github.com/yourusername/SlotMachineNPC.git
cd SlotMachineNPC
./gradlew build
```

The built JAR will be in `build/libs/`.

### Requirements

- Java 17+
- Paper API 1.21.11
- Gradle 9.1+

## 📝 Configuration File

The plugin automatically generates a `config.yml` file:

```yaml
npcs:
  - "uuid;world;x;y;z"  # NPC data saved automatically
```

NPC locations are saved and respawned on server restart.

## 🤝 Contributing

Contributions are welcome! Feel free to:
- Report bugs
- Suggest new features
- Submit pull requests
- Improve documentation

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Credits

- Built with [Paper API](https://papermc.io/)
- Inspired by real-world slot machine mechanics


**Enjoy the thrill of the slots! 🎰💰**
