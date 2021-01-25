package agency.highlysuspect.reshifter;

import com.mojang.brigadier.StringReader;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.IdList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Etc {
	public static final Logger LOGGER = LogManager.getLogger("Reshifter");
	
//	public static void serializeStatemap(PacketByteBuf buf, IdList<BlockState> list) {
//		//I'm really fucking sorry
//		buf.writeInt(list.size());
//		for(BlockState b : list) {
//			buf.writeString(b.toString());
//		}
//	}
//	
//	public static IdList<BlockState> deserializeStatemap(PacketByteBuf buf) {
//		IdList<BlockState> list = new IdList<>();
//		
//		int count = buf.readInt();
//		for(int i = 0; i < count; i++) {
//			String stringifiedState = buf.readString();
//			//im really fucking sorry here too
//			BlockArgumentParser haha = new BlockArgumentParser(new StringReader(stringifiedState), false);
//			BlockState state = null;
//			try {
//				haha.parse(false);
//				state = haha.getBlockState();
//			} catch (Exception e) {
//				//Nope
//			}
//			
//			list.set(state, i);
//		}
//		
//		return list;
//	}
}
