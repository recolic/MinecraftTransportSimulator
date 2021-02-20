package minecrafttransportsimulator.rendering.instances;

import minecrafttransportsimulator.rendering.components.ARenderEntity;
import minecrafttransportsimulator.vehicles.main.EntityPlayerGun;

public class RenderPlayerGun extends ARenderEntity<EntityPlayerGun>{
	
	@Override
	public void renderModel(EntityPlayerGun entity, float partialTicks){
		//Don't render anything, as the player gun doesn't have a model.
	}
}
