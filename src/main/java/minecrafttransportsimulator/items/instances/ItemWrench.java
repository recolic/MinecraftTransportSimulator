package minecrafttransportsimulator.items.instances;

import java.util.List;

import minecrafttransportsimulator.guis.instances.GUIDevEditor;
import minecrafttransportsimulator.guis.instances.GUIInstruments;
import minecrafttransportsimulator.guis.instances.GUITextEditor;
import minecrafttransportsimulator.items.components.AItemBase;
import minecrafttransportsimulator.items.components.IItemVehicleInteractable;
import minecrafttransportsimulator.mcinterface.IWrapperNBT;
import minecrafttransportsimulator.mcinterface.IWrapperPlayer;
import minecrafttransportsimulator.mcinterface.MasterLoader;
import minecrafttransportsimulator.packets.instances.PacketPlayerChatMessage;
import minecrafttransportsimulator.systems.ConfigSystem;
import minecrafttransportsimulator.systems.PackParserSystem;
import minecrafttransportsimulator.vehicles.main.EntityVehicleF_Physics;
import minecrafttransportsimulator.vehicles.parts.APart;

public class ItemWrench extends AItemBase implements IItemVehicleInteractable{
	
	@Override
	public void addTooltipLines(List<String> tooltipLines, IWrapperNBT data){
		tooltipLines.add(MasterLoader.coreInterface.translate("info.item.wrench.use"));
		tooltipLines.add(MasterLoader.coreInterface.translate("info.item.wrench.useblock"));
		tooltipLines.add(MasterLoader.coreInterface.translate("info.item.wrench.attack"));
		tooltipLines.add(MasterLoader.coreInterface.translate("info.item.wrench.sneakattack"));
		if(ConfigSystem.configObject.client.devMode.value){
			tooltipLines.add("Use while riding a vehicle to open the devMode editor.");
		}
	}
	
	@Override
	public CallbackType doVehicleInteraction(EntityVehicleF_Physics vehicle, APart part, IWrapperPlayer player, PlayerOwnerState ownerState, boolean rightClick){
		//If the player isn't the owner of the vehicle, they can't interact with it.
		if(!ownerState.equals(PlayerOwnerState.USER)){
			if(rightClick){
				if(vehicle.world.isClient()){
					if(ConfigSystem.configObject.client.devMode.value && vehicle.equals(player.getEntityRiding())){
						MasterLoader.guiInterface.openGUI(new GUIDevEditor(vehicle));
					}else if(player.isSneaking()){
						MasterLoader.guiInterface.openGUI(new GUITextEditor(vehicle));
					}else{
						MasterLoader.guiInterface.openGUI(new GUIInstruments(vehicle, player));
					}
				}else{
					return CallbackType.PLAYER;
				}
			}else if(!vehicle.world.isClient()){
				if(part != null && !player.isSneaking()){
					//Player can remove part.  Spawn item in the world and remove part.
					//Make sure to remove the part before spawning the item.  Some parts
					//care about this order and won't spawn items unless they've been removed.
					vehicle.removePart(part, null);
					ItemPart droppedItem = part.getItem();
					if(droppedItem != null){
						vehicle.world.spawnItem(droppedItem, part.getData(), part.worldPos);
					}
				}else if(player.isSneaking()){
					//Attacker is a sneaking player with a wrench.
					//Remove this vehicle if possible.
					if((!ConfigSystem.configObject.general.opPickupVehiclesOnly.value || ownerState.equals(PlayerOwnerState.ADMIN)) && (!ConfigSystem.configObject.general.creativePickupVehiclesOnly.value || player.isCreative())){
						//TODO this will need to be changed when vehicles don't share common definitions.
						AItemBase vehicleItem = PackParserSystem.getItem(vehicle.definition);
						IWrapperNBT vehicleData = MasterLoader.coreInterface.createNewTag();
						vehicle.save(vehicleData);
						vehicle.world.spawnItem(vehicleItem, vehicleData, vehicle.position);
						vehicle.isValid = false;
					}
				}
			}
		}else{
			player.sendPacket(new PacketPlayerChatMessage("interact.failure.vehicleowned"));
		}
		return CallbackType.NONE;
	}
}