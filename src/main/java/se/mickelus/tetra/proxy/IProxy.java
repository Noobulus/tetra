package se.mickelus.tetra.proxy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkEvent;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.items.ITetraItem;

public interface IProxy {

    public void preInit(ITetraItem[] items, ITetraBlock[] blocks);
    public void init(FMLCommonSetupEvent event, ITetraItem[] items, ITetraBlock[] blocks);
    public void postInit();

    public PlayerEntity getNetworkPlayer(NetworkEvent.Context context);
}
