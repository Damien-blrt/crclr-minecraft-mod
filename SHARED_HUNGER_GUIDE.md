# Synchronized Food/Hunger System Documentation

## Overview
This Minecraft Fabric mod implements a **synchronized food bar** across all players on the server. All players share the same hunger level, saturation, and food regeneration. When one player eats, all players gain hunger; when one player starves, all players lose health together.

## System Components

### 1. **SharedHungerDatas.java**
Holds the shared hunger state across all players:
- `sharedFoodLevel`: 0-20 (synchronized across all players)
- `sharedSaturation`: Determines how quickly hunger depletes
- `sharedExhaustion`: Internal exhaustion counter for hunger mechanics

### 2. **SharedHungerManager.java**
Manages all hunger-related updates:
- `syncHunger()`: Synchronizes hunger values to all players
- `addFood()`: Adds food to the shared pool when a player eats
- `exhaust()`: Depletes shared hunger based on player actions

### 3. **SharedHungerEvents.java**
Handles hunger events:
- Tracks starvation damage
- Manages natural health regeneration (when food ≥ 17)
- Handles regeneration every 80 ticks (~4 seconds)

### 4. **Mixins**

#### ServerPlayerMixin
- Runs on each player's tick
- Keeps each player's hunger synchronized with the shared values
- Prevents individual hunger desynchronization

#### FoodDataMixin
- Intercepts direct food data modifications
- Ensures all hunger changes go through the synchronization system

#### LivingEntityMixin (Updated)
- Already handles shared health
- Now imports SharedHungerManager for potential cross-system interactions

## How It Works

### Eating Food (Input)
1. Player eats food
2. ServerPlayerMixin detects the change
3. SharedHungerManager.addFood() is called
4. All players receive the same food level update

### Hunger Depletion (Natural)
1. Players perform actions (sprinting, jumping, etc.)
2. Exhaustion accumulates
3. SharedHungerManager.exhaust() processes it
4. All players lose hunger equally

### Health Regeneration
1. SharedHungerEvents.handleRegeneration() is called once per server tick
2. If food ≥ 17 and health < 20: players regenerate health
3. Hunger exhausted proportionally to health gained
4. All changes synchronized across all players

## Integration with Existing System

The hunger system integrates seamlessly with the existing **SharedHealthManager**:
- **Health**: Shared across all players (existing system)
- **Hunger**: Shared across all players (new system)
- **Regeneration**: Both systems work together

## How to Use

### Basic Setup (Already Done)
The system is automatically active when the mod loads. No additional setup needed:
```java
// In Crclr.java - called during mod initialization
SharedHealthEvents.register();
SharedHungerEvents.register();
```

### Using Shared Hunger API

#### Modify Shared Hunger Directly
```java
import name.crclr.data.SharedHungerManager;
import name.crclr.data.SharedHungerDatas;

// Get current shared hunger
int currentFood = SharedHungerDatas.sharedFoodLevel;
float currentSaturation = SharedHungerDatas.sharedSaturation;

// Update hunger for all players
ServerPlayer anyPlayer = server.getPlayerList().getPlayers().get(0);
SharedHungerManager.syncHunger(anyPlayer, 15, 3.0f); // Food=15, Saturation=3
```

#### Make Players Eat
```java
// Feed all players
SharedHungerManager.addFood(anyPlayer, 4, 2.0f); // Add 4 food, 2.0 saturation
```

#### Starve Players
```java
// Exhaust (deplete) shared hunger
SharedHungerManager.exhaust(anyPlayer, 5.0f); // Exhaust 5.0 hunger points
```

### Triggering Regeneration
For now, regeneration happens automatically during server ticks. To manually trigger:
```java
import name.crclr.event.SharedHungerEvents;

// Call from a server tick event or custom event
for (ServerPlayer player : server.getPlayerList().getPlayers()) {
    SharedHungerEvents.handleRegeneration(player);
    break; // Only call once per tick
}
```

## Configuration

To change regeneration frequency, modify in `SharedHungerEvents.java`:
```java
private static final long REGEN_INTERVAL = 80; // ticks between regeneration checks
// Default: 80 ticks = 4 seconds
// Decrease for faster regen, increase for slower
```

## Testing in-Game

1. **Test Eating**: 
   - Join server with 2+ players
   - One player eats food
   - All players' hunger bars should update instantly

2. **Test Starvation**: 
   - Get all players to low hunger (<3 food)
   - One player takes damage from hunger
   - All players should take damage together

3. **Test Regeneration**: 
   - Get all players to full hunger (20)
   - Stand still for 4 seconds
   - All players should regenerate health together (if above health < 20)

## Troubleshooting

### Hunger Not Syncing
- Check that `SharedHungerEvents.register()` is called in `Crclr.onInitialize()`
- Verify `ServerPlayerMixin` is listed in `crclr.mixins.json`

### Health/Hunger Desync
- Ensure players haven't modified `SharedHungerDatas` directly outside the manager
- Restart the server to reset shared values

### Build Errors
- Ensure all imports are correct
- Verify Minecraft 1.21.11 and Fabric Loader compatibility

## Future Enhancements

Potential additions:
1. Hunger effects (slowness, weakness when starving)
2. Custom hunger events (feast, famine modes)
3. Persistent hunger data (save/load across restarts)
4. Per-team hunger systems (not fully shared)
5. GUI indicator showing shared hunger is active
