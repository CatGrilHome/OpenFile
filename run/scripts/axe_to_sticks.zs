// 手持任意斧子右键木板 → 木板消失 → 掉落 2 木棍 → 斧子扣 1 耐久

events.onPlayerRightClickBlock(function(event as crafttweaker.api.event.PlayerRightClickBlockEvent) {

    val held = event.usedItem;
    if (isNull(held) || !held.toolClasses.contains("axe")) return;

    val blockState = event.world.getBlockState(event.position);
    val blockId = blockState.block.definition.id;
    val meta = blockState.meta;

    // 矿物词典匹配 plankWood（兼容所有模组木板）
    var isPlank = false;
    for item in ore("plankWood").items {
        if (item.definition.id == blockId && (item.damage == meta || item.damage == 32767)) {
            isPlank = true;
            break;
        }
    }
    if (!isPlank) return;

    if (event.world.remote) return;

    // 消耗木板
    event.world.setBlockState(<blockstate:minecraft:air>, event.position);
    // 斧子扣 1 耐久
    event.damageItem(1);
    // 掉落 2 木棍
    val drop = (<minecraft:stick> * 2).createEntityItem(event.world, event.position);
    event.world.spawnEntity(drop);
});
