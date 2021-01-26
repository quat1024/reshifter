package agency.highlysuspect.reshifter;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class StateDescription {
	public StateDescription(BlockState state) {
		this.blockId = Registry.BLOCK.getId(state.getBlock());
		state.getProperties().forEach(prop -> readProperty(state, prop));
	}
	
	public StateDescription(Identifier blockId, Map<String, String> valueNames) {
		this.blockId = blockId;
		this.valueNames.putAll(valueNames);
	}
	
	private <T extends Comparable<T>> void readProperty(BlockState state, Property<T> prop) {
		valueNames.put(prop.getName(), prop.name(state.get(prop)));
	}
	
	public final Identifier blockId;
	public final Map<String, String> valueNames = new HashMap<>();
	
	public BlockState approximateBlockState() {
		//Entire blocks that exist on the server but not the client
		if(!Registry.BLOCK.containsId(blockId)) return Blocks.AIR.getDefaultState();
		
		Block block = Registry.BLOCK.get(blockId);
		BlockState state = block.getDefaultState();
		
		for(Map.Entry<String, String> entry : valueNames.entrySet()) {
			Property<?> prop = propertyNamed(state, entry.getKey());
			if(prop == null) continue;
			state = tryWith(state, prop, entry.getValue());
		}
		
		return state;
	}
	
	private @Nullable Property<?> propertyNamed(BlockState state, String name) {
		//linear search
		for(Property<?> p : state.getProperties()) {
			if(p.getName().equals(name)) return p;
		}
		return null;
	}
	
	private <T extends Comparable<T>> BlockState tryWith(BlockState state, Property<T> prop, String valueName) {
		return prop.parse(valueName).map(value -> state.with(prop, value)).orElse(state);
	}
	
	public void write(DataOutput buf) throws IOException {
		buf.writeUTF(blockId.toString());
		buf.writeShort(valueNames.size());
		for(Map.Entry<String, String> entry : valueNames.entrySet()) {
			buf.writeUTF(entry.getKey());
			buf.writeUTF(entry.getValue());
		}
	}
	
	public static StateDescription read(DataInput buf) throws IOException {
		Identifier id = Identifier.tryParse(buf.readUTF());
		Map<String, String> props = new HashMap<>();
		short count = buf.readShort();
		for(short i = 0; i < count; i++) {
			props.put(buf.readUTF(), buf.readUTF());
		}
		return new StateDescription(id, props);
	}
}
