package minecrafttransportsimulator.packets.instances;

import io.netty.buffer.ByteBuf;
import minecrafttransportsimulator.baseclasses.Point3d;
import minecrafttransportsimulator.items.instances.ItemPart;
import minecrafttransportsimulator.jsondefs.JSONVehicle.VehiclePart;
import minecrafttransportsimulator.mcinterface.IWrapperNBT;
import minecrafttransportsimulator.mcinterface.IWrapperPlayer;
import minecrafttransportsimulator.mcinterface.IWrapperWorld;
import minecrafttransportsimulator.mcinterface.MasterLoader;
import minecrafttransportsimulator.packets.components.APacketVehiclePart;
import minecrafttransportsimulator.systems.PackParserSystem;
import minecrafttransportsimulator.vehicles.main.EntityVehicleF_Physics;
import minecrafttransportsimulator.vehicles.parts.APart;

/**Packet used to add/remove parts from a vehicle.  This packet only appears on clients after the
 * server has added or removed a part from the vehicle.
 * 
 * @author don_bruce
 */
public class PacketVehiclePartChange extends APacketVehiclePart{
	private final String partPackID;
	private final String partSystemName;
	private final IWrapperNBT partData;
	private boolean clickedPart;
	private Point3d partClickedOffset;
	
	public PacketVehiclePartChange(EntityVehicleF_Physics vehicle, Point3d offset){
		super(vehicle, offset);
		this.partPackID = "";
		this.partSystemName = "";
		this.partData = null;
		this.partClickedOffset = null;
	}
	
	public PacketVehiclePartChange(EntityVehicleF_Physics vehicle, Point3d offset, String partPackID, String partSystemName, IWrapperNBT partData, APart partClicked){
		super(vehicle, offset);
		this.partPackID = partPackID;
		this.partSystemName = partSystemName;
		this.partData = partData;
		this.clickedPart = partClicked != null;
		this.partClickedOffset = clickedPart ? partClicked.placementOffset : null;
	}
	
	public PacketVehiclePartChange(ByteBuf buf){
		super(buf);
		this.partPackID = readStringFromBuffer(buf);
		if(!partPackID.isEmpty()){
			this.partSystemName = readStringFromBuffer(buf);
			this.partData = MasterLoader.networkInterface.createDataFromBuffer(buf);
			this.clickedPart = buf.readBoolean();
			if(clickedPart){
				this.partClickedOffset = readPoint3dFromBuffer(buf);
			}else{
				this.partClickedOffset = null;
			}
		}else{
			this.partSystemName = "";
			this.partData = null;
			this.partClickedOffset = null;
		}
	}
	
	@Override
	public void writeToBuffer(ByteBuf buf){
		super.writeToBuffer(buf);
		writeStringToBuffer(partPackID, buf);
		if(!partPackID.isEmpty()){
			writeStringToBuffer(partSystemName, buf);
			partData.writeToBuffer(buf);
			buf.writeBoolean(clickedPart);
			if(clickedPart){
				writePoint3dToBuffer(partClickedOffset, buf);
			}
		}
	}
	
	@Override
	public boolean handle(IWrapperWorld world, IWrapperPlayer player, EntityVehicleF_Physics vehicle, Point3d offset){
		if(partPackID.isEmpty()){
			vehicle.removePart(vehicle.getPartAtLocation(offset), null);
		}else{
			ItemPart partItem = PackParserSystem.getItem(partPackID, partSystemName);
			VehiclePart packVehicleDef = vehicle.getPackDefForLocation(offset);
			vehicle.addPart(partItem.createPart(vehicle, packVehicleDef, partData, vehicle.getPartAtLocation(partClickedOffset)), false);
		}
		return true;
	}
}